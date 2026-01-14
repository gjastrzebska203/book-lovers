package com.booklovers.community.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.booklovers.community.dto.BookDto;
import com.booklovers.community.dto.RatingStatDto;
import com.booklovers.community.model.Book;
import com.booklovers.community.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Książki", description = "Zarządzanie katalogiem książek i przeglądanie")
public class BookController {
    
    private final BookService bookService;

    @Operation(summary = "Pobierz listę książek", description = "Zwraca paginowaną listę wszystkich książek.")
    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<BookDto>> getAllBooks(Pageable pageable) {
        return ResponseEntity.ok(bookService.getAllBooks(pageable));
    }

    @Operation(summary = "Szczegóły książki", description = "Pobiera informacje o książce na podstawie ID.")
    @GetMapping("/search")
    public ResponseEntity<Page<BookDto>> searchBooks(@RequestParam String query, Pageable pageable) {
        return ResponseEntity.ok(bookService.searchBooks(query, pageable));
    }

    @Operation(summary = "Wyszukiwanie książek", description = "Szuka książek po tytule, autorze lub ISBN.")
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @Operation(summary = "Statystyki ocen", description = "Zwraca histogram ocen dla danej książki (dane z JdbcTemplate).")
    @GetMapping("/{id}/stats")
    public ResponseEntity<List<RatingStatDto>> getBookStats(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookRatingStats(id));
    }

    @Operation(summary = "Dodaj nową książkę", description = "Dostępne tylko dla Administratora.")
    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        bookService.saveBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    @Operation(summary = "Zaktualizuj książkę", description = "Edycja danych istniejącej książki. Tylko Admin.")
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book bookDetails) {
        bookDetails.setId(id);
        bookService.saveBook(bookDetails);
        
        return ResponseEntity.ok(bookDetails);
    }

    @Operation(summary = "Usuń książkę", description = "Trwałe usunięcie książki z bazy. Tylko Admin.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

}
