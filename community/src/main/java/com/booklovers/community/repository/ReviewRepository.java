package com.booklovers.community.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.booklovers.community.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // pobierz recenzje dla konkretnej książki
    List<Review> findByBookId(Long bookId);

    // wymaganie: @Query - oblicz średnią ocenę dla książki
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double getAverageRatingForBook(@Param("bookId") Long bookId);
}
