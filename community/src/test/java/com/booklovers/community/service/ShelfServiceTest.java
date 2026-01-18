package com.booklovers.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.booklovers.community.model.Book;
import com.booklovers.community.model.Shelf;
import com.booklovers.community.model.User;
import com.booklovers.community.repository.BookRepository;
import com.booklovers.community.repository.ShelfRepository;
import com.booklovers.community.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ShelfServiceTest {
    @Mock
    private ShelfRepository shelfRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ShelfService shelfService;

    // pobieranie półek (Sukces)
    @Test
    void shouldReturnUserShelvesWhenUserExists() {
        // given
        String username = "jan_kowalski";
        User user = User.builder().id(1L).username(username).build();
        
        List<Shelf> shelves = List.of(
            Shelf.builder().name("Przeczytane").build(),
            Shelf.builder().name("Do przeczytania").build()
        );

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(shelfRepository.findAllByUserId(user.getId())).thenReturn(shelves);

        // when
        List<Shelf> result = shelfService.getUserShelves(username);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Przeczytane");
    }

    // pobieranie półek (Błąd - User nie istnieje)
    @Test
    void shouldThrowExceptionWhenGettingShelvesForNonExistentUser() {
        // given
        String username = "duch";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> shelfService.getUserShelves(username));

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    // dodawanie książki do półki (Sukces)
    @Test
    void shouldAddBookToShelfSuccessfully() {
        // given
        String username = "wlasciciel";
        Long shelfId = 10L;
        Long bookId = 50L;

        User user = User.builder().username(username).build();
        Shelf shelf = Shelf.builder()
                .id(shelfId)
                .user(user)
                .books(new ArrayList<>()) 
                .build();
        
        Book book = Book.builder().id(bookId).title("Wiedźmin").build();

        when(shelfRepository.findById(shelfId)).thenReturn(Optional.of(shelf));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // when
        shelfService.addBookToShelf(shelfId, bookId, username);

        // then
        assertThat(shelf.getBooks()).contains(book);
        verify(shelfRepository).save(shelf);
    }

    // blokada dostępu (Próba dodania do cudzej półki)
    @Test
    void shouldThrowExceptionWhenAddingToSomeoneElseShelf() {
        // given
        String ownerName = "wlasciciel";
        String hackerName = "haker";
        Long shelfId = 10L;
        Long bookId = 50L;

        User owner = User.builder().username(ownerName).build();
        Shelf shelf = Shelf.builder().id(shelfId).user(owner).build();

        when(shelfRepository.findById(shelfId)).thenReturn(Optional.of(shelf));
        
        // when
        Throwable thrown = catchThrowable(() -> shelfService.addBookToShelf(shelfId, bookId, hackerName));

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessage("Nie masz dostępu do tej półki");
        
        verify(bookRepository, never()).findById(any());
        verify(shelfRepository, never()).save(any());
    }

    // dodawanie książki, która już jest na półce (Unikanie duplikatów)
    @Test
    void shouldNotAddBookIfAlreadyOnShelf() {
        // given
        String username = "jan";
        Long shelfId = 1L;
        Long bookId = 2L;
        
        User user = User.builder().username(username).build();
        Book book = Book.builder().id(bookId).build();
        
        List<Book> booksOnShelf = new ArrayList<>();
        booksOnShelf.add(book);
        
        Shelf shelf = Shelf.builder()
                .id(shelfId)
                .user(user)
                .books(booksOnShelf)
                .build();

        when(shelfRepository.findById(shelfId)).thenReturn(Optional.of(shelf));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // when
        shelfService.addBookToShelf(shelfId, bookId, username);

        // then
        assertThat(shelf.getBooks()).hasSize(1);
        verify(shelfRepository, never()).save(any());
    }

    // błąd - Półka nie istnieje
    @Test
    void shouldThrowExceptionWhenShelfNotFound() {
        // given
        when(shelfRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> shelfService.addBookToShelf(999L, 1L, "user"));

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessage("Półka nie istnieje");
    }

    // błąd - książka nie istnieje 
    @Test
    void shouldThrowExceptionWhenBookNotFound() {
        // given
        String username = "jan";
        User user = User.builder().username(username).build();
        Shelf shelf = Shelf.builder().id(1L).user(user).build();

        when(shelfRepository.findById(1L)).thenReturn(Optional.of(shelf));
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> shelfService.addBookToShelf(1L, 999L, username));

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessage("Książka nie istnieje");
    }
}
