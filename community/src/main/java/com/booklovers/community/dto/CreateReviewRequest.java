package com.booklovers.community.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReviewRequest {
    @NotNull(message = "Ocena jest wymagana")
    @Min(1) @Max(10)
    private Integer rating;

    @NotBlank(message = "Treść recenzji nie może być pusta")
    private String content;
}
