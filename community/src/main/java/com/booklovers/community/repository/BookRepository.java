package com.booklovers.community.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.booklovers.community.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

    // wymaganie: custom query methods (findBy...)
    boolean existsByIsbn(String isbn);

    // wymaganie: pageable support (Page<T>) + wyszukiwanie (wymaganie specyficzne)
    // szukamy po tytule, nazwisku autora LUB numerze ISBN
    @Query("SELECT b FROM Book b " +
           "WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(b.author.lastName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Book> searchBooks(@Param("query") String query, Pageable pageable);
    
    // pobieranie wszystkich książek z paginacją (do listy głównej)
    Page<Book> findAll(Pageable pageable);
    
}
