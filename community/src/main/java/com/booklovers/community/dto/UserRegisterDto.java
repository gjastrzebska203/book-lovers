package com.booklovers.community.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDto {
    @NotBlank(message = "Nazwa użytkownika jest wymagana")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, message = "Hasło musi mieć minimum 6 znaków")
    private String password;

    @NotBlank
    @Email(message = "Niepoprawny format email")
    private String email;
}
