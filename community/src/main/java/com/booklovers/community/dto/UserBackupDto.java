package com.booklovers.community.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBackupDto {
    @NotBlank(message = "Nazwa użytkownika w backupie jest wymagana")
    private String username;

    @Email(message = "Niepoprawny format adresu email w backupie")
    @NotBlank(message = "Email w backupie jest wymagany")
    private String email;

    private String bio;
    private String joinDate;

    @Valid 
    @NotNull(message = "Lista półek nie może być nullem")
    private List<ShelfBackupDto> shelves;

    @Valid 
    private List<ReviewBackupDto> reviews;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShelfBackupDto {
        @NotBlank(message = "Nazwa półki nie może być pusta")
        private String name;

        @NotNull(message = "Lista książek na półce nie może być nullem")
        private List<String> books;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewBackupDto {
        @NotBlank(message = "Tytuł książki w recenzji jest wymagany")
        private String bookTitle;

        @Min(value = 1, message = "Minimalna ocena to 1")
        @Max(value = 10, message = "Maksymalna ocena to 10")
        @NotNull(message = "Ocena jest wymagana")
        private Integer rating;

        private String content;
        private String createdAt;
    }
}

