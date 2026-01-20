package com.booklovers.community.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import com.booklovers.community.dao.BookStatisticsDao;
import com.booklovers.community.dto.UserBackupDto;
import com.booklovers.community.dto.UserRegisterDto;
import com.booklovers.community.model.Book;
import com.booklovers.community.model.Review;
import com.booklovers.community.model.Shelf;
import com.booklovers.community.model.User;
import com.booklovers.community.repository.BookRepository;
import com.booklovers.community.repository.ReviewRepository;
import com.booklovers.community.repository.ShelfRepository;
import com.booklovers.community.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Validated
public class UserService {
    private final UserRepository userRepository;
    private final ShelfRepository shelfRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookStatisticsDao bookStatisticsDao;
    private final FileStorageService fileStorageService;
    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;

    @Transactional
    public User registerUser(@Valid @NotNull UserRegisterDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Nazwa użytkownika zajęta!");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email już istnieje!");
        }

        // tworzenie użytkownika
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) // Kodowanie hasła
                .role("ROLE_USER")
                .build();

        User savedUser = userRepository.save(user);

        // tworzenie domyślnych półek (wymaganie specyficzne)
        createDefaultShelves(savedUser);

        return savedUser;
    }

    private void createDefaultShelves(User user) {
        List<String> defaultShelves = List.of("Przeczytane", "Chcę przeczytać", "Teraz czytam");
        
        List<Shelf> shelves = defaultShelves.stream()
            .map(name -> Shelf.builder()
                    .name(name)
                    .user(user)
                    .isSystemShelf(true) // Oznaczamy jako systemowe
                    .build())
            .toList();
            
        shelfRepository.saveAll(shelves);
    }

    // aktualizacja profilu
    @Transactional
    public void updateUserProfile(@NotBlank String username, @Size(max = 1000) String newBio, MultipartFile avatarFile) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBio(newBio); // aktualizacja Bio
        // aktualizacja avatara (jeśli przesłano nowy plik)
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(avatarFile);
            user.setAvatar("/uploads/" + fileName); // Zapisujemy ścieżkę webową
        }

        userRepository.save(user);
    }

    // metoda do statystyk
    public int getBooksReadThisYear(@NotNull Long userId) {
        int currentYear = java.time.LocalDate.now().getYear();
        return bookStatisticsDao.countBooksReadInYear(userId, currentYear);
    }
    
    // metoda pomocnicza do pobrania user
    public User findByUsername(@NotBlank String username) {
         return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // blokowanie/odblokowanie użytkownika
    @Transactional
    public void toggleUserBlock(@NotNull Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        // Odwracamy wartość (true -> false, false -> true)
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }
    
    // lista użytkowników
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // usuwanie z anonimizacją (admin)
    @Transactional
    public void deleteUserByAdmin(Long userId) {
        reviewRepository.anonymizeReviewsByUserId(userId);
        shelfRepository.deleteAllByUserId(userId);
        userRepository.deleteById(userId);
    }

    // generowanie JSON
    public byte[] generateProfileBackup(String username) {
        try {
            User user = findByUsername(username);
            
            List<Shelf> shelves = shelfRepository.findAllByUserId(user.getId());
            List<Review> reviews = reviewRepository.findAllByUserId(user.getId());

            List<UserBackupDto.ShelfBackupDto> shelvesDto = shelves.stream()
                .map(s -> UserBackupDto.ShelfBackupDto.builder()
                        .name(s.getName())
                        .books(s.getBooks().stream().map(Book::getTitle).toList())
                        .build())
                .toList();

            List<UserBackupDto.ReviewBackupDto> reviewsDto = reviews.stream()
                .map(r -> UserBackupDto.ReviewBackupDto.builder()
                        .bookTitle(r.getBook().getTitle())
                        .rating(r.getRating())
                        .content(r.getContent())
                        .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : "")
                        .build())
                .toList();

            UserBackupDto backupDto = UserBackupDto.builder()
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .bio(user.getBio())
                    .joinDate(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "")
                    .shelves(shelvesDto)
                    .reviews(reviewsDto)
                    .build();

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); 
            
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(backupDto);

        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas generowania backupu", e);
        }
    }

    @Transactional
    public void importProfile(String username, MultipartFile file) {
        try {
            User user = findByUsername(username);
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            UserBackupDto backup = mapper.readValue(file.getInputStream(), UserBackupDto.class);

            if (backup.getShelves() != null) {
                for (UserBackupDto.ShelfBackupDto shelfDto : backup.getShelves()) {
                    
                    // 1. Znajdź istniejącą półkę lub stwórz nową (jeśli to np. półka niestandardowa)
                    Shelf shelf = shelfRepository.findByNameAndUserId(shelfDto.getName(), user.getId())
                            .orElseGet(() -> {
                                Shelf newShelf = new Shelf();
                                newShelf.setName(shelfDto.getName());
                                newShelf.setUser(user);
                                newShelf.setBooks(new ArrayList<>());
                                return shelfRepository.save(newShelf);
                            });

                    // 2. Iterujemy po tytułach książek z pliku JSON
                    for (String bookTitle : shelfDto.getBooks()) {
                        
                        // WALIDACJA: Szukamy książki w bazie po tytule
                        bookRepository.findByTitle(bookTitle).ifPresent(book -> {
                            // Wykona się TYLKO jeśli książka istnieje w bazie
                            
                            // Sprawdzamy, czy książki już nie ma na półce (unikamy duplikatów)
                            if (!shelf.getBooks().contains(book)) {
                                shelf.getBooks().add(book);
                            }
                        });
                        // Jeśli książka nie istnieje -> ifPresent się nie wykona -> pomijamy ją milcząco
                    }
                    shelfRepository.save(shelf);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas importu danych: " + e.getMessage(), e);
        }
    }
}
