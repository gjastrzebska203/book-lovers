package com.booklovers.community.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.booklovers.community.dao.BookStatisticsDao;
import com.booklovers.community.dto.BookDto;
import com.booklovers.community.dto.RatingStatDto;
import com.booklovers.community.exception.ResourceNotFoundException;
import com.booklovers.community.model.Book;
import com.booklovers.community.repository.BookRepository;
import com.booklovers.community.repository.ReviewRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Validated
public class BookService {
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final BookStatisticsDao bookStatisticsDao;
    
    // pobieranie listy książek z paginacją i mapowaniem na DTO
    @Transactional(readOnly = true)
    public Page<BookDto> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    // wyszukiwanie (Tytuł, Autor, ISBN)
    @Transactional(readOnly = true)
    public Page<BookDto> searchBooks(String query, Pageable pageable) {
        return bookRepository.searchBooks(query, pageable)
                .map(this::mapToDto);
    }

    // pobranie szczegółów książki (można by tu dodać statystyki z DAO)
    @Transactional(readOnly = true)
    public BookDto getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Książka nie znaleziona: " + id));
        return mapToDto(book);
    }

    // przykład użycia naszego DAO (JdbcTemplate)
    public List<RatingStatDto> getBookRatingStats(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Książka nie istnieje");
        }
        return bookStatisticsDao.getRatingDistribution(bookId);
    }

    // metoda pomocnicza: mapowanie Entity -> DTO
    private BookDto mapToDto(Book book) {
        // pobieramy średnią ocenę z repozytorium recenzji
        Double avgRating = reviewRepository.getAverageRatingForBook(book.getId());
        
        return BookDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .coverImage(book.getCoverImage())
                .authorName(book.getAuthor().getFirstName() + " " + book.getAuthor().getLastName())
                .description(book.getDescription())
                .averageRating(avgRating != null ? avgRating : 0.0)
                .build();
    }

    @Transactional
    public void saveBook(@Valid @NotNull Book book) {
        bookRepository.save(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
             throw new ResourceNotFoundException("Nie można usunąć. Książka nie istnieje: " + id);
        }
        bookRepository.deleteById(id);
    }

    public Book findEntityById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Książka nie znaleziona"));
    }

}
