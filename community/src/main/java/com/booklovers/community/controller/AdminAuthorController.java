package com.booklovers.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.booklovers.community.model.Author;
import com.booklovers.community.service.AuthorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/authors")
@RequiredArgsConstructor
@Tag(name = "Autorzy - zarządzanie przez admina", description = "Zarządzanie listą użytkowników przez admina")
public class AdminAuthorController {
    private final AuthorService authorService;

    @Operation(summary = "Pobierz listę autorów", description = "Zwraca paginowaną listę wszystkich autorów.")
    @GetMapping
    public String listAuthors(Model model) {
        model.addAttribute("authors", authorService.getAllAuthors());
        return "admin/authors";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("author", new Author());
        return "admin/author-form";
    }

    // formularz edycji
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("author", authorService.getAuthorById(id));
        return "admin/author-form";
    }

    // zapis (Dodawanie i Edycja)
    @PostMapping("/save")
    public String saveAuthor(@ModelAttribute Author author) {
        authorService.saveAuthor(author);
        return "redirect:/admin/authors?success";
    }

    // usuwanie
    @PostMapping("/delete/{id}")
    public String deleteAuthor(@PathVariable Long id) {
        try {
            authorService.deleteAuthor(id);
            return "redirect:/admin/authors?deleted";
        } catch (RuntimeException e) {
            return "redirect:/admin/authors?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
