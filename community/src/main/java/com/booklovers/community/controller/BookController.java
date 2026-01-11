package com.booklovers.community.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.booklovers.community.dto.BookDto;
import com.booklovers.community.dto.RatingStatDto;
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
}
