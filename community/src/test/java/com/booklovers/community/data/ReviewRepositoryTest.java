package com.booklovers.community.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

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
}
