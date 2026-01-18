package com.booklovers.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class FileStorageServiceTest {
    private FileStorageService fileStorageService;

    @Mock
    private MultipartFile multipartFile;

    private String createdFileName; 

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (createdFileName != null) {
            Path path = Paths.get("uploads").resolve(createdFileName);
            Files.deleteIfExists(path);
        }
    }

    // poprawny zapis pliku
    @Test
    void shouldStoreFileSuccessfully() throws IOException {
        // given
        String originalFilename = "test-image.jpg";
        byte[] content = "test content".getBytes();

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));

        // when
        createdFileName = fileStorageService.storeFile(multipartFile);

        // then
        assertThat(createdFileName).isNotNull();
        assertThat(createdFileName).contains(originalFilename);
        assertThat(createdFileName).contains("_");

        Path savedPath = Paths.get("uploads").resolve(createdFileName);
        assertTrue(Files.exists(savedPath), "Plik powinien zostać utworzony na dysku");
        
        assertThat(Files.readAllBytes(savedPath)).isEqualTo(content);
    }

    // próba zapisu pustego pliku ---
    @Test
    void shouldThrowExceptionWhenFileIsEmpty() {
        // given
        when(multipartFile.isEmpty()).thenReturn(true);

        // when
        Throwable thrown = catchThrowable(() -> fileStorageService.storeFile(multipartFile));

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessage("Nie można zapisać pustego pliku.");
        
        verify(multipartFile, never()).getOriginalFilename();
    }

    // błąd IO (IOException) podczas zapisu
    @Test
    void shouldThrowExceptionWhenIoErrorOccurs() throws IOException {
        // given
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("error.txt");
        when(multipartFile.getInputStream()).thenThrow(new IOException("Disk error"));

        // when
        Throwable thrown = catchThrowable(() -> fileStorageService.storeFile(multipartFile));

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Błąd zapisu pliku");
    }
}
