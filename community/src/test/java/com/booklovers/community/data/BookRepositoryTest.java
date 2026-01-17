package com.booklovers.community.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.booklovers.community.model.Author;
import com.booklovers.community.model.Book;
import com.booklovers.community.repository.AuthorRepository;
import com.booklovers.community.repository.BookRepository;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

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
}