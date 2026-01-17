package com.booklovers.community.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.booklovers.community.dao.BookStatisticsDao;
import com.booklovers.community.dto.RatingStatDto;

@DataJpaTest
@Import(BookStatisticsDao.class)
public class BookStatisticsDaoTest {
    @Autowired
    private BookStatisticsDao bookStatisticsDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper (getRatingDistribution)
    @Test
    void shouldGetRatingDistributionUsingRowMapper() {
        // given
        jdbcTemplate.update("INSERT INTO authors (id, first_name, last_name) VALUES (1000, 'Test', 'Author')");
        jdbcTemplate.update("INSERT INTO books (id, title, isbn, author_id) VALUES (1000000, 'Test Book', 'ISBN-TEST', 1000)");
        jdbcTemplate.update("INSERT INTO users (id, username, email, password, role, enabled) VALUES (100, 'user1', 'u1@t.pl', 'pass', 'USER', true)");
        jdbcTemplate.update("INSERT INTO reviews (id, rating, content, book_id, user_id, created_at) VALUES (100, 5, 'Super', 1000000, 100, CURRENT_TIMESTAMP)");
        jdbcTemplate.update("INSERT INTO reviews (id, rating, content, book_id, user_id, created_at) VALUES (101, 5, 'Ekstra', 1000000, 100, CURRENT_TIMESTAMP)");
        jdbcTemplate.update("INSERT INTO reviews (id, rating, content, book_id, user_id, created_at) VALUES (102, 4, 'Ok', 1000000, 100, CURRENT_TIMESTAMP)");

        // when
        List<RatingStatDto> stats = bookStatisticsDao.getRatingDistribution(1000000L);

        // then
        assertThat(stats).hasSize(2); // Powinny być dwa wpisy: dla oceny 5 i dla oceny 4
        
        // Sprawdzamy czy RowMapper poprawnie zmapował wyniki
        RatingStatDto fiveStars = stats.stream().filter(s -> s.getRating() == 5).findFirst().orElseThrow();
        assertThat(fiveStars.getCount()).isEqualTo(2); // Dwie piątki

        RatingStatDto fourStars = stats.stream().filter(s -> s.getRating() == 4).findFirst().orElseThrow();
        assertThat(fourStars.getCount()).isEqualTo(1); // Jedna czwórka
    }

    // countBooksReadInYear
    @Test
    void shouldCountBooksReadInYear() {
        // given
        jdbcTemplate.update("INSERT INTO authors (id, first_name, last_name) VALUES (1000, 'Test', 'Author')");
        jdbcTemplate.update("INSERT INTO users (id, username, email, password, role, enabled) VALUES (1000, 'reader', 'r@t.pl', 'pass', 'USER', true)");
        jdbcTemplate.update("INSERT INTO books (id, title, isbn, author_id) VALUES (1000000, 'Test Book', 'ISBN-TEST', 1000)");
        // Dodajemy recenzję z konkretną datą (rok 2023)
        jdbcTemplate.update("INSERT INTO reviews (id, rating, content, user_id, book_id, created_at) VALUES (10, 5, 'Super', 1000, 1000000, '2023-05-01 12:00:00')");

        // when
        int count = bookStatisticsDao.countBooksReadInYear(1000L, 2023);

        // then
        assertThat(count).isEqualTo(1);
    }
}
