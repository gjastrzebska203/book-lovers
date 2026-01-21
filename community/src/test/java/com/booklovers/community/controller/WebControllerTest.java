// package com.booklovers.community.controller;

// import static org.hamcrest.Matchers.containsString;
// import static org.hamcrest.Matchers.hasSize;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.doThrow;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// import java.util.Collections;
// import java.util.List;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.context.annotation.Import;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.Pageable;
// import org.springframework.http.MediaType;
// import org.springframework.mock.web.MockMultipartFile;
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.web.servlet.MockMvc;

// import com.booklovers.community.dto.BookDto;
// import com.booklovers.community.dto.BookStatsDto;
// import com.booklovers.community.dto.UserRegisterDto;
// import com.booklovers.community.model.Shelf;
// import com.booklovers.community.model.User;
// import com.booklovers.community.security.SecurityConfig;
// import com.booklovers.community.service.BookService;
// import com.booklovers.community.service.ReviewService;
// import com.booklovers.community.service.ShelfService;
// import com.booklovers.community.service.UserService;

// @WebMvcTest(WebController.class)
// @Import(SecurityConfig.class)
// public class WebControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private BookService bookService;

//     @MockBean
//     private UserService userService;

//     @MockBean
//     private ShelfService shelfService;

//     @MockBean
//     private ReviewService reviewService;

//     // 1. Strona główna
//     @Test
//     @WithMockUser
//     void shouldShowHomePage() throws Exception {
//         when(bookService.getMostPopularBooks()).thenReturn(Collections.emptyList());

//         mockMvc.perform(get("/"))
//                 .andExpect(status().isOk())
//                 .andExpect(view().name("index"))
//                 .andExpect(model().attributeExists("popularBooks"));
//     }

//     // 2. Formularz rejestracji (GET)
//     @Test
//     void shouldShowRegisterForm() throws Exception {
//         mockMvc.perform(get("/register"))
//                 .andExpect(status().isOk())
//                 .andExpect(view().name("register"))
//                 .andExpect(model().attributeExists("user"));
//     }

//     // 3. Rejestracja (POST) - Sukces
//     @Test
//     void shouldRegisterUserAndRedirect() throws Exception {
//         mockMvc.perform(post("/register")
//                         .param("username", "newUser")
//                         .param("email", "new@test.pl")
//                         .param("password", "StrongPass1!")
//                         .with(csrf()))
//                 .andExpect(status().is3xxRedirection())
//                 .andExpect(redirectedUrl("/login?success"));

//         verify(userService).registerUser(any(UserRegisterDto.class));
//     }

//     // 4. Rejestracja (POST) - Błąd Walidacji
//     @Test
//     void shouldReturnRegisterFormOnValidationError() throws Exception {
//         mockMvc.perform(post("/register")
//                         .param("username", "") // Puste - błąd
//                         .with(csrf()))
//                 .andExpect(status().isOk())
//                 .andExpect(view().name("register"))
//                 .andExpect(model().attributeHasErrors("user"));
//     }

//     // 5. Lista książek z wyszukiwaniem
//     @Test
//     @WithMockUser
//     void shouldSearchBooks() throws Exception {
//         Page<BookDto> page = new PageImpl<>(List.of(new BookDto()));
//         when(bookService.searchBooks(anyString(), any(Pageable.class))).thenReturn(page);

//         mockMvc.perform(get("/books").param("query", "Wiedźmin"))
//                 .andExpect(status().isOk())
//                 .andExpect(view().name("books"))
//                 .andExpect(model().attributeExists("books"))
//                 .andExpect(model().attribute("query", "Wiedźmin"));
//     }

//     // 6. Szczegóły książki
//     @Test
//     @WithMockUser(username = "reader")
//     void shouldShowBookDetails() throws Exception {
//         Long bookId = 1L;
//         BookDto bookDto = new BookDto();
//         BookStatsDto statsDto = BookStatsDto.builder().build();

//         when(bookService.getBookById(bookId)).thenReturn(bookDto);
//         when(bookService.getBookStatistics(bookId)).thenReturn(statsDto);
//         when(reviewService.getReviewsForBook(bookId)).thenReturn(Collections.emptyList());
//         when(shelfService.getUserShelves("reader")).thenReturn(Collections.emptyList());

//         mockMvc.perform(get("/books/{id}", bookId))
//                 .andExpect(status().isOk())
//                 .andExpect(view().name("book-details"))
//                 .andExpect(model().attributeExists("book"))
//                 .andExpect(model().attributeExists("stats"))
//                 .andExpect(model().attributeExists("userShelves"));
//     }

//     // 7. Dodawanie recenzji
//     @Test
//     @WithMockUser(username = "reader")
//     void shouldAddReview() throws Exception {
//         mockMvc.perform(post("/books/1/review")
//                         .param("rating", "5")
//                         .param("content", "Super")
//                         .with(csrf()))
//                 .andExpect(status().is3xxRedirection())
//                 .andExpect(redirectedUrl("/books/1"));

//         verify(reviewService).addReview(1L, "reader", 5, "Super");
//     }

//     // 8. Dodawanie do półki
//     @Test
//     @WithMockUser(username = "reader")
//     void shouldAddToShelf() throws Exception {
//         mockMvc.perform(post("/books/1/add-to-shelf")
//                         .param("shelfId", "10")
//                         .with(csrf()))
//                 .andExpect(status().is3xxRedirection())
//                 .andExpect(redirectedUrl("/books/1?added"));

//         verify(shelfService).addBookToShelf(10L, 1L, "reader");
//     }

//     // 9. Profil użytkownika (z Reading Challenge)
//     @Test
//     @WithMockUser(username = "reader")
//     void shouldShowProfileWithChallengeStats() throws Exception {
//         User user = new User();
//         user.setUsername("reader");
        
//         when(userService.findByUsername("reader")).thenReturn(user);
//         when(shelfService.getUserShelves("reader")).thenReturn(Collections.emptyList());
//         when(shelfService.getBooksReadCount("reader")).thenReturn(13); // Przeczytał 13 książek

//         mockMvc.perform(get("/profile"))
//                 .andExpect(status().isOk())
//                 .andExpect(view().name("profile"))
//                 .andExpect(model().attribute("booksReadYear", 13))
//                 .andExpect(model().attribute("challengeTarget", 52))
//                 .andExpect(model().attribute("challengeProgress", 25)); // 13/52 = 25%
//     }

//     // 10. Eksport profilu (Pobieranie pliku)
//     @Test
//     @WithMockUser(username = "reader")
//     void shouldExportProfile() throws Exception {
//         byte[] jsonContent = "{\"username\":\"reader\"}".getBytes();
//         when(userService.generateProfileBackup("reader")).thenReturn(jsonContent);

//         mockMvc.perform(get("/profile/export"))
//                 .andExpect(status().isOk())
//                 .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"backup_reader.json\"")))
//                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(content().bytes(jsonContent));
//     }

//     // 11. Import profilu (Upload pliku) - NOWOŚĆ
//     @Test
//     @WithMockUser(username = "reader")
//     void shouldImportProfile() throws Exception {
//         MockMultipartFile file = new MockMultipartFile(
//                 "file", 
//                 "backup.json", 
//                 "application/json", 
//                 "{\"shelves\":[]}".getBytes()
//         );

//         mockMvc.perform(multipart("/profile/import")
//                         .file(file)
//                         .with(csrf()))
//                 .andExpect(status().is3xxRedirection())
//                 .andExpect(redirectedUrl("/profile?imported=true"));

//         verify(userService).importProfile(eq("reader"), any());
//     }
    
//     // 12. Import profilu - Błąd (pusty plik)
//     @Test
//     @WithMockUser(username = "reader")
//     void shouldHandleImportError() throws Exception {
//         // Symulujemy, że serwis rzuci wyjątek
//         MockMultipartFile file = new MockMultipartFile("file", "test.json", "json", "data".getBytes());
//         doThrow(new RuntimeException("Błąd parsowania")).when(userService).importProfile(anyString(), any());

//         mockMvc.perform(multipart("/profile/import")
//                         .file(file)
//                         .with(csrf()))
//                 .andExpect(status().is3xxRedirection())
//                 // Sprawdzamy czy przekierowanie zawiera parametr error (zakodowany w URL)
//                 .andExpect(redirectedUrlPattern("/profile?error=*")); 
//     }

//     // 13. Tworzenie półki
//     @Test
//     @WithMockUser(username = "reader")
//     void shouldCreateShelf() throws Exception {
//         mockMvc.perform(post("/profile/shelves/create")
//                         .param("shelfName", "Nowa Półka")
//                         .with(csrf()))
//                 .andExpect(status().is3xxRedirection())
//                 .andExpect(redirectedUrl("/profile?shelfCreated"));

//         verify(shelfService).createShelf("Nowa Półka", "reader");
//     }

//     // 14. Przenoszenie książki
//     @Test
//     @WithMockUser(username = "reader")
//     void shouldMoveBook() throws Exception {
//         mockMvc.perform(post("/profile/shelves/1/move")
//                         .param("bookId", "10")
//                         .param("targetShelfId", "2")
//                         .with(csrf()))
//                 .andExpect(status().is3xxRedirection())
//                 .andExpect(redirectedUrl("/profile"));

//         verify(shelfService).moveBook(1L, 2L, 10L, "reader");
//     }
// }