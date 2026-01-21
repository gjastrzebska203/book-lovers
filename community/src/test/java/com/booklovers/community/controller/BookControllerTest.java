package com.booklovers.community.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.booklovers.community.dto.BookDto;
import com.booklovers.community.dto.RatingStatDto;
import com.booklovers.community.model.Author;
import com.booklovers.community.model.Book;
import com.booklovers.community.repository.BookRepository;
import com.booklovers.community.security.SecurityConfig;
import com.booklovers.community.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private BookRepository bookRepository;

    // pobieranie wszystkich książek
    @Test
    @WithMockUser
    void shouldReturnAllBooks() throws Exception {
        // given
        BookDto dto = BookDto.builder().id(1L).title("Test Book").build();
        Page<BookDto> page = new PageImpl<>(List.of(dto));
        
        when(bookService.getAllBooks(any(Pageable.class))).thenReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Book"));
    }

    // wyszukiwanie (Search)
    @Test
    @WithMockUser
    void shouldSearchBooks() throws Exception {
        // given
        String query = "Fantasy";
        Page<BookDto> page = new PageImpl<>(Collections.emptyList());
        when(bookService.searchBooks(any(String.class), any(Pageable.class))).thenReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/books/search")
                        .param("query", query))
                .andExpect(status().isOk());
    }

    // pobieranie po ID
    @Test
    @WithMockUser
    void shouldGetBookById() throws Exception {
        // given
        Long id = 1L;
        BookDto dto = BookDto.builder().id(id).title("Details").build();
        when(bookService.getBookById(id)).thenReturn(dto);

        // when & then
        mockMvc.perform(get("/api/v1/books/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Details"));
    }

    // statystyki - SUKCES (są dane)
    @Test
    @WithMockUser
    void shouldReturnStatsWhenAvailable() throws Exception {
        // given
        Long id = 1L;
        RatingStatDto stat = new RatingStatDto(5, 10L);
        when(bookService.getBookRatingStats(id)).thenReturn(List.of(stat));

        // when & then
        mockMvc.perform(get("/api/v1/books/{id}/stats", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    // statystyki - BŁĄD 404 (brak danych/null)
    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenStatsAreEmpty() throws Exception {
        // given
        Long id = 1L;
        when(bookService.getBookRatingStats(id)).thenReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/v1/books/{id}/stats", id))
                .andExpect(status().isNotFound());
    }

    // tworzenie książki (Tylko ADMIN)
@Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateBook() throws Exception {
        // given
        Author author = Author.builder().id(1L).firstName("Test").lastName("Author").build();

        Book book = Book.builder()
                .title("New Book")
                .isbn("9780261102217")
                .author(author)     
                .build();
        
        String json = objectMapper.writeValueAsString(book);

        // when & then
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Book"));

        verify(bookService).saveBook(any(Book.class));
    }

    // aktualizacja - SUKCES (Książka istnieje)
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateBookWhenExists() throws Exception {
        // given
        Long id = 1L;
        Author author = Author.builder().id(1L).firstName("Update").lastName("Test").build();

        Book book = Book.builder()
                .title("Updated Title")
                .isbn("9780261102217") 
                .author(author)    
                .build();
        
        String json = objectMapper.writeValueAsString(book);
        when(bookRepository.existsById(id)).thenReturn(true);

        // when & then
        mockMvc.perform(put("/api/v1/books/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print()) 
                .andExpect(jsonPath("$.title").value("Updated Title"));

        verify(bookService).saveBook(any(Book.class));
    }

    // aktualizacja - BŁĄD 404 (książka nie istnieje)
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenUpdatingNonExistentBook() throws Exception {
        // given
        Long id = 99L;
        Author author = Author.builder().id(1L).firstName("Ghost").lastName("Writer").build();
        
        Book book = Book.builder()
                .title("Ghost Book")
                .isbn("9780261102217") 
                .author(author)    
                .build();
                
        String json = objectMapper.writeValueAsString(book);
        when(bookRepository.existsById(id)).thenReturn(false);

        // when & then
        mockMvc.perform(put("/api/v1/books/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(bookService, never()).saveBook(any(Book.class));
    }

    // usuwanie - SUKCES
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteBookWhenExists() throws Exception {
        // given
        Long id = 1L;
        when(bookRepository.existsById(id)).thenReturn(true);

        // when & then
        mockMvc.perform(delete("/api/v1/books/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent()); // 204

        verify(bookService).deleteBook(id);
    }

    // usuwanie - BŁĄD 404
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenDeletingNonExistentBook() throws Exception {
        // given
        Long id = 99L;
        when(bookRepository.existsById(id)).thenReturn(false);

        // when & then
        mockMvc.perform(delete("/api/v1/books/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(bookService, never()).deleteBook(anyLong());
    }
}