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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {
    
    private final BookService bookService;

    // wymaganie: GET (lista z paginacją)
    // przykład użycia: GET /api/v1/books?page=0&size=10
    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<BookDto>> getAllBooks(Pageable pageable) {
        return ResponseEntity.ok(bookService.getAllBooks(pageable));
    }

    // wymaganie: GET z parametrem (wyszukiwanie)
    // przykład użycia: GET /api/v1/books/search?query=Harry
    @GetMapping("/search")
    public ResponseEntity<Page<BookDto>> searchBooks(@RequestParam String query, Pageable pageable) {
        return ResponseEntity.ok(bookService.searchBooks(query, pageable));
    }

    // wymaganie: GET (single) + @PathVariable
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    // dodatkowe: statystyki z JdbcTemplate (wykres ocen)
    @GetMapping("/{id}/stats")
    public ResponseEntity<List<RatingStatDto>> getBookStats(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookRatingStats(id));
    }

    // wymaganie: POST (tworzenie) + @RequestBody + kod 201
    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        bookService.saveBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    // wymaganie: PUT (aktualizacja) + @PathVariable + @RequestBody + kod 200
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book bookDetails) {
        bookDetails.setId(id);
        bookService.saveBook(bookDetails);
        
        return ResponseEntity.ok(bookDetails);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

}
