package com.booklovers.community.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookDto {
    private Long id;
    private String title;
    private String authorName; // zamiast całej encji Author
    private String isbn;
    private String coverImage;
    private Double averageRating; // obliczona średnia
}
