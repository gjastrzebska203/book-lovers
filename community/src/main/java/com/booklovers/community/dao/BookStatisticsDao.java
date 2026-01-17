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

    public BookStatisticsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RatingStatDto> getRatingDistribution(Long bookId) {
        String sql = "SELECT rating, COUNT(*) as count FROM reviews WHERE book_id = ? GROUP BY rating ORDER BY rating DESC";
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

    public Integer countBooksReadInYear(Long userId, int year) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND EXTRACT(YEAR FROM created_at) = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, year);
    }

    public void updateBookDescriptionRaw(Long bookId, String newDescription) {
        String sql = "UPDATE books SET description = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, newDescription, bookId);
        if (rowsAffected == 0) {
            throw new RuntimeException("Nie znaleziono książki o ID: " + bookId);
        }
    }

    public void deleteAllReviewsByUserId(Long userId) {
        String sql = "DELETE FROM reviews WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
}
