package com.booklovers.community.controller;

import com.booklovers.community.model.Author;
import com.booklovers.community.service.AuthorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthorController.class)
@AutoConfigureMockMvc(addFilters = false) 
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorService authorService;

    @Autowired
    private ObjectMapper objectMapper;

    private Author author1;
    private Author author2;

    @BeforeEach
    void setUp() {
        author1 = new Author();
        author1.setId(1L);
        author1.setFirstName("Adam");
        author1.setLastName("Mickiewicz");
        author1.setBio("Wieszcz narodowy");

        author2 = new Author();
        author2.setId(2L);
        author2.setFirstName("Juliusz");
        author2.setLastName("Słowacki");
        author2.setBio("Też wieszcz");
    }

    @Test
    void shouldReturnAllAuthors() throws Exception {
        // given
        List<Author> authors = Arrays.asList(author1, author2);
        when(authorService.getAllAuthors()).thenReturn(authors);

        // when & then
        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Adam"))
                .andExpect(jsonPath("$[1].lastName").value("Słowacki"));
    }

    @Test
    void shouldReturnAuthorById() throws Exception {
        // given
        when(authorService.getAuthorById(1L)).thenReturn(author1);

        // when & then
        mockMvc.perform(get("/api/v1/authors/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Adam"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldCreateAuthor() throws Exception {
        // given
        Author newAuthor = new Author();
        newAuthor.setFirstName("Henryk");
        newAuthor.setLastName("Sienkiewicz");
        doNothing().when(authorService).saveAuthor(any(Author.class));

        // when & then
        mockMvc.perform(post("/api/v1/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newAuthor)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Henryk"))
                .andExpect(jsonPath("$.lastName").value("Sienkiewicz"));
        
        verify(authorService, times(1)).saveAuthor(any(Author.class));
    }

    @Test
    void shouldUpdateAuthor() throws Exception {
        // given
        Long authorId = 1L;
        Author updateDetails = new Author();
        updateDetails.setFirstName("Adam Zmieniony");
        updateDetails.setLastName("Mickiewicz");
        updateDetails.setBio("Nowe bio");

        when(authorService.getAuthorById(authorId)).thenReturn(author1);

        // when & then
        mockMvc.perform(put("/api/v1/authors/{id}", authorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Adam Zmieniony"))
                .andExpect(jsonPath("$.bio").value("Nowe bio"));

        // Weryfikacja
        verify(authorService, times(1)).getAuthorById(authorId);
        verify(authorService, times(1)).saveAuthor(any(Author.class));
    }

    @Test
    void shouldDeleteAuthorSuccessfully() throws Exception {
        // given
        Long authorId = 1L;
        doNothing().when(authorService).deleteAuthor(authorId);

        // when & then
        mockMvc.perform(delete("/api/v1/authors/{id}", authorId))
                .andExpect(status().isNoContent()); 

        verify(authorService, times(1)).deleteAuthor(authorId);
    }

    @Test
    void shouldReturnConflictWhenDeletingAuthorWithBooks() throws Exception {
        // given
        Long authorId = 1L;
        String errorMessage = "Nie można usunąć autora, który ma przypisane książki";
        
        doThrow(new RuntimeException(errorMessage))
                .when(authorService).deleteAuthor(authorId);

        // when & then
        mockMvc.perform(delete("/api/v1/authors/{id}", authorId))
                .andExpect(status().isConflict())
                .andExpect(content().string(errorMessage));
    }
}