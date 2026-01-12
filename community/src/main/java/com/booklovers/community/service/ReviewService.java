package com.booklovers.community.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.booklovers.community.model.Book;
import com.booklovers.community.model.Review;
import com.booklovers.community.model.User;
import com.booklovers.community.repository.BookRepository;
import com.booklovers.community.repository.ReviewRepository;
import com.booklovers.community.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public List<Review> getReviewsForBook(Long bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    @Transactional
    public void addReview(Long bookId, String username, Integer rating, String content) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        Review review = Review.builder()
                .book(book)
                .user(user)
                .rating(rating)
                .content(content)
                .build();

        reviewRepository.save(review);
    }

}
