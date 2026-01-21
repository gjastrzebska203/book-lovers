package com.booklovers.community.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
// import java.util.Optional;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.booklovers.community.model.Author;
import com.booklovers.community.model.Book;
import com.booklovers.community.model.Review;
import com.booklovers.community.model.User;
import com.booklovers.community.repository.AuthorRepository;
import com.booklovers.community.repository.BookRepository;
import com.booklovers.community.repository.ReviewRepository;
import com.booklovers.community.repository.UserRepository;

import jakarta.persistence.EntityManager;

// import jakarta.persistence.EntityManager;

@DataJpaTest
public class ReviewRepositoryTest {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private EntityManager entityManager;

    // pobieranie recenzji dla konkretnej książki
    @Test
    void shouldFindReviewsByBookId() {
        // given
        Author author = authorRepository.save(Author.builder().firstName("J.R.R.").lastName("Tolkien").build());
        
        Book bookTarget = bookRepository.save(Book.builder()
                .title("Hobbit")
                .isbn("9780261102217")
                .author(author)
                .build());

        Book bookOther = bookRepository.save(Book.builder()
                .title("Władca pierścieni")
                .isbn("9780007129706")
                .author(author)
                .build());

        User user = userRepository.save(User.builder()
                .username("frodo")
                .email("frodo@shire.pl")
                .password("pass")
                .role("USER")
                .build());

        Review review1 = Review.builder().rating(10).content("Super").book(bookTarget).user(user).build();
        Review review2 = Review.builder().rating(8).content("Dobra").book(bookTarget).user(user).build();
        Review reviewOther = Review.builder().rating(5).content("Średnia").book(bookOther).user(user).build();

        reviewRepository.saveAll(List.of(review1, review2, reviewOther));

        // when
        List<Review> reviews = reviewRepository.findByBookId(bookTarget.getId());

        // then
        assertThat(reviews).hasSize(2); // Powinny być tylko 2 recenzje dla "Hobbita"
        assertThat(reviews).extracting(Review::getContent).contains("Super", "Dobra");
    }

    // obliczanie średniej oceny (@Query)
    @Test
    void shouldCalculateAverageRatingForBook() {
        // given
        Author author = authorRepository.save(Author.builder().firstName("Test").lastName("Author").build());
        Book book = bookRepository.save(Book.builder().title("Test Book").isbn("9780007129706").author(author).build());
        User user = userRepository.save(User.builder().username("tester").email("t@t.pl").password("p").role("ROLE_USER").build());

        // Dodajemy oceny: 10 i 6. Średnia powinna wyjść 8.0
        reviewRepository.save(Review.builder().rating(10).content("Ekstra").book(book).user(user).build());
        reviewRepository.save(Review.builder().rating(6).content("Spoko").book(book).user(user).build());

        // when
        Double average = reviewRepository.getAverageRatingForBook(book.getId());

        // then
        assertThat(average).isEqualTo(8.0);
    }

    // średnia dla książki bez recenzji
    @Test
    void shouldReturnNullAverageWhenNoReviews() {
        // given
        Author author = authorRepository.save(Author.builder().firstName("Empty").lastName("Book").build());
        Book book = bookRepository.save(Book.builder().title("Nieoceniona").isbn("9780007129706").author(author).build());

        // when
        Double average = reviewRepository.getAverageRatingForBook(book.getId());

        // then
        assertThat(average).isNull();
    }

    // anonimizacja recenzji (ustawienie user_id na NULL)
    @Test
    void shouldAnonymizeReviewsByUserId() {
        // given
        Author author = authorRepository.save(Author.builder().firstName("ABC").lastName("BCD").build());
        Book book = bookRepository.save(Book.builder().title("Tytuł").isbn("9780261102217").author(author).build());
        User user = userRepository.save(User.builder().username("deleteMe").email("d@d.pl").password("p").role("USER").build());

        Review review = Review.builder().rating(5).content("Delete me").book(book).user(user).build();
        reviewRepository.save(review);
        Long reviewId = review.getId();

        // when
        reviewRepository.anonymizeReviewsByUserId(user.getId());

        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Review> updatedReview = reviewRepository.findById(reviewId);
        assertThat(updatedReview).isPresent();
        assertThat(updatedReview.get().getUser()).isNull(); // Użytkownik powinien być NULL
        assertThat(updatedReview.get().getContent()).isEqualTo("Delete me"); // Treść zostaje
    }

    // pobieranie wszystkich recenzji użytkownika
    @Test
    void shouldFindAllReviewsByUserId() {
        // given
        Author author = authorRepository.save(Author.builder().firstName("ABC").lastName("BCD").build());
        Book book = bookRepository.save(Book.builder().title("Tytuł").isbn("9780261102217").author(author).build());
        
        User user1 = userRepository.save(User.builder().username("u12").email("u1@x.pl").password("p").role("USER").build());
        User user2 = userRepository.save(User.builder().username("u23").email("u2@x.pl").password("p").role("USER").build());

        reviewRepository.save(Review.builder().content("null1").rating(5).book(book).user(user1).build());
        reviewRepository.save(Review.builder().content("null2").rating(4).book(book).user(user1).build());
        reviewRepository.save(Review.builder().content("null3").rating(1).book(book).user(user2).build());

        // when
        List<Review> user1Reviews = reviewRepository.findAllByUserId(user1.getId());

        // then
        assertThat(user1Reviews).hasSize(2);
        assertThat(user1Reviews).extracting(Review::getRating).contains(5, 4);
    }

    // rozkład ocen (Histogram)
    @Test
    void shouldGetRatingDistribution() {
        // given
        Author author = authorRepository.save(Author.builder().firstName("AB").lastName("BC").build());
        Book book = bookRepository.save(Book.builder().title("StatBook").isbn("9780261102217").author(author).build());
        User user = userRepository.save(User.builder().username("u123").email("u@u.pl").password("p").role("USER").build());

        // 3 razy ocena 10
        reviewRepository.save(Review.builder().content("null1").rating(10).book(book).user(user).build());
        reviewRepository.save(Review.builder().content("null1").rating(10).book(book).user(user).build());
        reviewRepository.save(Review.builder().content("null1").rating(10).book(book).user(user).build());
        
        // 1 raz ocena 1
        reviewRepository.save(Review.builder().content("null1").rating(1).book(book).user(user).build());

        // when
        List<Object[]> distribution = reviewRepository.getRatingDistribution(book.getId());

        // then
        assertThat(distribution).hasSize(2);
        
        boolean foundTen = false;
        boolean foundOne = false;

        for (Object[] row : distribution) {
            Integer rating = ((Number) row[0]).intValue();
            Long count = ((Number) row[1]).longValue();

            if (rating == 10) {
                assertThat(count).isEqualTo(3L);
                foundTen = true;
            } else if (rating == 1) {
                assertThat(count).isEqualTo(1L);
                foundOne = true;
            }
        }
        
        assertThat(foundTen).isTrue();
        assertThat(foundOne).isTrue();
    }

    // liczenie recenzji dla książki (Count)
    @Test
    void shouldCountReviewsByBookId() {
        // given
        Author author = authorRepository.save(Author.builder().firstName("AB").lastName("BA").build());
        Book book = bookRepository.save(Book.builder().title("CountMe").isbn("9780261102217").author(author).build());
        User user = userRepository.save(User.builder().username("u123").email("u@u.pl").password("p").role("USER").build());

        reviewRepository.save(Review.builder().content("null1").rating(5).book(book).user(user).build());
        reviewRepository.save(Review.builder().content("null1").rating(5).book(book).user(user).build());

        // when
        long count = reviewRepository.countByBookId(book.getId());

        // then
        assertThat(count).isEqualTo(2L);
    }
}
