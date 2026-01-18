package com.booklovers.community.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.booklovers.community.model.Shelf;
import com.booklovers.community.model.User;
import com.booklovers.community.repository.ShelfRepository;
import com.booklovers.community.repository.UserRepository;

@DataJpaTest
public class ShelfRepositoryTest {
    @Autowired
    private ShelfRepository shelfRepository;

    @Autowired
    private UserRepository userRepository;

    // pobieranie wszystkich półek danego użytkownika
    @Test
    void shouldFindAllShelvesByUserId() {
        // given
        User user1 = userRepository.save(User.builder().username("user1").email("u1@test.pl").password("pass").role("ROLE_USER").build());
        User user2 = userRepository.save(User.builder().username("user2").email("u2@test.pl").password("pass").role("ROLE_USER").build());

        shelfRepository.save(Shelf.builder().name("Przeczytane").user(user1).build());
        shelfRepository.save(Shelf.builder().name("Chcę przeczytać").user(user1).build());

        shelfRepository.save(Shelf.builder().name("Przeczytane").user(user2).build());

        // when
        List<Shelf> shelves = shelfRepository.findAllByUserId(user1.getId());

        // then
        assertThat(shelves).hasSize(2); 
        assertThat(shelves).extracting(Shelf::getName)
                .containsExactlyInAnyOrder("Przeczytane", "Chcę przeczytać");
    }

    // zwracanie pustej listy, gdy user nie ma półek
    @Test
    void shouldReturnEmptyListWhenUserHasNoShelves() {
        // given
        User user = userRepository.save(User.builder().username("lonely").email("lonely@test.pl").password("pass").role("ROLE_USER").build());

        // when
        List<Shelf> shelves = shelfRepository.findAllByUserId(user.getId());

        // then
        assertThat(shelves).isEmpty();
    }

    // sprawdzanie istnienia półki (existsByNameAndUserId) - TRUE
    @Test
    void shouldReturnTrueIfShelfExistsForUser() {
        // given
        User user = userRepository.save(User.builder().username("tester").email("t@test.pl").password("pass").role("ROLE_USER").build());
        shelfRepository.save(Shelf.builder().name("Ulubione").user(user).build());

        // when
        boolean exists = shelfRepository.existsByNameAndUserId("Ulubione", user.getId());

        // then
        assertThat(exists).isTrue();
    }

    // sprawdzanie istnienia półki - FALSE (inna nazwa)
    @Test
    void shouldReturnFalseIfShelfNameDoesNotExistForUser() {
        // given
        User user = userRepository.save(User.builder().username("tester2").email("t2@test.pl").password("pass").role("ROLE_USER").build());
        shelfRepository.save(Shelf.builder().name("Fantasy").user(user).build());

        // when
        boolean exists = shelfRepository.existsByNameAndUserId("Sci-Fi", user.getId());

        // then
        assertThat(exists).isFalse();
    }

    // sprawdzanie istnienia półki - FALSE (ten sam tytuł, ale inny user)
    @Test
    void shouldReturnFalseIfShelfExistsButForDifferentUser() {
        // given
        User user1 = userRepository.save(User.builder().username("jan").email("jan@test.pl").password("pass").role("ROLE_USER").build());
        User user2 = userRepository.save(User.builder().username("anna").email("anna@test.pl").password("pass").role("ROLE_USER").build());

        shelfRepository.save(Shelf.builder().name("Historia").user(user1).build());

        // when
        boolean exists = shelfRepository.existsByNameAndUserId("Historia", user2.getId());

        // then
        assertThat(exists).isFalse();
    }
}
