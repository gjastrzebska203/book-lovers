package com.booklovers.community.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingStatDto {
    @NotNull(message = "Ocena nie może być pusta")
    @Min(value = 1, message = "Ocena musi wynosić minimum 1")
    @Max(value = 10, message = "Ocena może wynosić maksimum 10")
    private Integer rating; // liczba gwiazdek (np. 5)

    @NotNull(message = "Liczba głosów nie może być pusta")
    @Min(value = 0, message = "Liczba głosów nie może być ujemna")
    private Long count;     // ile osób dało taką ocenę
}
