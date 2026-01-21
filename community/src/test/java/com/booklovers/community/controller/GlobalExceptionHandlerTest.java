package com.booklovers.community.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.booklovers.community.exception.ResourceNotFoundException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Konfigurujemy MockMvc w trybie standalone, rejestrując nasz DummyController
        // oraz testowany GlobalExceptionHandler.
        this.mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // 1. Test dla ResourceNotFoundException (404)
    @Test
    void shouldHandleResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/test/resource-not-found"))
                .andExpect(status().isNotFound()) // Oczekujemy 404
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Zasób nie istnieje")));
    }

    // 2. Test dla RuntimeException (400 - zgodnie z Twoim kodem)
    @Test
    void shouldHandleRuntimeException() throws Exception {
        mockMvc.perform(get("/test/runtime-exception"))
                .andExpect(status().isBadRequest()) // Oczekujemy 400
                .andExpect(jsonPath("$.error", is("Conflict / Internal Error")))
                .andExpect(jsonPath("$.message", is("Błąd ogólny")));
    }

    // 3. Test dla MethodArgumentNotValidException (Walidacja - 400)
    @Test
    void shouldHandleValidationException() throws Exception {
        // given
        String invalidJson = "{\"name\": \"\"}"; // Puste pole, które jest @NotBlank

        // when & then
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest()) // Oczekujemy 400
                // Sprawdzamy czy mapa błędów zawiera pole "name" i komunikat
                .andExpect(jsonPath("$.name", is("Nie może być puste")));
    }

    // --- KLASY POMOCNICZE (Dummy Controller & DTO) ---

    /**
     * Kontroler pomocniczy, który służy tylko do rzucania wyjątków
     * w celu przetestowania Handlera.
     */
    @RestController
    static class DummyController {

        @GetMapping("/test/resource-not-found")
        public void throwResourceNotFound() {
            throw new ResourceNotFoundException("Zasób nie istnieje");
        }

        @GetMapping("/test/runtime-exception")
        public void throwRuntimeException() {
            throw new RuntimeException("Błąd ogólny");
        }

        @PostMapping("/test/validation")
        public void testValidation(@Valid @RequestBody TestDto dto) {
            // Metoda pusta - interesuje nas tylko, czy @Valid zadziała przed wejściem tutaj
        }
    }

    /**
     * Proste DTO do testowania walidacji.
     */
    @Data
    static class TestDto {
        @NotBlank(message = "Nie może być puste")
        private String name;
    }
}
