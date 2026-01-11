package com.booklovers.community.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
