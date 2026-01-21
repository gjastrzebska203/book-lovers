// package com.booklovers.community.controller;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.context.annotation.Import;
// import org.springframework.http.MediaType;
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.web.servlet.MockMvc;

// import com.booklovers.community.dto.UserRegisterDto;
// import com.booklovers.community.model.User;
// import com.booklovers.community.service.UserService;
// import com.fasterxml.jackson.databind.ObjectMapper;

// import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// import static org.hamcrest.Matchers.containsString;

// @WebMvcTest(AuthController.class)
// @Import(GlobalExceptionHandler.class)
// public class AuthControllerTest {
//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @MockBean
//     private UserService userService;

//     // poprawna rejestracja (201 Created)
//     @Test
//     @WithMockUser // Potrzebne, by Spring Security pozwolił na wykonanie requestu (nawet jeśli endpoint jest publiczny, CSRF działa)
//     void shouldRegisterUserSuccessfully() throws Exception {
//         // given
//         UserRegisterDto dto = new UserRegisterDto();
//         dto.setUsername("nowy_user");
//         dto.setEmail("nowy@test.pl");
//         dto.setPassword("Haslo123!"); // Zakładamy, że spełnia wymogi walidacji

//         // Symulujemy, że serwis zwraca utworzonego użytkownika z ID 10
//         User createdUser = User.builder().id(10L).username("nowy_user").build();
//         when(userService.registerUser(any(UserRegisterDto.class))).thenReturn(createdUser);

//         String jsonRequest = objectMapper.writeValueAsString(dto);

//         // when & then
//         mockMvc.perform(post("/api/v1/auth/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(jsonRequest)
//                         .with(csrf())) // CSRF token jest wymagany przy POST
//                 .andExpect(status().isCreated()) // Kod 201
//                 .andExpect(content().string(containsString("ID: 10"))); // Sprawdzamy treść body

//         verify(userService).registerUser(any(UserRegisterDto.class));
//     }

//     // błąd walidacji danych (400 Bad Request)
//     @Test
//     @WithMockUser
//     void shouldReturnBadRequestWhenValidationFails() throws Exception {
//         // given
//         // Pusty username i niepoprawny email
//         UserRegisterDto invalidDto = new UserRegisterDto();
//         invalidDto.setUsername(""); 
//         invalidDto.setEmail("zly-email"); 
//         invalidDto.setPassword("123");

//         String jsonRequest = objectMapper.writeValueAsString(invalidDto);

//         // when & then
//         mockMvc.perform(post("/api/v1/auth/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(jsonRequest)
//                         .with(csrf()))
//                 .andExpect(status().isBadRequest()) // Kod 400
//                 // Sprawdzamy, czy GlobalExceptionHandler zwrócił mapę błędów
//                 .andExpect(jsonPath("$.username").exists()) // Błąd dla pola username
//                 .andExpect(jsonPath("$.email").exists());   // Błąd dla pola email
        
//         // Serwis NIE powinien zostać wywołany, bo walidacja zatrzymała proces
//         verify(userService, never()).registerUser(any());
//     }

//     // błąd biznesowy - np. zajęty email (400 Bad Request z ExceptionHandler)
//     @Test
//     @WithMockUser
//     void shouldReturnErrorWhenUserAlreadyExists() throws Exception {
//         // given
//         UserRegisterDto dto = new UserRegisterDto();
//         dto.setUsername("zajety");
//         dto.setEmail("zajety@test.pl");
//         dto.setPassword("Haslo123!");

//         // Symulujemy, że serwis rzuca wyjątek (tak jak zdefiniowaliśmy to w UserService)
//         when(userService.registerUser(any(UserRegisterDto.class)))
//                 .thenThrow(new RuntimeException("Email już istnieje!"));

//         String jsonRequest = objectMapper.writeValueAsString(dto);

//         // when & then
//         mockMvc.perform(post("/api/v1/auth/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(jsonRequest)
//                         .with(csrf()))
//                 .andExpect(status().isBadRequest()) // Twój GlobalExceptionHandler mapuje RuntimeException na 400
//                 .andExpect(jsonPath("$.message").value("Email już istnieje!"));
//     }

//     // błędny format JSON (np. brak klamry zamykającej) - 400 Bad Request
//     @Test
//     @WithMockUser
//     void shouldReturnBadRequestWhenJsonIsMalformed() throws Exception {
//         // given
//         String malformedJson = "{\"username\": \"test\""; // Urwany JSON

//         // when & then
//         mockMvc.perform(post("/api/v1/auth/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(malformedJson)
//                         .with(csrf()))
//                 .andExpect(status().isBadRequest()); 
//                 // Spring automatycznie rzuci HttpMessageNotReadableException
//     }

//     // brak tokena CSRF - 403 Forbidden
//     // To ważny test bezpieczeństwa, ponieważ endpointy POST domyślnie wymagają CSRF w konfiguracji WebMvcTest
//     @Test
//     @WithMockUser
//     void shouldReturnForbiddenWhenCsrfTokenIsMissing() throws Exception {
//         // given
//         UserRegisterDto dto = new UserRegisterDto();
//         dto.setUsername("haker");
//         dto.setEmail("haker@test.pl");
//         dto.setPassword("pass");
        
//         String jsonRequest = objectMapper.writeValueAsString(dto);

//         // when & then
//         mockMvc.perform(post("/api/v1/auth/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(jsonRequest))
//                         // BRAK .with(csrf())
//                 .andExpect(status().isForbidden());
//     }

//     // nieobsługiwany Content-Type (np. text/plain zamiast application/json) - 415 Unsupported Media Type
//     @Test
//     @WithMockUser
//     void shouldReturnUnsupportedMediaTypeWhenContentTypeIsWrong() throws Exception {
//         // given
//         String textContent = "username=test&email=test@test.pl";

//         // when & then
//         mockMvc.perform(post("/api/v1/auth/register")
//                         .contentType(MediaType.TEXT_PLAIN) // Błędny typ
//                         .content(textContent)
//                         .with(csrf()))
//                 .andExpect(status().isUnsupportedMediaType());
//     }

//     // puste ciało żądania (Empty Body) - 400 Bad Request
//     @Test
//     @WithMockUser
//     void shouldReturnBadRequestWhenBodyIsMissing() throws Exception {
//         // when & then
//         mockMvc.perform(post("/api/v1/auth/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content("") // Puste body
//                         .with(csrf()))
//                 .andExpect(status().isBadRequest()); 
//                 // HttpMessageNotReadableException: Required request body is missing
//     }
// }
