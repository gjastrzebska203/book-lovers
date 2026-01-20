package com.booklovers.community.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.booklovers.community.model.Shelf;

@Repository
public interface ShelfRepository extends JpaRepository<Shelf, Long> {

    // Zznajdź wszystkie półki danego użytkownika
    List<Shelf> findAllByUserId(Long userId);
    
    // sprawdź czy użytkownik ma już półkę o takiej nazwie (unikalność nazw półek dla usera)
    boolean existsByNameAndUserId(String name, Long userId);

    void deleteAllByUserId(Long userId);

    Optional<Shelf> findByNameAndUserId(String name, Long userId);

    @Query("SELECT COUNT(s) FROM Shelf s JOIN s.books b WHERE b.id = :bookId")
    long countReadersByBookId(@Param("bookId") Long bookId);
}
