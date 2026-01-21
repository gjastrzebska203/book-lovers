package com.booklovers.community.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.booklovers.community.model.Author;
import com.booklovers.community.repository.AuthorRepository;
import com.booklovers.community.repository.BookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorService {
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    public Author getAuthorById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Autor nie znaleziony"));
    }

    @Transactional
    public void saveAuthor(Author author) {
        authorRepository.save(author);
    }

    @Transactional
    public void deleteAuthor(Long id) {
        // nie usuwamy autora, jeśli ma książki
        if (bookRepository.existsByAuthorId(id)) {
            throw new RuntimeException("Nie można usunąć autora, który ma przypisane książki. Najpierw usuń książki.");
        }
        authorRepository.deleteById(id);
    }
}
