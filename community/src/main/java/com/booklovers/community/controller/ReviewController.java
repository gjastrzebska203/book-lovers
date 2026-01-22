package com.booklovers.community.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.booklovers.community.dto.CreateReviewRequest;
import com.booklovers.community.model.Review;
import com.booklovers.community.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Recenzje", description = "Zarządzanie recenzjami (API dla Postmana/Frontendu)")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Pobierz recenzje dla książki")
    @GetMapping("/books/{bookId}/reviews")
    public ResponseEntity<List<Review>> getReviewsForBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(reviewService.getReviewsForBook(bookId));
    }

    @Operation(summary = "Dodaj recenzję do książki", description = "Wymaga bycia zalogowanym (Bearer Token lub Sesja)")
    @PostMapping("/books/{bookId}/reviews")
    public ResponseEntity<?> addReview(@PathVariable Long bookId,
                                       @Valid @RequestBody CreateReviewRequest request,
                                       java.security.Principal principal) {
        reviewService.addReview(bookId, principal.getName(), request.getRating(), request.getContent());
        
        return ResponseEntity.status(HttpStatus.CREATED).body("Recenzja została dodana");
    }

    @Operation(summary = "Pobierz wszystkie recenzje (Admin)")
    @GetMapping("/admin/reviews")
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @Operation(summary = "Usuń recenzję (Admin)")
    @DeleteMapping("/admin/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}