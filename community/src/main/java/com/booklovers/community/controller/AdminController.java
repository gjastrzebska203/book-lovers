package com.booklovers.community.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.booklovers.community.model.Book;
import com.booklovers.community.repository.AuthorRepository;
import com.booklovers.community.service.BookService;
import com.booklovers.community.service.ReviewService;
import com.booklovers.community.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserService userService;
    private final BookService bookService;
    private final ReviewService reviewService;
    private final AuthorRepository authorRepository; 

    @GetMapping
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/books")
    public String manageBooks(Model model, Pageable pageable) {
        model.addAttribute("books", bookService.getAllBooks(pageable));
        return "admin/books";
    }

    @GetMapping("/books/new")
    public String showCreateBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("authors", authorRepository.findAll());
        return "admin/book-form";
    }

    @GetMapping("/books/edit/{id}")
    public String showUpdateBookForm(@PathVariable Long id, Model model) {
        Book book = bookService.findEntityById(id); 
        model.addAttribute("book", book);
        model.addAttribute("authors", authorRepository.findAll());
        return "admin/book-form"; // Reużywamy tego samego formularza
    }

    @PostMapping("/books/save")
    public String saveBook(@Valid @ModelAttribute Book book, BindingResult result, Model model) {
        if (result.hasErrors()) {
            // BARDZO WAŻNE: Skoro wracamy do formularza, musimy znowu załadować 
            // listę autorów do dropdowna, inaczej formularz się wywali (błąd Thymeleaf)
            model.addAttribute("authors", authorRepository.findAll());
            // Zwracamy nazwę widoku HTML (nie redirect!), żeby pokazać błędy
            return "admin/book-form";
        }
        bookService.saveBook(book);
        return "redirect:/admin/books";
    }

    @GetMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return "redirect:/admin/books";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @GetMapping("/users/toggle-block/{id}")
    public String toggleUserBlock(@PathVariable Long id) {
        userService.toggleUserBlock(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/reviews")
    public String manageReviews(Model model) {
        model.addAttribute("reviews", reviewService.getAllReviews());
        return "admin/reviews";
    }

    @GetMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return "redirect:/admin/reviews";
    }

    @GetMapping("/books/export")
    public ResponseEntity<byte[]> exportBooks() {
        byte[] csvData = bookService.exportBooksToCsv();
        
        String fileName = "ksiazki_export_" + System.currentTimeMillis() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("text/csv")) // lub application/csv
                .body(csvData);
    }

}
