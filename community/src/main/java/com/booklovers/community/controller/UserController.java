package com.booklovers.community.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.booklovers.community.dto.UserRegisterDto;
import com.booklovers.community.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Użytkownicy", description = "Operacje na użytkownikach (Rejestracja, Profil, Admin)")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Rejestracja użytkownika")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegisterDto userDto) {
        userService.registerUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Użytkownik zarejestrowany pomyślnie");
    }

    @Operation(summary = "Aktualizacja profilu (Bio i Avatar)")
    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @RequestParam(required = false) String bio,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            java.security.Principal principal) {
        
        userService.updateUserProfile(principal.getName(), bio, avatar);
        return ResponseEntity.ok("Profil zaktualizowany");
    }

    @Operation(summary = "Pobierz backup profilu (JSON)")
    @GetMapping("/profile/export")
    public ResponseEntity<byte[]> exportProfile(java.security.Principal principal) {
        String username = principal.getName();
        byte[] fileContent = userService.generateProfileBackup(username);
        String filename = "backup_" + username + ".json";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fileContent);
    }

    @Operation(summary = "Importuj profil z backupu")
    @PostMapping(value = "/profile/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importProfile(@RequestParam("file") MultipartFile file,
                                           java.security.Principal principal) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Plik jest pusty");
        }
        try {
            userService.importProfile(principal.getName(), file);
            return ResponseEntity.ok("Profil zaimportowany pomyślnie");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd importu: " + e.getMessage());
        }
    }

    @Operation(summary = "Zablokuj/Odblokuj użytkownika")
    @PatchMapping("/{id}/toggle-block")
    public ResponseEntity<?> toggleUserBlock(@PathVariable Long id) {
        userService.toggleUserBlock(id);
        return ResponseEntity.ok("Status blokady zmieniony");
    }

    @Operation(summary = "Usuń użytkownika (Admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUserByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}