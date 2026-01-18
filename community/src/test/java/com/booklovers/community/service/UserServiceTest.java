package com.booklovers.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.booklovers.community.dao.BookStatisticsDao;
import com.booklovers.community.dto.UserRegisterDto;
import com.booklovers.community.model.Shelf;
import com.booklovers.community.model.User;
import com.booklovers.community.repository.ShelfRepository;
import com.booklovers.community.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock 
    private UserRepository userRepository;
    @Mock
    private ShelfRepository shelfRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private BookStatisticsDao bookStatisticsDao;

    @InjectMocks
    private UserService userService;

    // rejestracja użytkownika (sukces)
    @Test
    void shouldRegisterUserAndCreateDefaultShelves() {
        // given
        UserRegisterDto dto = new UserRegisterDto();
        dto.setUsername("janusz");
        dto.setEmail("janusz@pl.pl");
        dto.setPassword("haslo123");

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashed_secret");
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        // when
        User result = userService.registerUser(dto);

        // then
        assertThat(result.getPassword()).isEqualTo("hashed_secret");
        
        verify(userRepository).save(any(User.class));
        
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Shelf>> captor = ArgumentCaptor.forClass(List.class);
        verify(shelfRepository).saveAll(captor.capture());
        
        List<Shelf> capturedShelves = captor.getValue();
        assertThat(capturedShelves).hasSize(3);
        assertThat(capturedShelves).extracting(Shelf::getName)
                .contains("Przeczytane", "Chcę przeczytać", "Teraz czytam");
    }

    // rejestracja nieudana (zajęty login)
    @Test
    void shouldThrowExceptionWhenUsernameTaken() {
        // given
        UserRegisterDto dto = new UserRegisterDto();
        dto.setUsername("zajety_login");

        when(userRepository.existsByUsername("zajety_login")).thenReturn(true);

        // when
        Throwable thrown = catchThrowable(() -> userService.registerUser(dto));

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessage("Nazwa użytkownika zajęta!");
        
        verify(userRepository, never()).save(any());
    }

    // aktualizacja profilu z avatarem
    @Test
    void shouldUpdateProfileWithAvatar() {
        // given
        String username = "jan";
        String newBio = "Lubię książki";
        MultipartFile mockFile = mock(MultipartFile.class);
        
        User existingUser = User.builder().username(username).bio("Stare bio").build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(mockFile)).thenReturn("avatar123.jpg");

        // when
        userService.updateUserProfile(username, newBio, mockFile);

        // then
        assertThat(existingUser.getBio()).isEqualTo(newBio);
        assertThat(existingUser.getAvatar()).isEqualTo("/uploads/avatar123.jpg");
        
        verify(userRepository).save(existingUser);
    }

    // rejestracja nieudana (zajęty email)
    @Test
    void shouldThrowExceptionWhenEmailTaken() {
        // given
        UserRegisterDto dto = new UserRegisterDto();
        dto.setUsername("nowy");
        dto.setEmail("zajety@email.pl");

        when(userRepository.existsByUsername("nowy")).thenReturn(false);
        when(userRepository.existsByEmail("zajety@email.pl")).thenReturn(true);

        // when
        Throwable thrown = catchThrowable(() -> userService.registerUser(dto));

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessage("Email już istnieje!");
        verify(userRepository, never()).save(any());
    }

    // aktualizacja profilu BEZ zmiany avatara
    @Test
    void shouldUpdateBioWithoutChangingAvatar() {
        // given
        String username = "jan";
        String newBio = "Nowe bio";
        
        User existingUser = User.builder()
                .username(username)
                .avatar("/uploads/stary.jpg")
                .bio("Stare")
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        // when
        userService.updateUserProfile(username, newBio, null);

        // then
        assertThat(existingUser.getBio()).isEqualTo(newBio);
        assertThat(existingUser.getAvatar()).isEqualTo("/uploads/stary.jpg"); 
        
        verify(fileStorageService, never()).storeFile(any());
        verify(userRepository).save(existingUser);
    }

    // blokowanie użytkownika (toggle)
    @Test
    void shouldToggleUserBlockStatus() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).enabled(true).build(); 

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        userService.toggleUserBlock(userId);

        // then
        assertThat(user.isEnabled()).isFalse(); 
        verify(userRepository).save(user);
    }
    
    // pobieranie statystyk (DAO)
    @Test
    void shouldGetBooksReadThisYear() {
        // given
        Long userId = 5L;
        int currentYear = java.time.LocalDate.now().getYear();
        
        when(bookStatisticsDao.countBooksReadInYear(userId, currentYear)).thenReturn(12);

        // when
        int count = userService.getBooksReadThisYear(userId);

        // then
        assertThat(count).isEqualTo(12);
        verify(bookStatisticsDao).countBooksReadInYear(userId, currentYear);
    }

    // User Not Found
    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // given
        String username = "duch";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> userService.findByUsername(username));

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }
}
