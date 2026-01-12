package com.booklovers.community.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.booklovers.community.model.Book;
import com.booklovers.community.model.Shelf;
import com.booklovers.community.model.User;
import com.booklovers.community.repository.BookRepository;
import com.booklovers.community.repository.ShelfRepository;
import com.booklovers.community.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShelfService {
    
    private final ShelfRepository shelfRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public List<Shelf> getUserShelves(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return shelfRepository.findAllByUserId(user.getId());
    }

    @Transactional
    public void addBookToShelf(Long shelfId, Long bookId, String username) {
        Shelf shelf = shelfRepository.findById(shelfId)
                .orElseThrow(() -> new RuntimeException("Półka nie istnieje"));
        
        // zy ta półka należy do tego użytkownika?
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
    
}
