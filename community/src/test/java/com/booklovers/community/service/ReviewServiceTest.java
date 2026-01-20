package com.booklovers.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.booklovers.community.model.Book;
import com.booklovers.community.model.Review;
import com.booklovers.community.model.User;
import com.booklovers.community.repository.BookRepository;
import com.booklovers.community.repository.ReviewRepository;
import com.booklovers.community.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ReviewService reviewService;

    // pobieranie recenzji dla konkretnej książki 
    @Test
    void shouldReturnReviewsForSpecificBook() {
        // given
        Long bookId = 100L;
        Review r1 = Review.builder().content("Super").build();
        Review r2 = Review.builder().content("Słaba").build();
        
        when(reviewRepository.findByBookId(bookId)).thenReturn(List.of(r1, r2));

        // when
        List<Review> result = reviewService.getReviewsForBook(bookId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).contains(r1, r2);
    }

    // dodawanie recenzji (Sukces)
    @Test
    void shouldAddReviewSuccessfully() {
        // given
        Long bookId = 1L;
        String username = "recenzent";
        Integer rating = 8;
        String content = "Bardzo dobra książka";

        User user = User.builder().username(username).build();
        Book book = Book.builder().id(bookId).title("Tytuł").build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // when
        reviewService.addReview(bookId, username, rating, content);

        // then
        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());

        Review savedReview = captor.getValue();
        assertThat(savedReview.getUser()).isEqualTo(user);
        assertThat(savedReview.getBook()).isEqualTo(book);
        assertThat(savedReview.getRating()).isEqualTo(rating);
        assertThat(savedReview.getContent()).isEqualTo(content);
    }

    // błąd przy dodawaniu - użytkownik nie istnieje 
    @Test
    void shouldThrowExceptionWhenUserNotFoundAddingReview() {
        // given
        String username = "duch";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> 
            reviewService.addReview(1L, username, 5, "Opis")
        );

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
        
        verify(bookRepository, never()).findById(any());
        verify(reviewRepository, never()).save(any());
    }

    // błąd przy dodawaniu - książka nie istnieje 
    @Test
    void shouldThrowExceptionWhenBookNotFoundAddingReview() {
        // given
        Long bookId = 999L;
        String username = "istniejacy_user";
        User user = User.builder().username(username).build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> 
            reviewService.addReview(bookId, username, 5, "Opis")
        );

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessage("Book not found");
        
        verify(reviewRepository, never()).save(any());
    }

    // pobieranie wszystkich recenzji
    @Test
    void shouldGetAllReviews() {
        // given
        when(reviewRepository.findAll()).thenReturn(List.of(new Review(), new Review()));

        // when
        List<Review> result = reviewService.getAllReviews();

        // then
        assertThat(result).hasSize(2);
    }

    // usuwanie recenzji
    @Test
    void shouldDeleteReviewById() {
        // given
        Long reviewId = 55L;

        // when
        reviewService.deleteReview(reviewId);

        // then
        verify(reviewRepository).deleteById(reviewId);
    }

    // test błędu zapisu (np. błąd bazy danych)
    @Test
    void shouldThrowExceptionWhenDatabaseFailsToSaveReview() {
        // given
        Long bookId = 1L;
        String username = "user";
        Integer rating = 5;
        String content = "Tekst";

        User user = new User();
        Book book = new Book();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        
        when(reviewRepository.save(any(Review.class))).thenThrow(new RuntimeException("DB Error"));

        // when
        Throwable thrown = catchThrowable(() -> 
            reviewService.addReview(bookId, username, rating, content)
        );

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessage("DB Error");
    }
}
