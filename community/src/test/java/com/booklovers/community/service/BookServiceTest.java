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
import com.booklovers.community.repository.ShelfRepository;
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

    @Mock
    private ShelfRepository shelfRepository;

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

    // pobieranie wszystkich książek (paginacja)
    @Test
    void shouldReturnAllBooksPageMappedToDto() {
        // given
        PageRequest pageable = PageRequest.of(0, 5);
        Author author = Author.builder().firstName("Test").lastName("Author").build();
        Book book = Book.builder().id(1L).title("Book 1").author(author).build();
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        // when
        Page<BookDto> result = bookService.getAllBooks(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Book 1");
        verify(bookRepository).findAll(pageable);
    }

    // zapis książki
    @Test
    void shouldSaveBook() {
        // given
        Book book = new Book();
        book.setTitle("New Book");

        // when
        bookService.saveBook(book);

        // then
        verify(bookRepository).save(book);
    }

    // pobieranie encji (nie DTO) - sukces
    @Test
    void shouldReturnBookEntityById() {
        // given
        Long id = 1L;
        Book book = new Book();
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        // when
        Book result = bookService.findEntityById(id);

        // then
        assertThat(result).isEqualTo(book);
    }

    // pobieranie encji - błąd (nie znaleziono)
    @Test
    void shouldThrowExceptionWhenEntityNotFound() {
        // given
        Long id = 99L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> bookService.findEntityById(id));

        // then
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class);
    }

    // najpopularniejsze książki
    @Test
    void shouldReturnMostPopularBooks() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 6);
        List<Book> books = List.of(new Book(), new Book());
        when(bookRepository.findMostPopularBooks(pageRequest)).thenReturn(books);

        // when
        List<Book> result = bookService.getMostPopularBooks();

        // then
        assertThat(result).hasSize(2);
        verify(bookRepository).findMostPopularBooks(pageRequest);
    }

    // statystyki książki (Dobre dane)
    @Test
    void shouldReturnCorrectBookStatistics() {
        // given
        Long bookId = 1L;
        
        when(reviewRepository.getAverageRatingForBook(bookId)).thenReturn(4.5);
        
        when(reviewRepository.countByBookId(bookId)).thenReturn(100L);
        when(shelfRepository.countReadersByBookId(bookId)).thenReturn(50L);
        
        List<Object[]> histogram = List.of(
            new Object[]{5, 2L}, // Ocena 5, 2 głosy
            new Object[]{1, 1L}  // Ocena 1, 1 głos
        );
        when(reviewRepository.getRatingDistribution(bookId)).thenReturn(histogram);

        // when
        var stats = bookService.getBookStatistics(bookId);

        // then
        assertThat(stats.getAverageRating()).isEqualTo(4.5);
        assertThat(stats.getRatingCount()).isEqualTo(100L);
        assertThat(stats.getReaderCount()).isEqualTo(50L);
        
        // Sprawdzanie mapy rozkładu
        assertThat(stats.getRatingDistribution().get(5)).isEqualTo(2L);
        assertThat(stats.getRatingDistribution().get(1)).isEqualTo(1L);
        assertThat(stats.getRatingDistribution().get(3)).isEqualTo(0L); 
    }

    // statystyki książki (brak danych / wartości null)
    @Test
    void shouldHandleNullValuesInStatistics() {
        // given
        Long bookId = 2L;

        when(reviewRepository.getAverageRatingForBook(bookId)).thenReturn(null); // Brak ocen
        when(reviewRepository.countByBookId(bookId)).thenReturn(0L);
        when(shelfRepository.countReadersByBookId(bookId)).thenReturn(0L);
        when(reviewRepository.getRatingDistribution(bookId)).thenReturn(List.of()); // Pusty histogram

        // when
        var stats = bookService.getBookStatistics(bookId);

        // then
        assertThat(stats.getAverageRating()).isEqualTo(0.0);
        assertThat(stats.getRatingCount()).isEqualTo(0L);
        assertThat(stats.getRatingDistribution()).hasSize(10); 
    }
}
