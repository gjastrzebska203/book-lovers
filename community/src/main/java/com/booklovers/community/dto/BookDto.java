package com.booklovers.community.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor
public class BookDto {
    private Long id;

    @NotBlank(message = "Tytuł nie może być pusty")
    @Size(min = 2, max = 100, message = "Tytuł musi mieć od 2 do 100 znaków")
    private String title;

    @NotBlank(message = "Nazwa autora jest wymagana")
    @Size(max = 100, message = "Nazwa autora jest zbyt długa")
    private String authorName; 

    @NotBlank(message = "ISBN jest wymagany")
    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$", message = "Niepoprawny format ISBN")
    private String isbn;

    @Size(max = 255, message = "Ścieżka do okładki jest zbyt długa")
    private String coverImage;

    private String description;

    @Min(value = 1, message = "Ocena nie może być mniejsza niż 1")
    @Max(value = 10, message = "Ocena nie może być większa niż 10")
    private Double averageRating;
}
