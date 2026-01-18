package com.booklovers.community.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import com.booklovers.community.dao.BookStatisticsDao;
import com.booklovers.community.dto.UserRegisterDto;
import com.booklovers.community.model.Shelf;
import com.booklovers.community.model.User;
import com.booklovers.community.repository.ReviewRepository;
import com.booklovers.community.repository.ShelfRepository;
import com.booklovers.community.repository.UserRepository;

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
}
