package com.booklovers.community.dto;

import java.util.Map;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookStatsDto {
    @NotNull(message = "Średnia ocena nie może być nullem")
    @DecimalMin(value = "0.0", message = "Średnia ocena nie może być mniejsza niż 0")
    @DecimalMax(value = "10.0", message = "Średnia ocena nie może być większa niż 10")
    private Double averageRating;   

    @NotNull(message = "Liczba ocen nie może być nullem")
    @Min(value = 0, message = "Liczba ocen nie może być ujemna")
    private Long ratingCount;    

    @NotNull(message = "Liczba czytelników nie może być nullem")
    @Min(value = 0, message = "Liczba czytelników nie może być ujemna")
    private Long readerCount;

    @NotNull(message = "Rozkład ocen nie może być nullem")
    private Map<Integer, Long> ratingDistribution;
    
    public int getPercentage(int star) {
        if (ratingCount == 0) return 0;
        long count = ratingDistribution.getOrDefault(star, 0L);
        return (int) ((count * 100) / ratingCount);
    }
}
