package com.booklovers.community.controller;

import com.booklovers.community.model.Shelf;
import com.booklovers.community.service.ShelfService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ShelfController.class)
@AutoConfigureMockMvc(addFilters = false) 
class ShelfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShelfService shelfService;

    private Principal mockPrincipal(String username) {
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn(username);
        return principal;
    }

    @Test
    void shouldReturnUserShelves() throws Exception {
        // given
        String username = "czytelnik";
        Shelf shelf = new Shelf();
        shelf.setId(1L);
        shelf.setName("Do przeczytania");
        
        when(shelfService.getUserShelves(username)).thenReturn(Arrays.asList(shelf));

        // when & then
        mockMvc.perform(get("/api/v1/shelves")
                .principal(mockPrincipal(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Do przeczytania"));
    }

    @Test
    void shouldCreateShelfSuccessfully() throws Exception {
        // given
        String username = "user1";
        String shelfName = "Nowa Półka";

        doNothing().when(shelfService).createShelf(shelfName, username);

        // when & then
        mockMvc.perform(post("/api/v1/shelves")
                .param("name", shelfName) 
                .principal(mockPrincipal(username)))
                .andExpect(status().isCreated()) 
                .andExpect(content().string("Półka została utworzona"));
        
        verify(shelfService).createShelf(shelfName, username);
    }

    @Test
    void shouldReturnBadRequestWhenCreatingDuplicateShelf() throws Exception {
        // given
        String username = "user1";
        String shelfName = "Istniejąca Półka";

        doThrow(new RuntimeException("Półka o takiej nazwie już istnieje"))
                .when(shelfService).createShelf(shelfName, username);

        // when & then
        mockMvc.perform(post("/api/v1/shelves")
                .param("name", shelfName)
                .principal(mockPrincipal(username)))
                .andExpect(status().isBadRequest()) 
                .andExpect(content().string("Półka o takiej nazwie już istnieje"));
    }

    @Test
    void shouldAddBookToShelf() throws Exception {
        // given
        Long shelfId = 10L;
        Long bookId = 55L;
        String username = "user1";

        doNothing().when(shelfService).addBookToShelf(shelfId, bookId, username);

        // when & then
        mockMvc.perform(post("/api/v1/shelves/{shelfId}/books", shelfId)
                .param("bookId", String.valueOf(bookId))
                .principal(mockPrincipal(username)))
                .andExpect(status().isOk())
                .andExpect(content().string("Książka dodana do półki"));
    }

    @Test
    void shouldReturnBadRequestWhenAddingBookFails() throws Exception {
        // given
        Long shelfId = 10L;
        Long bookId = 55L;
        String username = "user1";

        doThrow(new RuntimeException("Książka już jest na półce"))
                .when(shelfService).addBookToShelf(shelfId, bookId, username);

        // when & then
        mockMvc.perform(post("/api/v1/shelves/{shelfId}/books", shelfId)
                .param("bookId", String.valueOf(bookId))
                .principal(mockPrincipal(username)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Książka już jest na półce"));
    }

    @Test
    void shouldRemoveBookFromShelf() throws Exception {
        // given
        Long shelfId = 10L;
        Long bookId = 55L;
        String username = "user1";

        doNothing().when(shelfService).removeBookFromShelf(shelfId, bookId, username);

        // when & then
        mockMvc.perform(delete("/api/v1/shelves/{shelfId}/books/{bookId}", shelfId, bookId)
                .principal(mockPrincipal(username)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldMoveBookSuccessfully() throws Exception {
        // given
        Long sourceShelfId = 1L;
        Long targetShelfId = 2L;
        Long bookId = 100L;
        String username = "user1";

        doNothing().when(shelfService).moveBook(sourceShelfId, targetShelfId, bookId, username);

        // when & then
        mockMvc.perform(post("/api/v1/shelves/{shelfId}/move", sourceShelfId)
                .param("bookId", String.valueOf(bookId))
                .param("targetShelfId", String.valueOf(targetShelfId))
                .principal(mockPrincipal(username)))
                .andExpect(status().isOk())
                .andExpect(content().string("Książka przeniesiona pomyślnie"));
        
        verify(shelfService).moveBook(sourceShelfId, targetShelfId, bookId, username);
    }
}
