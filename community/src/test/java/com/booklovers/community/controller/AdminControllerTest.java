// package com.booklovers.community.controller;

// import static org.hamcrest.Matchers.hasSize;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
// import org.springframework.data.domain.Pageable;
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.web.servlet.MockMvc;

// import com.booklovers.community.dto.BookDto;
// import com.booklovers.community.model.Author;
// import com.booklovers.community.model.Book;
// import com.booklovers.community.model.User;
// import com.booklovers.community.repository.AuthorRepository;
// import com.booklovers.community.security.SecurityConfig;
// import com.booklovers.community.service.BookService;
// import com.booklovers.community.service.ReviewService;
// import com.booklovers.community.service.UserService;

// @WebMvcTest(AdminController.class)
// @Import(SecurityConfig.class)
// public class AdminControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private UserService userService;

//     @MockBean
//     private BookService bookService;

//     @MockBean
//     private ReviewService reviewService;

//     @MockBean
//     private AuthorRepository authorRepository;

//     // 1. Dashboard
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldShowDashboard() throws Exception {
//         mockMvc.perform(get("/admin"))
//                 .andExpect(status().isOk())
//                 .andExpect(view().name("admin/dashboard"));
//     }

//     // 2. Zarządzanie książkami (Lista)
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldShowManageBooksPage() throws Exception {
//         // given
//         BookDto bookDto = BookDto.builder().id(1L).title("Test Book").build();
//         Page<BookDto> page = new PageImpl<>(List.of(bookDto));
//         when(bookService.getAllBooks(any(Pageable.class))).thenReturn(page);

//         // when & then
//         mockMvc.perform(get("/admin/books"))
//                 .andExpect(status().isOk())
//                 .andExpect(view().name("admin/books"))
//                 .andExpect(model().attributeExists("books"));
//     }

//     // 3. Formularz nowej książki
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldShowCreateBookForm() throws Exception {
//         // given
//         when(authorRepository.findAll()).thenReturn(Collections.emptyList());

//         // when & then
//         mockMvc.perform(get("/admin/books/new"))
//                 .andExpect(status().isOk())
//                 .andExpect(view().name("admin/book-form"))
//                 .andExpect(model().attributeExists("book"))
//                 .andExpect(model().attributeExists("authors"));
//     }

//     // 4. Formularz edycji książki
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldShowUpdateBookForm() throws Exception {
//         // given
//         Long bookId = 1L;
//         Book book = new Book();
//         book.setId(bookId);
//         when(bookService.findEntityById(bookId)).thenReturn(book);
//         when(authorRepository.findAll()).thenReturn(List.of(new Author()));

//         // when & then
//         mockMvc.perform(get("/admin/books/edit/{id}", bookId))
//                 .andExpect(status().isOk())
//                 .andExpect(view().name("admin/book-form"))
//                 .andExpect(model().attribute("book", book))
//                 .andExpect(model().attributeExists("authors"));
//     }

//     // 5. Zapisywanie książki (Sukces - przekierowanie)
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldSaveBookAndRedirect() throws Exception {
//         // given
//         // Przekazujemy parametry formularza symulując poprawne dane
//         // Zakładamy, że Book ma walidację (np. @NotBlank title), więc podajemy tytuł
        
//         // when & then
//         mockMvc.perform(post("/admin/books/save")
//                         .param("title", "Valid Title") 
//                         .param("isbn", "1234567890")
//                         .with(csrf())) // CSRF wymagany dla POST
//                 .andExpect(status().is3xxRedirection())
//                 .andExpect(redirectedUrl("/admin/books"));

//         verify(bookService).saveBook(any(Book.class));
//     }

//     // 6. Zapisywanie książki (Błąd walidacji - powrót do formularza)
//     // UWAGA: Ten test zadziała tylko jeśli klasa Book ma adnotacje walidacyjne (np. @NotBlank na tytule)
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldReturnToFormWhenValidationFails() throws Exception {
//         // given
//         // Przesyłamy pusty tytuł
        
//         // when & then
//         mockMvc.perform(post("/admin/books/save")
//                         .param("title", "") // Pusty tytuł -> błąd walidacji
//                         .with(csrf()))
//                 .andExpect(status().isOk()) // Nie ma przekierowania, zostajemy na stronie (200 OK)
//                 .andExpect(view().name("admin/book-form"))
//                 .andExpect(model().attributeHasFieldErrors("book", "title")); 
//     }

//     // 7. Usuwanie książki
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldDeleteBook() throws Exception {
//         mockMvc.perform(get("/admin/books/delete/{id}", 1L))
//                 .andExpect(status().is3xxRedirection())
//                 .andExpect(redirectedUrl("/admin/books"));

//         verify(bookService).deleteBook(1L);
//     }

//     // 8. Eksport do CSV
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldExportBooksToCsv() throws Exception {
//         // given
//         byte[] csvContent = "ID;Title\n1;Test".getBytes();
//         when(bookService.exportBooksToCsv()).thenReturn(csvContent);

//         // when & then
//         mockMvc.perform(get("/admin/books/export"))
//                 .andExpect(status().isOk())
//                 .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=\"ksiazki_export_")))
//                 .andExpect(content().contentType("text/csv"))
//                 .andExpect(content().bytes(csvContent));
//     }

//     // 9. Zarządzanie użytkownikami (Lista)
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldShowManageUsersPage() throws Exception {
//         // given
//         when(userService.findAll()).thenReturn(List.of(new User(), new User()));

//         // when & then
//         mockMvc.perform(get("/admin/users"))
//                 .andExpect(status().isOk())
//                 .andExpect(view().name("admin/users"))
//                 .andExpect(model().attribute("users", hasSize(2)));
//     }

//     // 10. Blokowanie użytkownika
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldToggleUserBlock() throws Exception {
//         mockMvc.perform(get("/admin/users/toggle-block/{id}", 10L))
//                 .andExpect(status().is3xxRedirection())
//                 .andExpect(redirectedUrl("/admin/users"));

//         verify(userService).toggleUserBlock(10L);
//     }

//     // 11. Usuwanie użytkownika (POST)
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldDeleteUser() throws Exception {
//         mockMvc.perform(post("/admin/users/{id}/delete", 5L)
//                         .with(csrf()))
//                 .andExpect(status().is3xxRedirection())
//                 .andExpect(redirectedUrl("/admin/users?deleted"));

//         verify(userService).deleteUserByAdmin(5L);
//     }

//     // 12. Zarządzanie recenzjami
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldShowManageReviewsPage() throws Exception {
//         // given
//         when(reviewService.getAllReviews()).thenReturn(List.of(new Review()));

//         // when & then
//         mockMvc.perform(get("/admin/reviews"))
//                 .andExpect(status().isOk())
//                 .andExpect(view().name("admin/reviews"))
//                 .andExpect(model().attributeExists("reviews"));
//     }

//     // 13. Usuwanie recenzji
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldDeleteReview() throws Exception {
//         mockMvc.perform(get("/admin/reviews/delete/{id}", 99L))
//                 .andExpect(status().is3xxRedirection())
//                 .andExpect(redirectedUrl("/admin/reviews"));

//         verify(reviewService).deleteReview(99L);
//     }
    
//     // Test zabezpieczeń: Próba wejścia bez roli ADMIN
//     @Test
//     @WithMockUser(roles = "USER") // Zwykły user
//     void shouldForbidAccessForRegularUser() throws Exception {
//         mockMvc.perform(get("/admin"))
//                 .andExpect(status().isForbidden());
//     }
// }