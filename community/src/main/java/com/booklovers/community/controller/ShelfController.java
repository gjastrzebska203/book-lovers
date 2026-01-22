package com.booklovers.community.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.booklovers.community.model.Shelf;
import com.booklovers.community.service.ShelfService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/shelves")
@RequiredArgsConstructor
@Tag(name = "Półki", description = "Zarządzanie półkami użytkownika (API)")
public class ShelfController {

    private final ShelfService shelfService;
    
    @Operation(summary = "Pobierz półki zalogowanego użytkownika")
    @GetMapping
    public ResponseEntity<List<Shelf>> getUserShelves(java.security.Principal principal) {
        return ResponseEntity.ok(shelfService.getUserShelves(principal.getName()));
    }

    @Operation(summary = "Utwórz nową półkę")
    @PostMapping
    public ResponseEntity<?> createShelf(@RequestParam String name,
                                         java.security.Principal principal) {
        try {
            shelfService.createShelf(name, principal.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body("Półka została utworzona");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Dodaj książkę do półki")
    @PostMapping("/{shelfId}/books")
    public ResponseEntity<?> addBookToShelf(@PathVariable Long shelfId,
                                            @RequestParam Long bookId,
                                            java.security.Principal principal) {
        try {
            shelfService.addBookToShelf(shelfId, bookId, principal.getName());
            return ResponseEntity.ok("Książka dodana do półki");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Usuń książkę z półki")
    @DeleteMapping("/{shelfId}/books/{bookId}")
    public ResponseEntity<?> removeBookFromShelf(@PathVariable Long shelfId,
                                                 @PathVariable Long bookId,
                                                 java.security.Principal principal) {
        try {
            shelfService.removeBookFromShelf(shelfId, bookId, principal.getName());
            return ResponseEntity.noContent().build(); 
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Przenieś książkę na inną półkę")
    @PostMapping("/{shelfId}/move")
    public ResponseEntity<?> moveBook(@PathVariable Long shelfId,
                                      @RequestParam Long bookId,
                                      @RequestParam Long targetShelfId,
                                      java.security.Principal principal) {
        try {
            shelfService.moveBook(shelfId, targetShelfId, bookId, principal.getName());
            return ResponseEntity.ok("Książka przeniesiona pomyślnie");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}