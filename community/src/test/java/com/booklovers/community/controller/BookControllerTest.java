// package com.booklovers.community.controller;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

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
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.web.servlet.MockMvc;

// import com.booklovers.community.dto.BookDto;
// import com.booklovers.community.dto.RatingStatDto;
// import com.booklovers.community.model.Book;
// import com.booklovers.community.security.SecurityConfig;
// import com.booklovers.community.service.BookService;
// import com.fasterxml.jackson.databind.ObjectMapper;

// import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(BookController.class)
// @Import(SecurityConfig.class)
// public class BookControllerTest {
//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @MockBean
//     private BookService bookService;

//     // pobranie listy książek (Dostęp publiczny)
//     @Test
//     @WithMockUser // Symulujemy dowolnego użytkownika (lub brak, zależnie od configu, tutaj user wystarczy)
//     void shouldReturnListOfBooks() throws Exception {
//         // given
//         BookDto bookDto = BookDto.builder().id(1L).title("Wiedźmin").authorName("Sapkowski").build();
//         Page<BookDto> page = new PageImpl<>(List.of(bookDto));

//         when(bookService.getAllBooks(any(Pageable.class))).thenReturn(page);

//         // when & then
//         mockMvc.perform(get("/api/v1/books")
//                         .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isOk()) // Oczekujemy HTTP 200
//                 .andExpect(jsonPath("$.content.length()").value(1)) // Czy jest 1 element
//                 .andExpect(jsonPath("$.content[0].title").value("Wiedźmin"));
//     }

//     // pobranie szczegółów książki po ID (Dostęp publiczny)
//     @Test
//     @WithMockUser
//     void shouldReturnBookById() throws Exception {
//         // given
//         Long bookId = 1L;
//         BookDto bookDto = BookDto.builder().id(bookId).title("Hobbit").isbn("12345").build();

//         when(bookService.getBookById(bookId)).thenReturn(bookDto);

//         // when & then
//         mockMvc.perform(get("/api/v1/books/{id}", bookId))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.title").value("Hobbit"))
//                 .andExpect(jsonPath("$.isbn").value("12345"));
//     }

//     // dodanie nowej książki (Tylko ADMIN - Sukces)
//     @Test
//     @WithMockUser(username = "admin", roles = "ADMIN") // Symulujemy Admina
//     void shouldCreateBookAsAdmin() throws Exception {
//         // given
//         Book newBook = Book.builder().title("Nowa Książka").isbn("999").build();
//         String jsonRequest = objectMapper.writeValueAsString(newBook);

//         // when & then
//         mockMvc.perform(post("/api/v1/books")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(jsonRequest)
//                         .with(csrf())) // CSRF jest wymagany przy metodach zmieniających stan (POST, PUT, DELETE)
//                 .andExpect(status().isCreated()) // Oczekujemy HTTP 201
//                 .andExpect(jsonPath("$.title").value("Nowa Książka"));

//         verify(bookService).saveBook(any(Book.class));
//     }

//     // blokada dodawania książki (Zwykły USER - Forbidden)
//     @Test
//     @WithMockUser(username = "user", roles = "USER") // Symulujemy zwykłego usera
//     void shouldForbidCreateBookForRegularUser() throws Exception {
//         // given
//         Book newBook = Book.builder().title("Hacker Book").build();
//         String jsonRequest = objectMapper.writeValueAsString(newBook);

//         // when & then
//         mockMvc.perform(post("/api/v1/books")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(jsonRequest)
//                         .with(csrf()))
//                 .andExpect(status().isForbidden()); // Oczekujemy HTTP 403 Forbidden
//     }

//     // usuwanie książki (Tylko ADMIN - Sukces)
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldDeleteBookAsAdmin() throws Exception {
//         // given
//         Long bookId = 10L;

//         // when & then
//         mockMvc.perform(delete("/api/v1/books/{id}", bookId)
//                         .with(csrf()))
//                 .andExpect(status().isNoContent()); // Oczekujemy HTTP 204 No Content

//         verify(bookService).deleteBook(bookId);
//     }

//     // pobieranie statystyk (JdbcTemplate)
//     @Test
//     @WithMockUser
//     void shouldReturnBookStats() throws Exception {
//         // given
//         Long bookId = 1L;
//         RatingStatDto stat = new RatingStatDto(5, 10L); // Ocena 5, 10 głosów
//         when(bookService.getBookRatingStats(bookId)).thenReturn(List.of(stat));

//         // when & then
//         mockMvc.perform(get("/api/v1/books/{id}/stats", bookId))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$[0].rating").value(5))
//                 .andExpect(jsonPath("$[0].count").value(10));
//     }

//     // wyszukiwanie książek (searchBooks)
//     @Test
//     @WithMockUser
//     void shouldSearchBooks() throws Exception {
//         // given
//         String query = "Wiedźmin";
//         BookDto bookDto = BookDto.builder().id(1L).title("Wiedźmin").build();
//         Page<BookDto> page = new PageImpl<>(List.of(bookDto));

//         when(bookService.searchBooks(any(String.class), any(Pageable.class))).thenReturn(page);

//         // when & then
//         mockMvc.perform(get("/api/v1/books/search")
//                         .param("query", query)
//                         .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.content[0].title").value("Wiedźmin"));
//     }

//     // aktualizacja książki (Tylko ADMIN - Sukces)
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldUpdateBookAsAdmin() throws Exception {
//         // given
//         Long bookId = 1L;
//         Book updateInfo = Book.builder().title("Zaktualizowany Tytuł").isbn("111").build();
//         String jsonRequest = objectMapper.writeValueAsString(updateInfo);

//         // when & then
//         mockMvc.perform(put("/api/v1/books/{id}", bookId)
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(jsonRequest)
//                         .with(csrf()))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.title").value("Zaktualizowany Tytuł"));

//         verify(bookService).saveBook(any(Book.class));
//     }

//     // blokada aktualizacji książki (Zwykły USER - Forbidden)
//     @Test
//     @WithMockUser(roles = "USER")
//     void shouldForbidUpdateBookForRegularUser() throws Exception {
//         // given
//         Long bookId = 1L;
//         Book updateInfo = Book.builder().title("Hacker Update").build();
//         String jsonRequest = objectMapper.writeValueAsString(updateInfo);

//         // when & then
//         mockMvc.perform(put("/api/v1/books/{id}", bookId)
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(jsonRequest)
//                         .with(csrf()))
//                 .andExpect(status().isForbidden());
//     }

//     // blokada usuwania książki (Zwykły USER - Forbidden)
//     @Test
//     @WithMockUser(roles = "USER")
//     void shouldForbidDeleteBookForRegularUser() throws Exception {
//         // given
//         Long bookId = 1L;

//         // when & then
//         mockMvc.perform(delete("/api/v1/books/{id}", bookId)
//                         .with(csrf()))
//                 .andExpect(status().isForbidden());
//     }
// }
