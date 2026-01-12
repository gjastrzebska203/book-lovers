package com.booklovers.community.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.booklovers.community.dto.UserRegisterDto;
import com.booklovers.community.service.BookService;
import com.booklovers.community.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final BookService bookService;
    private final UserService userService;

    // strona główna
    @GetMapping("/")
    public String home() {
        return "index"; // szuka pliku templates/index.html
    }

    // formularz rejestracji (GET)
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        // przekazujemy pusty obiekt do formularza (wymagane przez th:object)
        model.addAttribute("user", new UserRegisterDto());
        return "register";
    }

    // obsługa rejestracji (POST)
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegisterDto userDto, 
                               BindingResult result, 
                               Model model) {
        
        // 1. walidacja adnotacji (@NotBlank itp.)
        if (result.hasErrors()) {
            return "register"; // jeśli są błędy, wróć do formularza i pokaż je
        }

        try {
            // 2. próba rejestracji
            userService.registerUser(userDto);
            return "redirect:/login?success"; // przekierowanie po sukcesie
        } catch (RuntimeException e) {
            // 3. obsługa błędu biznesowego (np. email zajęty)
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // strona logowania
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // lista książek
    @GetMapping("/books")
    public String listBooks(
            @RequestParam(defaultValue = "") String query, // szukana fraza (domyślnie pusta)
            @RequestParam(defaultValue = "0") int page,    // numer strony (od 0)
            @RequestParam(defaultValue = "10") int size,   // rozmiar strony
            Model model) {

        // obiekt paginacji (strona, wielkość)
        PageRequest pageable = PageRequest.of(page, size);
        
        Page<?> bookPage;

        if (query == null || query.isBlank()) {
            bookPage = bookService.getAllBooks(pageable);
        } else {
            bookPage = bookService.searchBooks(query, pageable);
        }
        
        // przekazujemy dane do widoku
        model.addAttribute("books", bookPage);
        model.addAttribute("query", query); // żeby w pasku wyszukiwania został wpisany tekst
        
        return "books";
    }

}
