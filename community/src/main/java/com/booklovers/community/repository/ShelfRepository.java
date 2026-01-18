package com.booklovers.community.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.booklovers.community.model.Shelf;

@Repository
public interface ShelfRepository extends JpaRepository<Shelf, Long> {

    // Zznajdź wszystkie półki danego użytkownika
    List<Shelf> findAllByUserId(Long userId);
    
    // sprawdź czy użytkownik ma już półkę o takiej nazwie (unikalność nazw półek dla usera)
    boolean existsByNameAndUserId(String name, Long userId);

    void deleteAllByUserId(Long userId);
}
