package com.booklovers.community.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.booklovers.community.model.User;
import com.booklovers.community.repository.UserRepository;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    // znajdowanie po nazwie użytkownika
    @Test
    void shouldFindUserByUsername() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("securePass123")
                .role("ROLE_USER")
                .build();
        
        userRepository.save(user);

        // when
        Optional<User> found = userRepository.findByUsername("testuser");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    // existsByEmail
    @Test
    void shouldReturnTrueIfEmailExists() {
        // given
        User user = User.builder()
                .username("anna")
                .email("anna@example.com")
                .password("pass")
                .role("ROLE_USER")
                .build();
        
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByEmail("anna@example.com");

        // then
        assertThat(exists).isTrue();
    }

    // znajdowanie po emailu (sukces)
    @Test
    void shouldFindUserByEmail() {
        // given
        User user = User.builder()
                .username("emailUser")
                .email("findme@example.com")
                .password("pass")
                .role("ROLE_USER")
                .build();
        
        userRepository.save(user);

        // when
        Optional<User> found = userRepository.findByEmail("findme@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("emailUser");
    }

    // znajdowanie po emailu (nie znaleziono)
    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        // given (baza pusta lub brak tego maila)

        // when
        Optional<User> found = userRepository.findByEmail("ghost@example.com");

        // then
        assertThat(found).isEmpty();
    }

    // znajdowanie po nazwie użytkownika (nie znaleziono)
    @Test
    void shouldReturnEmptyWhenUsernameNotFound() {
        // given

        // when
        Optional<User> found = userRepository.findByUsername("nieistniejący");

        // then
        assertThat(found).isEmpty();
    }

    // existsByUsername (Prawda)
    @Test
    void shouldReturnTrueIfUsernameExists() {
        // given
        User user = User.builder()
                .username("existingUser")
                .email("e@e.com")
                .password("p")
                .role("USER")
                .build();
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByUsername("existingUser");

        // then
        assertThat(exists).isTrue();
    }

    // existsByUsername (Fałsz)
    @Test
    void shouldReturnFalseIfUsernameDoesNotExist() {
        // when
        boolean exists = userRepository.existsByUsername("nobody");

        // then
        assertThat(exists).isFalse();
    }

    // existsByEmail (Fałsz)
    @Test
    void shouldReturnFalseIfEmailDoesNotExist() {
        // when
        boolean exists = userRepository.existsByEmail("fake@email.com");

        // then
        assertThat(exists).isFalse();
    }
}
