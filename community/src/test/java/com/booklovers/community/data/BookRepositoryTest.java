package com.booklovers.community.data;

import static org.assertj.core.api.Assertions.assertThat;

// import java.util.List;
// import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.booklovers.community.model.Author;
import com.booklovers.community.model.Book;
// import com.booklovers.community.model.Review;
// import com.booklovers.community.model.User;
import com.booklovers.community.repository.AuthorRepository;
import com.booklovers.community.repository.BookRepository;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    // @Autowired
    // private TestEntityManager entityManager;

    // zapis
    @Test
    void shouldSaveBook() {
        // given
        Author author = Author.builder()
                .firstName("Andrzej")
                .lastName("Sapkowski")
                .build();
        authorRepository.save(author);

        Book book = Book.builder()
                .title("Wiedźmin")
                .isbn("9788375900934")
                .author(author)
                .build();

        // when
        Book savedBook = bookRepository.save(book);

        // then
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo("Wiedźmin");
    }

    // existsByIsbn
    @Test
    void shouldCheckIfBookExistsByIsbn() {
        // given
        Author author = Author.builder().firstName("J.K.").lastName("Rowling").build();
        authorRepository.save(author);
        
        Book book = Book.builder()
                .title("Harry Potter")
                .isbn("9788380082113")
                .author(author)
                .build();
        bookRepository.save(book);

        // when
        boolean exists = bookRepository.existsByIsbn("9788380082113");

        // then
        assertThat(exists).isTrue();
    }

    // custom @Query searchBooks
    @Test
    void shouldSearchBooksByPartialTitle() {
        // given
        Author author = Author.builder().firstName("George").lastName("Orwell").build();
        authorRepository.save(author);
        
        bookRepository.save(Book.builder().title("Rok 1984").isbn("9780155658110").author(author).build());
        bookRepository.save(Book.builder().title("Folwark Zwierzęcy").isbn("9780194267533").author(author).build());

        // when
        Page<Book> result = bookRepository.searchBooks("198", PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Rok 1984");
    }

    // // findByTitle (Sukces)
    // @Test
    // void shouldFindBookByTitle() {
    //     // given
    //     Author author = authorRepository.save(Author.builder().firstName("J.R.R.").lastName("Tolkien").build());
    //     bookRepository.save(Book.builder().title("Hobbit").isbn("111").author(author).build());

    //     // when
    //     Optional<Book> found = bookRepository.findByTitle("Hobbit");

    //     // then
    //     assertThat(found).isPresent();
    //     assertThat(found.get().getIsbn()).isEqualTo("111");
    // }

    // // existsByAuthorId (Prawda/Fałsz)
    // @Test
    // void shouldCheckIfBookExistsByAuthorId() {
    //     // given
    //     Author authorWithBook = authorRepository.save(Author.builder().firstName("A").lastName("B").build());
    //     Author authorWithoutBook = authorRepository.save(Author.builder().firstName("C").lastName("D").build());
        
    //     bookRepository.save(Book.builder().title("Książka").isbn("222").author(authorWithBook).build());

    //     // when
    //     boolean exists1 = bookRepository.existsByAuthorId(authorWithBook.getId());
    //     boolean exists2 = bookRepository.existsByAuthorId(authorWithoutBook.getId());

    //     // then
    //     assertThat(exists1).isTrue();
    //     assertThat(exists2).isFalse();
    // }

    // // searchBooks - po nazwisku autora
    // @Test
    // void shouldSearchBooksByAuthorLastName() {
    //     // given
    //     Author author = authorRepository.save(Author.builder().firstName("Stephen").lastName("King").build());
    //     bookRepository.save(Book.builder().title("To").isbn("333").author(author).build());

    //     // when
    //     Page<Book> result = bookRepository.searchBooks("King", PageRequest.of(0, 10));

    //     // then
    //     assertThat(result.getContent()).hasSize(1);
    //     assertThat(result.getContent().get(0).getAuthor().getLastName()).isEqualTo("King");
    // }

    // // searchBooks - po ISBN
    // @Test
    // void shouldSearchBooksByIsbn() {
    //     // given
    //     Author author = authorRepository.save(Author.builder().firstName("A").lastName("B").build());
    //     bookRepository.save(Book.builder().title("Książka").isbn("978-1-2345").author(author).build());

    //     // when
    //     Page<Book> result = bookRepository.searchBooks("1-2345", PageRequest.of(0, 10));

    //     // then
    //     assertThat(result.getContent()).hasSize(1);
    //     assertThat(result.getContent().get(0).getIsbn()).isEqualTo("978-1-2345");
    // }

    // // searchBooks - ignorowanie wielkości liter (Case Insensitive)
    // @Test
    // void shouldSearchBooksCaseInsensitive() {
    //     // given
    //     Author author = authorRepository.save(Author.builder().firstName("Adam").lastName("Mickiewicz").build());
    //     bookRepository.save(Book.builder().title("Pan Tadeusz").isbn("444").author(author).build());

    //     // when - szukamy małym "pan"
    //     Page<Book> result = bookRepository.searchBooks("pan", PageRequest.of(0, 10));

    //     // then
    //     assertThat(result.getContent()).hasSize(1);
    // }

    // // findMostPopularBooks (Złożone zapytanie z JOIN)
    // @Test
    // void shouldFindMostPopularBooksOrderedByReviewCount() {
    //     // given
    //     Author author = authorRepository.save(Author.builder().firstName("Autor").lastName("Testowy").build());
        
    //     // Tworzymy dwie książki
    //     Book popularBook = bookRepository.save(Book.builder().title("Popularna").isbn("POP").author(author).build());
    //     // Book lessPopularBook = bookRepository.save(Book.builder().title("Mało znana").isbn("LOW").author(author).build());
        
    //     // Tworzymy użytkownika (potrzebny do recenzji)
    //     User user = new User();
    //     user.setUsername("krytyk");
    //     user.setEmail("k@k.pl");
    //     user.setPassword("pass");
    //     user.setRole("USER");
    //     entityManager.persist(user); // Zapisujemy usera przez EntityManager

    //     // Dodajemy 2 recenzje do "Popularna"
    //     entityManager.persist(Review.builder().book(popularBook).user(user).rating(5).content("Super").build());
    //     entityManager.persist(Review.builder().book(popularBook).user(user).rating(4).content("Ok").build());
        
    //     // Dodajemy 0 recenzji do "Mało znana"
        
    //     entityManager.flush(); // Wypychamy zmiany do bazy przed zapytaniem

    //     // when
    //     List<Book> result = bookRepository.findMostPopularBooks(PageRequest.of(0, 10));

    //     // then
    //     assertThat(result).hasSize(2);
    //     assertThat(result.get(0).getTitle()).isEqualTo("Popularna"); // Pierwsza powinna być ta z 2 recenzjami
    //     assertThat(result.get(1).getTitle()).isEqualTo("Mało znana"); // Druga ta z 0
    // }
}