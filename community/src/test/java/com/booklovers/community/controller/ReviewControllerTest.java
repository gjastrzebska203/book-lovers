package com.booklovers.community.controller;

import com.booklovers.community.dto.CreateReviewRequest;
import com.booklovers.community.model.Review;
import com.booklovers.community.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReviewController.class)
@AutoConfigureMockMvc(addFilters = false) 
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    private Review review1;
    private Review review2;

    @BeforeEach
    void setUp() {
        review1 = new Review();
        review1.setId(1L);
        review1.setRating(5);
        review1.setContent("Super książka!");

        review2 = new Review();
        review2.setId(2L);
        review2.setRating(3);
        review2.setContent("Taka sobie.");
    }

    // --- TESTY PUBLICZNE ---

    @Test
    void shouldReturnReviewsForBook() throws Exception {
        // given
        Long bookId = 100L;
        List<Review> reviews = Arrays.asList(review1, review2);
        when(reviewService.getReviewsForBook(bookId)).thenReturn(reviews);

        // when & then
        mockMvc.perform(get("/api/v1/books/{bookId}/reviews", bookId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].content").value("Super książka!"));
    }

    @Test
    void shouldAddReviewSuccessfullyWhenUserIsLoggedIn() throws Exception {
        // given
        Long bookId = 100L;
        String username = "testUser";

        CreateReviewRequest request = new CreateReviewRequest();
        request.setRating(8);
        request.setContent("Bardzo wciągająca lektura.");

        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(username);

        // when & then
        mockMvc.perform(post("/api/v1/books/{bookId}/reviews", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(mockPrincipal)) 
                .andExpect(status().isCreated()) 
                .andExpect(content().string("Recenzja została dodana"));

        verify(reviewService, times(1))
                .addReview(eq(bookId), eq(username), eq(8), eq("Bardzo wciągająca lektura."));
    }

    @Test
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        // given
        Long bookId = 100L;
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user");

        CreateReviewRequest invalidRequest = new CreateReviewRequest();
        invalidRequest.setRating(11); 
        invalidRequest.setContent("");

        // when & then
        mockMvc.perform(post("/api/v1/books/{bookId}/reviews", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .principal(mockPrincipal))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewService);
    }

    @Test
    void shouldReturnAllReviewsForAdmin() throws Exception {
        // given
        when(reviewService.getAllReviews()).thenReturn(Arrays.asList(review1, review2));

        // when & then
        mockMvc.perform(get("/api/v1/admin/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void shouldDeleteReviewSuccessfully() throws Exception {
        // given
        Long reviewId = 55L;
        doNothing().when(reviewService).deleteReview(reviewId);

        // when & then
        mockMvc.perform(delete("/api/v1/admin/reviews/{id}", reviewId))
                .andExpect(status().isNoContent());

        verify(reviewService, times(1)).deleteReview(reviewId);
    }
}