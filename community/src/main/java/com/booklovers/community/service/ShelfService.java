package com.booklovers.community.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.booklovers.community.model.Book;
import com.booklovers.community.model.Shelf;
import com.booklovers.community.model.User;
import com.booklovers.community.repository.BookRepository;
import com.booklovers.community.repository.ShelfRepository;
import com.booklovers.community.repository.UserRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Validated
public class ShelfService {
    
    private final ShelfRepository shelfRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public List<Shelf> getUserShelves(@NotBlank String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return shelfRepository.findAllByUserId(user.getId());
    }

    @Transactional
    public void addBookToShelf(@NotNull Long shelfId, @NotNull Long bookId, @NotBlank String username) {
        Shelf shelf = shelfRepository.findById(shelfId)
                .orElseThrow(() -> new RuntimeException("Półka nie istnieje"));

        if (!shelf.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Nie masz dostępu do tej półki");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Książka nie istnieje"));

        if (!shelf.getBooks().contains(book)) {
            shelf.getBooks().add(book);
            shelfRepository.save(shelf);
        }
    }
    
    // usuwanie wszystkich półek użytkownika (np. przy usuwaniu konta)
    @Transactional
    public void deleteAllByUserId(Long userId) {
        shelfRepository.deleteAllByUserId(userId);
    }

    @Transactional
    public void createShelf(@NotBlank String shelfName, @NotBlank String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean exists = shelfRepository.findByNameAndUserId(shelfName, user.getId()).isPresent();
        
        if (exists) {
            throw new RuntimeException("Masz już półkę o takiej nazwie.");
        }

        Shelf shelf = new Shelf();
        shelf.setName(shelfName);
        shelf.setUser(user);
        shelf.setBooks(new java.util.ArrayList<>());
        shelfRepository.save(shelf);
    }

    @Transactional
    public void removeBookFromShelf(@NotNull Long shelfId, @NotNull Long bookId, @NotBlank String username) {
        Shelf shelf = getShelfAndValidateOwner(shelfId, username);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Książka nie istnieje"));

        if (shelf.getBooks().contains(book)) {
            shelf.getBooks().remove(book);
            shelfRepository.save(shelf);
        }
    }

    @Transactional
    public void moveBook(@NotNull Long sourceShelfId, @NotNull Long targetShelfId, @NotNull Long bookId, @NotBlank String username) {
        removeBookFromShelf(sourceShelfId, bookId, username);
        addBookToShelf(targetShelfId, bookId, username);
    }

    private Shelf getShelfAndValidateOwner(Long shelfId, String username) {
        Shelf shelf = shelfRepository.findById(shelfId)
                .orElseThrow(() -> new RuntimeException("Półka nie istnieje"));
        if (!shelf.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Nie masz dostępu do tej półki");
        }
        return shelf;
    }

    public int getBooksReadCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return shelfRepository.findByNameAndUserId("Przeczytane", user.getId())
                .map(shelf -> shelf.getBooks().size())
                .orElse(0);
    }
}
