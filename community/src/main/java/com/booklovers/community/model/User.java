package com.booklovers.community.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nazwa użytkownika jest wymagana")
    @Size(min = 3, max = 50, message = "Nazwa użytkownika musi mieć od 3 do 50 znaków")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Hasło jest wymagane")
    @Column(nullable = false)
    private String password; // zahaszowane hasło

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Niepoprawny format adresu email")
    @Column(nullable = false, unique = true)
    private String email;

    @Size(max = 255, message = "Ścieżka do avatara jest zbyt długa")
    private String avatar; 

    @Size(max = 1000, message = "Bio nie może być dłuższe niż 1000 znaków")
    private String bio;

    @NotBlank
    private String role; // np. "ROLE_USER", "ROLE_ADMIN"

    // jeden użytkownik -> wiele półek
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Shelf> shelves = new ArrayList<>();

    // jeden użytkownik -> wiele recenzji
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true; 

}
