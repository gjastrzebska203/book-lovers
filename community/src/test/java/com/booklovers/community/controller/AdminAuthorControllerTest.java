// package com.booklovers.community.controller;

// import static org.hamcrest.Matchers.hasProperty;
// import static org.hamcrest.Matchers.is;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.doThrow;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// import java.util.List;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.context.annotation.Import;
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.web.servlet.MockMvc;

// import com.booklovers.community.model.Author;
// import com.booklovers.community.security.SecurityConfig;
// import com.booklovers.community.service.AuthorService;

// @WebMvcTest(AdminAuthorController.class)
// @Import(SecurityConfig.class)
// public class AdminAuthorControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private AuthorService authorService;

//     // 1. Wyświetlanie listy autorów
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldListAuthors() throws Exception {
//         // given
//         Author author1 = new Author(); author1.setId(1L); author1.setFirstName("Adam");
//         Author author2 = new Author(); author2.setId(2L); author2.setFirstName("Juliusz");
        
//         when(authorService.getAllAuthors()).thenReturn(List.of(author1, author2));

//         // when & then
//         mockMvc.perform(get("/admin/authors"))
//                 .andExpect(status().isOk())
//                 .andExpect(view().name("admin/authors"))
//                 .andExpect(model().attributeExists("authors"))
//                 .andExpect(model().attribute("authors", org.hamcrest.Matchers.hasSize(2)));
//     }

//     // 2. Formularz dodawania nowego autora
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldShowAddForm() throws Exception {
//         mockMvc.perform(get("/admin/authors/new"))
//                 .andExpect(status().isOk())
//                 .andExpect(view().name("admin/author-form"))
//                 .andExpect(model().attributeExists("author"))
//                 .andExpect(model().attribute("author", hasProperty("id", org.hamcrest.Matchers.nullValue())));
//     }

//     // 3. Formularz edycji autora
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldShowEditForm() throws Exception {
//         // given
//         Long authorId = 1L;
//         Author author = new Author();
//         author.setId(authorId);
//         author.setFirstName("Henryk");
        
//         when(authorService.getAuthorById(authorId)).thenReturn(author);

//         // when & then
//         mockMvc.perform(get("/admin/authors/edit/{id}", authorId))
//                 .andExpect(status().isOk())
//                 .andExpect(view().name("admin/author-form"))
//                 .andExpect(model().attribute("author", hasProperty("firstName", is("Henryk"))));
//     }

//     // 4. Zapisywanie autora (POST)
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldSaveAuthorAndRedirect() throws Exception {
//         // when & then
//         mockMvc.perform(post("/admin/authors/save")
//                         .param("firstName", "Stanisław")
//                         .param("lastName", "Lem")
//                         .with(csrf())) // Wymagany CSRF
//                 .andExpect(status().is3xxRedirection())
//                 .andExpect(redirectedUrl("/admin/authors?success"));

//         verify(authorService).saveAuthor(any(Author.class));
//     }

//     // 5. Usuwanie autora - Sukces
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldDeleteAuthorAndRedirect() throws Exception {
//         // given
//         Long authorId = 10L;

//         // when & then
//         mockMvc.perform(post("/admin/authors/delete/{id}", authorId)
//                         .with(csrf()))
//                 .andExpect(status().is3xxRedirection())
//                 .andExpect(redirectedUrl("/admin/authors?deleted"));

//         verify(authorService).deleteAuthor(authorId);
//     }

//     // 6. Usuwanie autora - Błąd (np. autor ma przypisane książki)
//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void shouldRedirectWithErrorWhenDeleteFails() throws Exception {
//         // given
//         Long authorId = 10L;
//         String errorMessage = "Nie można usunąć autora, który ma przypisane książki";

//         // Symulujemy rzucenie wyjątku przez serwis
//         doThrow(new RuntimeException(errorMessage)).when(authorService).deleteAuthor(authorId);

//         // when & then
//         mockMvc.perform(post("/admin/authors/delete/{id}", authorId)
//                         .with(csrf()))
//                 .andExpect(status().is3xxRedirection())
//                 // Sprawdzamy czy URL zawiera parametr error (pattern, bo URL będzie zakodowany)
//                 .andExpect(redirectedUrlPattern("/admin/authors?error=*")); 
//     }

//     // 7. Zabezpieczenia - Brak dostępu dla zwykłego Usera
//     @Test
//     @WithMockUser(roles = "USER")
//     void shouldForbidAccessForRegularUser() throws Exception {
//         mockMvc.perform(get("/admin/authors"))
//                 .andExpect(status().isForbidden());
//     }
// }