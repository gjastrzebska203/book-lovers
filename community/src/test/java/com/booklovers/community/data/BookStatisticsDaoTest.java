package com.booklovers.community.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

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

    // aktualizacja opisu książki (Sukces)
    @Test
    void shouldUpdateBookDescriptionRaw() {
        // given
        long bookId = 2000000L;
        jdbcTemplate.update("INSERT INTO authors (id, first_name, last_name) VALUES (2000, 'Desc', 'Updater')");
        // Wstawiamy książkę ze starym opisem
        jdbcTemplate.update("INSERT INTO books (id, title, isbn, description, author_id) VALUES (?, 'Title', 'ISBN-DESC', 'Old Description', 2000)", bookId);

        // when
        bookStatisticsDao.updateBookDescriptionRaw(bookId, "New Description");

        // then
        String newDesc = jdbcTemplate.queryForObject("SELECT description FROM books WHERE id = ?", String.class, bookId);
        assertThat(newDesc).isEqualTo("New Description");
    }

    // aktualizacja opisu książki (Błąd - książka nie istnieje)
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentBook() {
        // given
        long nonExistentId = 99999L;

        // when
        Throwable thrown = catchThrowable(() -> 
            bookStatisticsDao.updateBookDescriptionRaw(nonExistentId, "New Desc")
        );

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie znaleziono książki o ID: " + nonExistentId);
    }

    // usuwanie wszystkich recenzji użytkownika (Raw SQL)
    @Test
    void shouldDeleteAllReviewsByUserId() {
        // given
        long userIdToDelete = 500L;
        long otherUserId = 501L;

        // Setup danych: 2 userów, 1 autor, 1 książka
        jdbcTemplate.update("INSERT INTO users (id, username, email, password, role, enabled) VALUES (?, 'delUser', 'del@test.pl', 'p', 'USER', true)", userIdToDelete);
        jdbcTemplate.update("INSERT INTO users (id, username, email, password, role, enabled) VALUES (?, 'safeUser', 'safe@test.pl', 'p', 'USER', true)", otherUserId);
        jdbcTemplate.update("INSERT INTO authors (id, first_name, last_name) VALUES (5000, 'A', 'B')");
        jdbcTemplate.update("INSERT INTO books (id, title, isbn, author_id) VALUES (5000000, 'B', 'I', 5000)");

        // 2 recenzje dla usera do usunięcia
        jdbcTemplate.update("INSERT INTO reviews (rating, content, book_id, user_id) VALUES (5, 'A', 5000000, ?)", userIdToDelete);
        jdbcTemplate.update("INSERT INTO reviews (rating, content, book_id, user_id) VALUES (4, 'B', 5000000, ?)", userIdToDelete);

        // 1 recenzja dla innego usera (powinna zostać)
        jdbcTemplate.update("INSERT INTO reviews (rating, content, book_id, user_id) VALUES (3, 'C', 5000000, ?)", otherUserId);

        // when
        bookStatisticsDao.deleteAllReviewsByUserId(userIdToDelete);

        // then
        Integer countDeletedUser = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reviews WHERE user_id = ?", Integer.class, userIdToDelete);
        Integer countOtherUser = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reviews WHERE user_id = ?", Integer.class, otherUserId);

        assertThat(countDeletedUser).isEqualTo(0); // Recenzje tego usera zniknęły
        assertThat(countOtherUser).isEqualTo(1);   // Recenzje innego usera zostały
    }
}
