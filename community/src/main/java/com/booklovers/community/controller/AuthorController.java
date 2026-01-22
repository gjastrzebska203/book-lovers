package com.booklovers.community.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.booklovers.community.model.Author;
import com.booklovers.community.service.AuthorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Tag(name = "Autorzy", description = "Zarządzanie autorami (API)")
public class AuthorController {

    private final AuthorService authorService;

    @Operation(summary = "Pobierz wszystkich autorów")
    @GetMapping
    public ResponseEntity<List<Author>> getAllAuthors() {
        return ResponseEntity.ok(authorService.getAllAuthors());
    }

    @Operation(summary = "Pobierz autora po ID")
    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable Long id) {
        return ResponseEntity.ok(authorService.getAuthorById(id));
    }

    @Operation(summary = "Dodaj nowego autora")
    @PostMapping
    public ResponseEntity<Author> createAuthor(@Valid @RequestBody Author author) {
        author.setId(null); 
        authorService.saveAuthor(author);
        return ResponseEntity.status(HttpStatus.CREATED).body(author);
    }

    @Operation(summary = "Zaktualizuj dane autora")
    @PutMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(@PathVariable Long id, @Valid @RequestBody Author authorDetails) {
        Author existingAuthor = authorService.getAuthorById(id);
        
        existingAuthor.setFirstName(authorDetails.getFirstName());
        existingAuthor.setLastName(authorDetails.getLastName());
        existingAuthor.setBio(authorDetails.getBio());

        authorService.saveAuthor(existingAuthor);
        return ResponseEntity.ok(existingAuthor);
    }

    @Operation(summary = "Usuń autora")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAuthor(@PathVariable Long id) {
        try {
            authorService.deleteAuthor(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}