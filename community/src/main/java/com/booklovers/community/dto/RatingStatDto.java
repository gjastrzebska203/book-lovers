package com.booklovers.community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingStatDto {
    private Integer rating; // liczba gwiazdek (np. 5)
    private Long count;     // ile osób dało taką ocenę
}
