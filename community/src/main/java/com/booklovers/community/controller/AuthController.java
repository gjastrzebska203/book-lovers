package com.booklovers.community.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.booklovers.community.dto.UserRegisterDto;
import com.booklovers.community.model.User;
import com.booklovers.community.service.UserService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;

    // wymaganie: POST (tworzenie), @RequestBody, ResponseEntity z kodem 201
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegisterDto registerDto) {
        User registeredUser = userService.registerUser(registerDto);
        
        // zwracamy 201 Created i ID utworzonego użytkownika
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Użytkownik utworzony. ID: " + registeredUser.getId());
    }

}
