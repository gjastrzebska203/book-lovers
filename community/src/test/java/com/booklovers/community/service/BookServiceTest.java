package com.booklovers.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.booklovers.community.dao.BookStatisticsDao;
import com.booklovers.community.dto.BookDto;
import com.booklovers.community.exception.ResourceNotFoundException;
import com.booklovers.community.model.Author;
import com.booklovers.community.model.Book;
import com.booklovers.community.repository.BookRepository;
import com.booklovers.community.repository.ReviewRepository;
import com.booklovers.community.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    
    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookStatisticsDao bookStatisticsDao;

    @InjectMocks
    private BookService bookService;

    @InjectMocks
    private UserService userService;

    @InjectMocks
    private FileStorageService fileStorageService;

    // generowanie CSV
    @Test
    void shouldExportBooksToCsvFormat() {
        // given
        Author author = Author.builder().firstName("Adam").lastName("Mickiewicz").build();
        
        Book b1 = Book.builder().id(1L).title("Pan Tadeusz").isbn("123").author(author).build();
        Book b2 = Book.builder().id(2L).title("Dziady").isbn("456").author(author).build();

        when(bookRepository.findAll()).thenReturn(List.of(b1, b2));
        when(reviewRepository.getAverageRatingForBook(1L)).thenReturn(8.5);
        when(reviewRepository.getAverageRatingForBook(2L)).thenReturn(null);

        // when
        byte[] csvBytes = bookService.exportBooksToCsv();
        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);

        // then
        assertThat(csvContent).contains("ID;Tytul;Autor;ISBN;Ocena");
        assertThat(csvContent).contains("1;Pan Tadeusz;Adam Mickiewicz;123;8.5");
        assertThat(csvContent).contains("2;Dziady;Adam Mickiewicz;456;0.0");
    }

    // pobieranie książki po ID
    @Test
    void shouldReturnBookDtoWithCorrectMapping() {
        // given
        Long bookId = 1L;
        Author author = Author.builder().firstName("J.R.R.").lastName("Tolkien").build();
        Book book = Book.builder()
                .id(bookId)
                .title("Hobbit")
                .isbn("123")
                .author(author)
                .build();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(reviewRepository.getAverageRatingForBook(bookId)).thenReturn(9.5);

        // when
        BookDto result = bookService.getBookById(bookId);

        // then
        assertThat(result.getTitle()).isEqualTo("Hobbit");
        assertThat(result.getAuthorName()).isEqualTo("J.R.R. Tolkien");
        assertThat(result.getAverageRating()).isEqualTo(9.5);
    }

    // pobieranie nieistniejącej książki (Błąd)
    @Test
    void shouldThrowExceptionWhenBookNotFound() {
        // given
        Long bookId = 999L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> bookService.getBookById(bookId));

        // then
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Książka nie znaleziona");
    }

    // usuwanie książki (Sukces)
    @Test
    void shouldDeleteBookWhenExists() {
        // given
        Long bookId = 1L;
        when(bookRepository.existsById(bookId)).thenReturn(true);

        // when
        bookService.deleteBook(bookId);

        // then
        verify(bookRepository).deleteById(bookId);
    }

    // usuwanie nieistniejącej książki (Błąd)
    @Test
    void shouldThrowExceptionWhenDeletingNonExistentBook() {
        // given
        Long bookId = 999L;
        when(bookRepository.existsById(bookId)).thenReturn(false);

        // when
        Throwable thrown = catchThrowable(() -> bookService.deleteBook(bookId));

        // then
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class);
        verify(bookRepository, never()).deleteById(anyLong());
    }

    // wyszukiwanie (Paginacja)
    @Test
    void shouldSearchBooksAndMapToDto() {
        // given
        String query = "Wiedźmin";
        PageRequest pageable = PageRequest.of(0, 10);
        
        Author author = Author.builder().firstName("A").lastName("S").build();
        Book book = Book.builder().id(1L).title("Wiedźmin").author(author).build();
        
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        when(bookRepository.searchBooks(query, pageable)).thenReturn(bookPage);
        when(reviewRepository.getAverageRatingForBook(1L)).thenReturn(null); 

        // when
        Page<BookDto> result = bookService.searchBooks(query, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Wiedźmin");
        assertThat(result.getContent().get(0).getAverageRating()).isEqualTo(0.0);
    }
}
