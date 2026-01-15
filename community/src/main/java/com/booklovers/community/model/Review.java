package com.booklovers.community.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Ocena jest wymagana")
    @Min(value = 1, message = "Minimalna ocena to 1")
    @Max(value = 10, message = "Maksymalna ocena to 10")
    @Column(nullable = false)
    private Integer rating; // 1-10

    @NotBlank(message = "Treść recenzji nie może być pusta")
    @Size(min = 5, max = 4000, message = "Recenzja musi mieć od 5 do 4000 znaków")
    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    // wiele recenzji -> jedna książka
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // wiele recenzji -> jeden użytkownik
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "user_id") // może być null jeśli usuniemy usera (anonimizacja)
    private User user;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
