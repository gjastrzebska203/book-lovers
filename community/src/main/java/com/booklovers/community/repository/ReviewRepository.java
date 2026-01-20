package com.booklovers.community.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // anonimizacja recenzji
    @Modifying
    @Query("UPDATE Review r SET r.user = NULL WHERE r.user.id = :userId")
    void anonymizeReviewsByUserId(@Param("userId") Long userId);

    List<Review> findAllByUserId(Long userId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.book.id = :bookId GROUP BY r.rating")
    List<Object[]> getRatingDistribution(@Param("bookId") Long bookId);

    long countByBookId(Long bookId);
}
