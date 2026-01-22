package com.booklovers.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    // tworzenie półki (sukces)
    @Test
    void shouldCreateShelfSuccessfully() {
        // given
        String username = "jan";
        String shelfName = "Fantastyka";
        User user = User.builder().id(1L).username(username).build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(shelfRepository.findByNameAndUserId(shelfName, user.getId())).thenReturn(Optional.empty());

        // when
        shelfService.createShelf(shelfName, username);

        // then
        ArgumentCaptor<Shelf> shelfCaptor = ArgumentCaptor.forClass(Shelf.class);
        verify(shelfRepository).save(shelfCaptor.capture());
        
        Shelf savedShelf = shelfCaptor.getValue();
        assertThat(savedShelf.getName()).isEqualTo(shelfName);
        assertThat(savedShelf.getUser()).isEqualTo(user);
        assertThat(savedShelf.getBooks()).isEmpty();
    }

    // tworzenie półki (błąd - półka już istnieje)
    @Test
    void shouldThrowExceptionWhenCreatingExistingShelf() {
        // given
        String username = "jan";
        String shelfName = "Istniejaca";
        User user = User.builder().id(1L).username(username).build();
        Shelf existingShelf = Shelf.builder().name(shelfName).build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(shelfRepository.findByNameAndUserId(shelfName, user.getId())).thenReturn(Optional.of(existingShelf));

        // when
        Throwable thrown = catchThrowable(() -> shelfService.createShelf(shelfName, username));

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessage("Masz już półkę o takiej nazwie.");
        verify(shelfRepository, never()).save(any());
    }

    // usuwanie książki z półki (sukces)
    @Test
    void shouldRemoveBookFromShelf() {
        // given
        String username = "jan";
        Long shelfId = 1L;
        Long bookId = 100L;
        
        User user = User.builder().username(username).build();
        Book book = Book.builder().id(bookId).title("Hobbit").build();
        
        List<Book> books = new ArrayList<>(); 
        books.add(book);
        
        Shelf shelf = Shelf.builder().id(shelfId).user(user).books(books).build();

        when(shelfRepository.findById(shelfId)).thenReturn(Optional.of(shelf));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // when
        shelfService.removeBookFromShelf(shelfId, bookId, username);

        // then
        assertThat(shelf.getBooks()).isEmpty();
        verify(shelfRepository).save(shelf);
    }

    // przenoszenie książki (sukces)
    @Test
    void shouldMoveBookBetweenShelves() {
        // given
        String username = "jan";
        Long sourceShelfId = 1L;
        Long targetShelfId = 2L;
        Long bookId = 50L;
        
        User user = User.builder().username(username).build();
        Book book = Book.builder().id(bookId).build();
        
        Shelf sourceShelf = Shelf.builder().id(sourceShelfId).user(user).books(new ArrayList<>(List.of(book))).build();
        Shelf targetShelf = Shelf.builder().id(targetShelfId).user(user).books(new ArrayList<>()).build();

        when(shelfRepository.findById(sourceShelfId)).thenReturn(Optional.of(sourceShelf));
        when(shelfRepository.findById(targetShelfId)).thenReturn(Optional.of(targetShelf));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // when
        shelfService.moveBook(sourceShelfId, targetShelfId, bookId, username);

        // then
        assertThat(sourceShelf.getBooks()).doesNotContain(book);
        verify(shelfRepository).save(sourceShelf);
        
        // Książka dodana do docelowej
        assertThat(targetShelf.getBooks()).contains(book);
        verify(shelfRepository).save(targetShelf);
    }

    // licznik przeczytanych książek (gdy półka istnieje)
    @Test
    void shouldReturnCorrectBooksReadCount() {
        // given
        String username = "jan";
        Long userId = 1L;
        User user = User.builder().id(userId).username(username).build();
        
        List<Book> readBooks = List.of(new Book(), new Book(), new Book()); 
        Shelf readShelf = Shelf.builder().name("Przeczytane").books(readBooks).build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(shelfRepository.findByNameAndUserId("Przeczytane", userId)).thenReturn(Optional.of(readShelf));

        // when
        int count = shelfService.getBooksReadCount(username);

        // then
        assertThat(count).isEqualTo(3);
    }

    // licznik przeczytanych książek (gdy półka nie istnieje - np. błąd danych)
    @Test
    void shouldReturnZeroIfReadShelfNotFound() {
        // given
        String username = "jan";
        Long userId = 1L;
        User user = User.builder().id(userId).username(username).build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(shelfRepository.findByNameAndUserId("Przeczytane", userId)).thenReturn(Optional.empty());

        // when
        int count = shelfService.getBooksReadCount(username);

        // then
        assertThat(count).isEqualTo(0);
    }

    // usuwanie wszystkich półek użytkownika
    @Test
    void shouldDeleteAllShelvesByUserId() {
        // given
        Long userId = 123L;

        // when
        shelfService.deleteAllByUserId(userId);

        // then
        verify(shelfRepository).deleteAllByUserId(userId);
    }

    @Test
    void shouldThrowExceptionWhenUserTriesToEditOthersShelf() {
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("pass");
        userRepository.save(user2);

        Shelf shelfOfUser2 = new Shelf();
        shelfOfUser2.setName("Półka Usera 2");
        shelfOfUser2.setUser(user2);
        shelfRepository.save(shelfOfUser2);

        String loggedInUser = "user1"; 

        assertThrows(RuntimeException.class, () -> {
            shelfService.moveBook(shelfOfUser2.getId(), 999L, 1L, loggedInUser);
        });
    }
}
