package com.booklovers.community.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
// import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.booklovers.community.model.Author;
import com.booklovers.community.model.Book;
// import com.booklovers.community.model.Book;
import com.booklovers.community.model.Shelf;
import com.booklovers.community.model.User;
import com.booklovers.community.repository.AuthorRepository;
import com.booklovers.community.repository.BookRepository;
// import com.booklovers.community.repository.BookRepository;
import com.booklovers.community.repository.ShelfRepository;
import com.booklovers.community.repository.UserRepository;

@DataJpaTest
public class ShelfRepositoryTest {
    @Autowired
    private ShelfRepository shelfRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

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

    // test usuwania wszystkich półek użytkownika
    @Test
    void shouldDeleteAllShelvesByUserId() {
        // given
        User user = userRepository.save(User.builder().username("deleteMe").email("del@test.pl").password("pass").role("USER").build());
        User otherUser = userRepository.save(User.builder().username("safe").email("safe@test.pl").password("pass").role("USER").build());

        shelfRepository.save(Shelf.builder().name("S1").user(user).build());
        shelfRepository.save(Shelf.builder().name("S2").user(user).build());
        shelfRepository.save(Shelf.builder().name("S3").user(otherUser).build()); 

        // when
        shelfRepository.deleteAllByUserId(user.getId());

        // then
        List<Shelf> userShelves = shelfRepository.findAllByUserId(user.getId());
        assertThat(userShelves).isEmpty();

        List<Shelf> otherUserShelves = shelfRepository.findAllByUserId(otherUser.getId());
        assertThat(otherUserShelves).hasSize(1); 
    }

    // test znajdowania półki po nazwie i ID usera (Znaleziono)
    @Test
    void shouldFindShelfByNameAndUserId() {
        // given
        User user = userRepository.save(User.builder().username("finder").email("f@test.pl").password("pass").role("USER").build());
        Shelf shelf = shelfRepository.save(Shelf.builder().name("Szukana").user(user).build());

        // when
        Optional<Shelf> found = shelfRepository.findByNameAndUserId("Szukana", user.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(shelf.getId());
    }

    // test znajdowania półki po nazwie (Nie znaleziono)
    @Test
    void shouldReturnEmptyWhenShelfNotFoundByName() {
        // given
        User user = userRepository.save(User.builder().username("finder2").email("f2@test.pl").password("pass").role("USER").build());
        shelfRepository.save(Shelf.builder().name("Istnieje").user(user).build());

        // when
        Optional<Shelf> found = shelfRepository.findByNameAndUserId("Nieistnieje", user.getId());

        // then
        assertThat(found).isEmpty();
    }

    // test Query: Liczenie czytelników (ile razy książka występuje na półkach)
    @Test
    void shouldCountReadersByBookId() {
        // given
        // 1. Tworzymy książkę (uproszczone, dostosuj do swojej encji Book/Author)
        Book book = Book.builder().title("Popularna Książka").isbn("9780261102217").build();
        Author author = authorRepository.save(Author.builder().firstName("ABB").lastName("BBB").build());
        book.setAuthor(author);
        book = bookRepository.save(book);

        // 2. Tworzymy userów
        User u1 = userRepository.save(User.builder().username("r12").email("r1@test.pl").password("p").role("USER").build());
        User u2 = userRepository.save(User.builder().username("r23").email("r2@test.pl").password("p").role("USER").build());
        User u3 = userRepository.save(User.builder().username("r34").email("r3@test.pl").password("p").role("USER").build());

        // 3. Tworzymy półki i dodajemy tę samą książkę do dwóch userów
        Shelf s1 = Shelf.builder().name("P1").user(u1).books(new ArrayList<>()).build();
        s1.getBooks().add(book);
        shelfRepository.save(s1);

        Shelf s2 = Shelf.builder().name("P2").user(u2).books(new ArrayList<>()).build();
        s2.getBooks().add(book);
        shelfRepository.save(s2);

        // User 3 ma półkę, ale bez tej książki
        Shelf s3 = Shelf.builder().name("P3").user(u3).books(new ArrayList<>()).build();
        shelfRepository.save(s3);

        // when
        long count = shelfRepository.countReadersByBookId(book.getId());

        // then
        assertThat(count).isEqualTo(2);
    }
}
