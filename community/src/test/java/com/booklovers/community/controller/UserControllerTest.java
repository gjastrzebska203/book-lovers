package com.booklovers.community.controller;

import com.booklovers.community.dto.UserRegisterDto;
import com.booklovers.community.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false) 
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        // given
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setUsername("nowyUser");
        registerDto.setPassword("Haslo123!");
        registerDto.setEmail("email@test.com");

        when(userService.registerUser(any(UserRegisterDto.class))).thenReturn(null);

        // when & then
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Użytkownik zarejestrowany pomyślnie"));

        verify(userService, times(1)).registerUser(any(UserRegisterDto.class));
    }

    @Test
    void shouldUpdateProfileWithBioAndAvatar() throws Exception {
        // given
        String username = "testUser";
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(username);

        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatar", "avatar.jpg", "image/jpeg", "obrazek".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/v1/users/profile")
                .file(avatarFile)
                .param("bio", "Nowy opis")
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                })
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(content().string("Profil zaktualizowany"));

        verify(userService).updateUserProfile(eq(username), eq("Nowy opis"), any());
    }

    @Test
    void shouldExportProfileAsJsonFile() throws Exception {
        // given
        String username = "exportUser";
        byte[] fakeJsonContent = "{ \"data\": \"backup\" }".getBytes();
        
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(username);
        
        when(userService.generateProfileBackup(username)).thenReturn(fakeJsonContent);

        // when & then
        mockMvc.perform(get("/api/v1/users/profile/export")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"backup_exportUser.json\""))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().bytes(fakeJsonContent));
    }

    @Test
    void shouldImportProfileSuccessfully() throws Exception {
        // given
        String username = "importUser";
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(username);

        MockMultipartFile backupFile = new MockMultipartFile(
                "file", "backup.json", "application/json", "{...}".getBytes()
        );

        doNothing().when(userService).importProfile(eq(username), any());

        // when & then
        mockMvc.perform(multipart("/api/v1/users/profile/import")
                .file(backupFile)
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(content().string("Profil zaimportowany pomyślnie"));
    }

    @Test
    void shouldReturnBadRequestWhenImportFileIsEmpty() throws Exception {
        // given
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user");

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.json", "application/json", new byte[0]
        );

        // when & then
        mockMvc.perform(multipart("/api/v1/users/profile/import")
                .file(emptyFile)
                .principal(mockPrincipal))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Plik jest pusty"));
        
        verify(userService, never()).importProfile(any(), any());
    }

    @Test
    void shouldReturnInternalServerErrorOnImportException() throws Exception {
        // given
        String username = "user";
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(username);
        
        MockMultipartFile file = new MockMultipartFile("file", "test.json", "json", "dane".getBytes());

        doThrow(new RuntimeException("Zły format pliku"))
                .when(userService).importProfile(eq(username), any());

        // when & then
        mockMvc.perform(multipart("/api/v1/users/profile/import")
                .file(file)
                .principal(mockPrincipal))
                .andExpect(status().isInternalServerError()) 
                .andExpect(content().string("Błąd importu: Zły format pliku"));
    }

    @Test
    void shouldToggleUserBlock() throws Exception {
        // given
        Long userId = 10L;
        doNothing().when(userService).toggleUserBlock(userId);

        // when & then
        mockMvc.perform(patch("/api/v1/users/{id}/toggle-block", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Status blokady zmieniony"));

        verify(userService).toggleUserBlock(userId);
    }

    @Test
    void shouldDeleteUser() throws Exception {
        // given
        Long userId = 99L;
        doNothing().when(userService).deleteUserByAdmin(userId);

        // when & then
        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent()); 

        verify(userService).deleteUserByAdmin(userId);
    }
}