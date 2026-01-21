package com.booklovers.community.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.booklovers.community.dto.BookDto;
import com.booklovers.community.dto.BookStatsDto;
import com.booklovers.community.dto.UserRegisterDto;
import com.booklovers.community.model.Author;
import com.booklovers.community.model.Book;
import com.booklovers.community.model.User;
import com.booklovers.community.repository.AuthorRepository;
import com.booklovers.community.security.SecurityConfig;
import com.booklovers.community.service.AuthorService;
import com.booklovers.community.service.BookService;
import com.booklovers.community.service.ReviewService;
import com.booklovers.community.service.ShelfService;
import com.booklovers.community.service.UserService;

@WebMvcTest(WebController.class)
@Import(SecurityConfig.class)
public class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private ShelfService shelfService;
    @MockitoBean
    private ReviewService reviewService;
    @MockitoBean
    private AuthorService authorService;
    @MockitoBean
    private AuthorRepository authorRepository;

    // Strona główna
    @Test
    @WithMockUser
    void shouldShowHomePage() throws Exception {
        when(bookService.getMostPopularBooks()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("popularBooks"));
    }

    // Formularz rejestracji GET
    @Test
    void shouldShowRegisterForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    // Rejestracja POST - Sukces
    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "user")
                        .param("email", "test@test.com")
                        .param("password", "Pass1234")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?success"));
    }

    // Rejestracja POST - Błąd walidacji (hasErrors)
    @Test
    void shouldReturnRegisterFormOnValidationError() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "") // Puste - błąd
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasErrors("user"));
    }

    // Rejestracja POST - Wyjątek z serwisu (RuntimeException)
    @Test
    void shouldHandleExceptionDuringRegistration() throws Exception {
        doThrow(new RuntimeException("Email zajęty")).when(userService).registerUser(any(UserRegisterDto.class));

        mockMvc.perform(post("/register")
                        .param("username", "user")
                        .param("email", "test@test.com")
                        .param("password", "Pass1234")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Email zajęty"));
    }

    // Strona logowania
    @Test
    void shouldShowLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    // Lista książek - Bez query (domyślny branch)
    @Test
    @WithMockUser
    void shouldListBooksWithoutQuery() throws Exception {
        when(bookService.getAllBooks(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("query", ""));
    }

    // Lista książek - Z query (branch else)
    @Test
    @WithMockUser
    void shouldSearchBooksWithQuery() throws Exception {
        when(bookService.searchBooks(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/books").param("query", "Wiedźmin"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("query", "Wiedźmin"));
    }

    // Szczegóły książki - Bez zalogowanego użytkownika (branch principal == null)
    @Test
    void shouldShowBookDetailsWithoutUser() throws Exception {
        // given
        when(bookService.getBookById(anyLong())).thenReturn(new BookDto());
        
        BookStatsDto emptyStats = BookStatsDto.builder()
                .ratingCount(0L) 
                .averageRating(0.0)
                .readerCount(0L)
                .ratingDistribution(new java.util.HashMap<>()) 
                .build();
        
        when(bookService.getBookStatistics(anyLong())).thenReturn(emptyStats);

        // when & then
        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("userShelves"));
    }

    // Szczegóły książki - Z zalogowanym użytkownikiem (branch principal != null)
    @Test
    @WithMockUser(username = "user")
    void shouldShowBookDetailsWithUserShelves() throws Exception {
        // given
        BookStatsDto safeStats = BookStatsDto.builder()
                .ratingCount(0L)      
                .averageRating(0.0)
                .ratingDistribution(new java.util.HashMap<>()) 
                .build();

        when(bookService.getBookById(anyLong())).thenReturn(new BookDto());
        when(bookService.getBookStatistics(anyLong())).thenReturn(safeStats);

        // when & then
        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userShelves"));
    }

    // Dodawanie recenzji
    @Test
    @WithMockUser(username = "user")
    void shouldAddReview() throws Exception {
        mockMvc.perform(post("/books/1/review")
                        .param("rating", "5")
                        .param("content", "Super")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"));
    }

    // Dodawanie do półki
    @Test
    @WithMockUser(username = "user")
    void shouldAddToShelf() throws Exception {
        mockMvc.perform(post("/books/1/add-to-shelf")
                        .param("shelfId", "10")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1?added"));
    }

    // Profil użytkownika
    @Test
    @WithMockUser(username = "user")
    void shouldShowProfile() throws Exception {
        when(userService.findByUsername("user")).thenReturn(new User());
        when(shelfService.getBooksReadCount("user")).thenReturn(5);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"));
    }

    // Aktualizacja profilu
    @Test
    @WithMockUser(username = "user")
    void shouldUpdateProfile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("avatar", "test.jpg", "image/jpeg", new byte[1]);

        mockMvc.perform(multipart("/profile/update")
                        .file(file)
                        .param("bio", "New Bio")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?updated"));
    }

    // Eksport profilu
    @Test
    @WithMockUser(username = "user")
    void shouldExportProfile() throws Exception {
        when(userService.generateProfileBackup("user")).thenReturn(new byte[0]);

        mockMvc.perform(get("/profile/export"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));
    }

    // Tworzenie półki - Sukces
    @Test
    @WithMockUser(username = "user")
    void shouldCreateShelfSuccessfully() throws Exception {
        mockMvc.perform(post("/profile/shelves/create")
                        .param("shelfName", "Nowa")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?shelfCreated"));
    }

    // Tworzenie półki - Wyjątek (catch block)
    @Test
    @WithMockUser(username = "user")
    void shouldHandleErrorCreatingShelf() throws Exception {
        doThrow(new RuntimeException("Błąd")).when(shelfService).createShelf(anyString(), anyString());

        mockMvc.perform(post("/profile/shelves/create")
                        .param("shelfName", "Nowa")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/profile?error=*"));
    }

    // Usuwanie z półki
    @Test
    @WithMockUser(username = "user")
    void shouldRemoveBookFromShelf() throws Exception {
        mockMvc.perform(post("/profile/shelves/1/remove")
                        .param("bookId", "5")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    // Przenoszenie książki
    @Test
    @WithMockUser(username = "user")
    void shouldMoveBookBetweenShelves() throws Exception {
        mockMvc.perform(post("/profile/shelves/1/move")
                        .param("bookId", "5")
                        .param("targetShelfId", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    // Import profilu - Sukces
    @Test
    @WithMockUser(username = "user")
    void shouldImportProfileSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "backup.json", "json", "{data}".getBytes());

        mockMvc.perform(multipart("/profile/import").file(file).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?imported=true"));
    }

    // Import profilu - Pusty plik (branch if file.isEmpty)
    @Test
    @WithMockUser(username = "user")
    void shouldHandleEmptyFileImport() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "", "json", new byte[0]);

        mockMvc.perform(multipart("/profile/import").file(file).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/profile?error=*"));
    }

    // Import profilu - Wyjątek (catch block)
    @Test
    @WithMockUser(username = "user")
    void shouldHandleImportException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "backup.json", "json", "{data}".getBytes());
        doThrow(new RuntimeException("Fail")).when(userService).importProfile(anyString(), any());

        mockMvc.perform(multipart("/profile/import").file(file).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/profile?error=*"));
    }

    // Admin - Lista autorów
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListAuthorsAdmin() throws Exception {
        mockMvc.perform(get("/admin/authors"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/authors"));
    }

    // Admin - Formularz dodawania autora
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowAuthorAddForm() throws Exception {
        mockMvc.perform(get("/admin/authors/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/author-form"));
    }

    // Admin - Formularz edycji autora
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowAuthorEditForm() throws Exception {
        when(authorService.getAuthorById(anyLong())).thenReturn(new Author());
        mockMvc.perform(get("/admin/authors/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/author-form"));
    }

    // Admin - Zapis autora
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSaveAuthor() throws Exception {
        mockMvc.perform(post("/admin/authors/save")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/authors?success"));
    }

    // Admin - Usuwanie autora (Sukces)
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteAuthorSuccessfully() throws Exception {
        mockMvc.perform(post("/admin/authors/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/authors?deleted"));
    }

    // Admin - Usuwanie autora (Wyjątek/catch block)
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldHandleAuthorDeleteException() throws Exception {
        doThrow(new RuntimeException("Błąd")).when(authorService).deleteAuthor(anyLong());

        mockMvc.perform(post("/admin/authors/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/admin/authors?error=*"));
    }

    // Admin - Dashboard
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    // Admin - Zarządzanie książkami
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowManageBooks() throws Exception {
        when(bookService.getAllBooks(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // when & then
        mockMvc.perform(get("/admin/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/books"))
                .andExpect(model().attributeExists("books"));
    }

    // Admin - Formularz nowej książki
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowCreateBookForm() throws Exception {
        mockMvc.perform(get("/admin/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book-form"));
    }

    // Admin - Formularz edycji książki
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowUpdateBookForm() throws Exception {
        when(bookService.findEntityById(anyLong())).thenReturn(new Book());
        mockMvc.perform(get("/admin/books/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book-form"));
    }

    // Admin - Zapis książki (Sukces)
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSaveBookSuccessfully() throws Exception {
        // given
        Author author = new Author();
        author.setId(1L);

        Book book = new Book();
        book.setTitle("Poprawny Tytuł");
        book.setIsbn("9780261102217"); 
        book.setAuthor(author);         

        // when & then
        mockMvc.perform(post("/admin/books/save")
                        .flashAttr("book", book) 
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/books"));
        
        verify(bookService).saveBook(any(Book.class));
    }

    // Admin - Zapis książki (Błąd walidacji - hasErrors)
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBookFormOnValidationErrors() throws Exception {
        mockMvc.perform(post("/admin/books/save")
                        .param("title", "") // Puste - błąd
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book-form"));
    }

    // Admin - Usuwanie książki
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteBook() throws Exception {
        mockMvc.perform(get("/admin/books/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/books"));
    }

    // Admin - Zarządzanie użytkownikami
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldManageUsers() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"));
    }

    // Admin - Blokowanie użytkownika
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldToggleUserBlock() throws Exception {
        mockMvc.perform(get("/admin/users/toggle-block/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    // Admin - Zarządzanie recenzjami
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldManageReviews() throws Exception {
        mockMvc.perform(get("/admin/reviews"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reviews"));
    }

    // Admin - Usuwanie recenzji
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteReview() throws Exception {
        mockMvc.perform(get("/admin/reviews/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reviews"));
    }

    // Admin - Eksport książek
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldExportBooks() throws Exception {
        when(bookService.exportBooksToCsv()).thenReturn(new byte[0]);
        mockMvc.perform(get("/admin/books/export"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));
    }

    // Admin - Usuwanie użytkownika
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(post("/admin/users/1/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?deleted"));
    }
}