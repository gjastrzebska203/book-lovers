package com.booklovers.community.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.booklovers.community.dto.BookDto;
import com.booklovers.community.dto.UserRegisterDto;
import com.booklovers.community.model.Author;
import com.booklovers.community.model.Book;
import com.booklovers.community.model.Shelf;
import com.booklovers.community.model.User;
import com.booklovers.community.repository.AuthorRepository;
import com.booklovers.community.service.AuthorService;
import com.booklovers.community.service.BookService;
import com.booklovers.community.service.ReviewService;
import com.booklovers.community.service.ShelfService;
import com.booklovers.community.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WebController {
    private final BookService bookService;
    private final UserService userService;
    private final ShelfService shelfService;
    private final ReviewService reviewService;
    private final AuthorService authorService;
    private final AuthorRepository authorRepository;

    // strona główna
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("popularBooks", bookService.getMostPopularBooks());
        return "index";
    }

    // formularz rejestracji
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserRegisterDto());
        return "register";
    }

    // obsługa rejestracji
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegisterDto userDto, 
                               BindingResult result, 
                               Model model) {
        if (result.hasErrors()) {
            return "register"; // Zwraca HTML z błędami
        }
        try {
            userService.registerUser(userDto); // Woła ten sam serwis co API!
            return "redirect:/login?success";  // Przekierowuje
        } catch (RuntimeException e) {
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
            @RequestParam(defaultValue = "") String query, 
            @RequestParam(defaultValue = "0") int page,   
            @RequestParam(defaultValue = "10") int size,  
            Model model) {

        PageRequest pageable = PageRequest.of(page, size);
        
        Page<?> bookPage;

        if (query == null || query.isBlank()) {
            bookPage = bookService.getAllBooks(pageable);
        } else {
            bookPage = bookService.searchBooks(query, pageable);
        }

        model.addAttribute("books", bookPage);
        model.addAttribute("query", query); 

        return "books";
    }

    // szczegóły książki
    @GetMapping("/books/{id}")
    public String bookDetails(@PathVariable Long id, Model model, java.security.Principal principal) {
        BookDto book = bookService.getBookById(id);
        model.addAttribute("book", book);

        model.addAttribute("reviews", reviewService.getReviewsForBook(id));
        model.addAttribute("stats", bookService.getBookStatistics(id));

        if (principal != null) {
            model.addAttribute("userShelves", shelfService.getUserShelves(principal.getName()));
        }

        return "book-details";
    }

    // dodawanie recenzji
    @PostMapping("/books/{id}/review")
    public String addReview(@PathVariable Long id, 
                            @RequestParam Integer rating, 
                            @RequestParam String content,
                            java.security.Principal principal) {
        reviewService.addReview(id, principal.getName(), rating, content);
        return "redirect:/books/" + id;
    }

    // dodawanie do półki
    @PostMapping("/books/{id}/add-to-shelf")
    public String addToShelf(@PathVariable Long id, 
                             @RequestParam Long shelfId,
                             java.security.Principal principal) {
        shelfService.addBookToShelf(shelfId, id, principal.getName());
        return "redirect:/books/" + id + "?added";
    }

    // wyświetlanie profilu
    @GetMapping("/profile")
    public String profile(Model model, java.security.Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        List<Shelf> shelves = shelfService.getUserShelves(username);
        
        int readCount = shelfService.getBooksReadCount(username);
        int challengeTarget = 52;
        
        int progressPercent = (challengeTarget > 0) ? (int)((double)readCount / challengeTarget * 100) : 0;
        if (progressPercent > 100) progressPercent = 100;

        model.addAttribute("user", user);
        model.addAttribute("shelves", shelves);
        
        model.addAttribute("booksReadYear", readCount);
        model.addAttribute("challengeTarget", challengeTarget);
        model.addAttribute("challengeProgress", progressPercent);
        model.addAttribute("currentYear", java.time.Year.now().getValue());

        return "profile";
    }

    // edycja profilu (post)
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String bio,
                                @RequestParam("avatar") MultipartFile avatar,
                                java.security.Principal principal) {
        
        userService.updateUserProfile(principal.getName(), bio, avatar);
        return "redirect:/profile?updated";
    }

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

    @PostMapping("/profile/shelves/create")
    public String createShelf(@RequestParam String shelfName, 
                              java.security.Principal principal,
                              Model model) {
        try {
            shelfService.createShelf(shelfName, principal.getName());
            return "redirect:/profile?shelfCreated";
        } catch (RuntimeException e) {
            return "redirect:/profile?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/profile/shelves/{shelfId}/remove")
    public String removeBookFromShelf(@PathVariable Long shelfId, 
                                      @RequestParam Long bookId,
                                      java.security.Principal principal) {
        shelfService.removeBookFromShelf(shelfId, bookId, principal.getName());
        return "redirect:/profile";
    }

    @PostMapping("/profile/shelves/{shelfId}/move")
    public String moveBook(@PathVariable Long shelfId, 
                           @RequestParam Long bookId,
                           @RequestParam Long targetShelfId,
                           java.security.Principal principal) {
        shelfService.moveBook(shelfId, targetShelfId, bookId, principal.getName());
        return "redirect:/profile";
    }

    @PostMapping("/profile/import")
    public String importProfile(@RequestParam("file") MultipartFile file,
                                java.security.Principal principal) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Nie wybrano pliku.");
            }
            userService.importProfile(principal.getName(), file);
            return "redirect:/profile?imported=true";
        } catch (Exception e) {
            return "redirect:/profile?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/admin/authors")
    public String listAuthors(Model model) {
        model.addAttribute("authors", authorService.getAllAuthors());
        return "admin/authors";
    }

    @GetMapping("/admin/authors/new")
    public String showAddForm(Model model) {
        model.addAttribute("author", new Author());
        return "admin/author-form";
    }

    // formularz edycji
    @GetMapping("/admin/authors/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("author", authorService.getAuthorById(id));
        return "admin/author-form";
    }

    // zapis (Dodawanie i Edycja)
    @PostMapping("/admin/authors/save")
    public String saveAuthor(@ModelAttribute Author author) {
        authorService.saveAuthor(author);
        return "redirect:/admin/authors?success";
    }

    // usuwanie
    @PostMapping("/admin/authors/delete/{id}")
    public String deleteAuthor(@PathVariable Long id) {
        try {
            authorService.deleteAuthor(id);
            return "redirect:/admin/authors?deleted";
        } catch (RuntimeException e) {
            return "redirect:/admin/authors?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

        // dashboard admina
    @GetMapping("/admin")
    public String dashboard() {
        return "admin/dashboard";
    }

    // lista książek - widok admina
    @GetMapping("/admin/books")
    public String manageBooks(Model model, Pageable pageable) {
        model.addAttribute("books", bookService.getAllBooks(pageable));
        return "admin/books";
    }

    // dodawanie książki
    @GetMapping("/admin/books/new")
    public String showCreateBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("authors", authorRepository.findAll());
        return "admin/book-form";
    }

    // edycja książki
    @GetMapping("/admin/books/edit/{id}")
    public String showUpdateBookForm(@PathVariable Long id, Model model) {
        Book book = bookService.findEntityById(id); 
        model.addAttribute("book", book);
        model.addAttribute("authors", authorRepository.findAll());
        return "admin/book-form"; 
    }

    // zapisanie książki
    @PostMapping("/admin/books/save")
    public String saveBook(@Valid @ModelAttribute Book book, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("authors", authorRepository.findAll());
            return "admin/book-form";
        }
        bookService.saveBook(book);
        return "redirect:/admin/books";
    }

    // usuwanie książki
    @GetMapping("/admin/books/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return "redirect:/admin/books";
    }

    // zarządzanie użytkownikami
    @GetMapping("/admin/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    // blokowanie/odblokowanie użytkowników
    @GetMapping("/admin/users/toggle-block/{id}")
    public String toggleUserBlock(@PathVariable Long id) {
        userService.toggleUserBlock(id);
        return "redirect:/admin/users";
    }

    // recenzje
    @GetMapping("/admin/reviews")
    public String manageReviews(Model model) {
        model.addAttribute("reviews", reviewService.getAllReviews());
        return "admin/reviews";
    }

    // usuwanie recenzji
    @GetMapping("/admin/reviews/delete/{id}")
    public String deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return "redirect:/admin/reviews";
    }

    // export listy książek do CSV
    @GetMapping("/admin/books/export")
    public ResponseEntity<byte[]> exportBooks() {
        byte[] csvData = bookService.exportBooksToCsv();
        String fileName = "ksiazki_export_" + System.currentTimeMillis() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    // usuwanie użytkownika
    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserByAdmin(id);
        return "redirect:/admin/users?deleted";
    }
}