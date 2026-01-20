package com.booklovers.community.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.booklovers.community.model.Author;
import com.booklovers.community.repository.AuthorRepository;
import com.booklovers.community.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private AuthorService authorService;

    // test pobierania wszystkich autorów
    @Test
    void shouldReturnAllAuthors() {
        // given
        Author a1 = new Author();
        a1.setFirstName("Adam");
        Author a2 = new Author();
        a2.setFirstName("Juliusz");

        when(authorRepository.findAll()).thenReturn(List.of(a1, a2));

        // when
        List<Author> result = authorService.getAllAuthors();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).contains(a1, a2);
    }

    // test pobierania autora po ID - Sukces
    @Test
    void shouldReturnAuthorById() {
        // given
        Long id = 1L;
        Author author = new Author();
        author.setId(id);
        
        when(authorRepository.findById(id)).thenReturn(Optional.of(author));

        // when
        Author result = authorService.getAuthorById(id);

        // then
        assertThat(result).isEqualTo(author);
        assertThat(result.getId()).isEqualTo(id);
    }

    // test pobierania autora po ID - błąd (nie znaleziono)
    @Test
    void shouldThrowExceptionWhenAuthorNotFound() {
        // given
        Long id = 99L;
        when(authorRepository.findById(id)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> authorService.getAuthorById(id));

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessage("Autor nie znaleziony");
    }

    // test zapisywania autora
    @Test
    void shouldSaveAuthor() {
        // given
        Author author = new Author();
        author.setFirstName("Henryk");

        // when
        authorService.saveAuthor(author);

        // then
        verify(authorRepository).save(author);
    }

    // test usuwania autora - Sukces (brak książek)
    @Test
    void shouldDeleteAuthorWhenNoBooksAssigned() {
        // given
        Long authorId = 1L;
        when(bookRepository.existsByAuthorId(authorId)).thenReturn(false);

        // when
        authorService.deleteAuthor(authorId);

        // then
        verify(authorRepository).deleteById(authorId);
    }

    // test usuwania autora - błąd (autor ma książki)
    @Test
    void shouldThrowExceptionWhenDeletingAuthorWithBooks() {
        // given
        Long authorId = 1L;
        when(bookRepository.existsByAuthorId(authorId)).thenReturn(true);

        // when
        Throwable thrown = catchThrowable(() -> authorService.deleteAuthor(authorId));

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie można usunąć autora, który ma przypisane książki");
        
        verify(authorRepository, never()).deleteById(any());
    }
}