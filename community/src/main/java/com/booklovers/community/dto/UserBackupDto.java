package com.booklovers.community.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBackupDto {
    private String username;
    private String email;
    private String bio;
    private String joinDate;
    private List<ShelfBackupDto> shelves;
    private List<ReviewBackupDto> reviews;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShelfBackupDto {
        private String name;
        private List<String> books;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewBackupDto {
        private String bookTitle;
        private Integer rating;
        private String content;
        private String createdAt;
    }
}
