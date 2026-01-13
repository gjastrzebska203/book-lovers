package com.booklovers.community.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.booklovers.community.dao.BookStatisticsDao;
import com.booklovers.community.dto.UserRegisterDto;
import com.booklovers.community.model.Shelf;
import com.booklovers.community.model.User;
import com.booklovers.community.repository.ShelfRepository;
import com.booklovers.community.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ShelfRepository shelfRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookStatisticsDao bookStatisticsDao;
    private final FileStorageService fileStorageService;

    @Transactional
    public User registerUser(UserRegisterDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Nazwa użytkownika zajęta!");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email już istnieje!");
        }

        // 1. Tworzenie użytkownika
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) // Kodowanie hasła
                .role("ROLE_USER")
                .build();

        User savedUser = userRepository.save(user);

        // 2. Tworzenie domyślnych półek (wymaganie specyficzne)
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
    public void updateUserProfile(String username, String newBio, MultipartFile avatarFile) {
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
    public int getBooksReadThisYear(Long userId) {
        int currentYear = java.time.LocalDate.now().getYear();
        return bookStatisticsDao.countBooksReadInYear(userId, currentYear);
    }
    
    // metoda pomocnicza do pobrania user
    public User findByUsername(String username) {
         return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
