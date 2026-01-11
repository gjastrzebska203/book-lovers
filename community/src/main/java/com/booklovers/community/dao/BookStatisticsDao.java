package com.booklovers.community.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.booklovers.community.dto.RatingStatDto;

@Repository
public class BookStatisticsDao {
    
    private final JdbcTemplate jdbcTemplate;

    // Wstrzykiwanie JdbcTemplate przez konstruktor
    public BookStatisticsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //  wymaganie: zapytania SELECT z query() i RowMapper
    //  funkcja: pobiera rozkład ocen dla danej książki (histogram)
    //  SQL: grupuje recenzje po ocenie i zlicza wystąpienia
    public List<RatingStatDto> getRatingDistribution(Long bookId) {
        String sql = "SELECT rating, COUNT(*) as count FROM reviews WHERE book_id = ? GROUP BY rating ORDER BY rating DESC";
        // użycie query() z ręcznym RowMapperem
        return jdbcTemplate.query(sql, new RowMapper<RatingStatDto>() {
            @Override
            public RatingStatDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new RatingStatDto(
                    rs.getInt("rating"),
                    rs.getLong("count")
                );
            }
        }, bookId);
    }

    // wymaganie: użycie JdbcTemplate (metoda queryForObject dla pojedynczej wartości)
    // funkcja: liczy ile książek użytkownik zrecenzował w danym roku (Reading Challenge)
    public Integer countBooksReadInYear(Long userId, int year) {
        // zakładamy, że recenzja = przeczytana książka 
        // H2/Postgres używają EXTRACT(YEAR FROM ...)
        String sql = "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND EXTRACT(YEAR FROM created_at) = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, year);
    }

    // wymaganie: operacje INSERT/UPDATE/DELETE z update().
    // funkcja: szybka aktualizacja opisu książki (np. przez administratora), z pominięciem pełnego cyklu życia encji JPA.
    public void updateBookDescriptionRaw(Long bookId, String newDescription) {
        String sql = "UPDATE books SET description = ? WHERE id = ?";
        // metoda update() zwraca liczbę zmodyfikowanych wierszy
        int rowsAffected = jdbcTemplate.update(sql, newDescription, bookId);
        if (rowsAffected == 0) {
            throw new RuntimeException("Nie znaleziono książki o ID: " + bookId);
        }
    }

    // wymaganie: operacje INSERT/UPDATE/DELETE z update()
    // funkcja: usuwanie wszystkich recenzji użytkownika (np. przy usuwaniu konta - czyszczenie danych)
    public void deleteAllReviewsByUserId(Long userId) {
        String sql = "DELETE FROM reviews WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

}
