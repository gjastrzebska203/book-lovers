package com.booklovers.community.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tytuł nie może być pusty")
    @Size(min = 2, max = 100, message = "Tytuł musi mieć od 2 do 100 znaków")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "ISBN jest wymagany")
    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$", message = "Niepoprawny format ISBN")
    @Column(unique = true)
    private String isbn;

    @Size(max = 255, message = "Ścieżka do pliku jest zbyt długa")
    private String coverImage; //ścieżka do pliku graficznego

    @Size(max = 4000, message = "Opis nie może być dłuższy niż 4000 znaków")
    @Column(columnDefinition = "TEXT")
    private String description;

    // relacja N:1 (wiele książek -> jeden autor)
    @NotNull(message = "Autor jest wymagany")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore // klucz obcy w tabeli books
    private Author author;

    // relacja 1:N do Recenzji (jedna książka -> wiele recenzji)
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Review> reviews;

}
