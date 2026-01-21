package com.booklovers.community.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import com.booklovers.community.model.Shelf;
import com.booklovers.community.model.User;
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

    // strona główna
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("popularBooks", bookService.getMostPopularBooks());
        return "index";
    }

    // formularz rejestracji (GET)
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserRegisterDto());
        return "register";
    }

    // obsługa rejestracji (POST)
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegisterDto userDto, 
                               BindingResult result, 
                               Model model) {
        
        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(userDto);
            return "redirect:/login?success"; 
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
}
