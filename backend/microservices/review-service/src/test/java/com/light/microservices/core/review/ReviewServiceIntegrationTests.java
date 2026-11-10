package com.light.microservices.core.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.light.api.core.review.Review;
import com.light.api.core.review.ReviewService;
import com.light.api.exceptions.InvalidInputException;
import com.light.microservices.core.review.persistence.ReviewRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests driving the real {@code ReviewServiceImpl} against a MySQL
 * Testcontainer. These exercise the generated MapStruct {@code ReviewMapperImpl},
 * the JPA {@code ReviewEntity}, and the repository (incl. the unique-index
 * data-integrity-violation duplicate-key path).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ReviewServiceIntegrationTests extends MySqlTestBase {

    @Autowired
    private ReviewService service;

    @Autowired
    private ReviewRepository repository;

    @BeforeEach
    void clearDatabase() {
        repository.deleteAll();
    }

    private Review review(int productId, int reviewId) {
        return new Review(productId, reviewId, "author", "subject", "content", null);
    }

    @Test
    void createAndGetRoundTrip() {
        service.createReview(review(1, 1));

        List<Review> reviews = service.getReviews(1);
        assertEquals(1, reviews.size());
        assertEquals(1, reviews.get(0).getReviewId());
        assertEquals("subject", reviews.get(0).getSubject());
        assertEquals("author", reviews.get(0).getAuthor());
        assertNotNull(reviews.get(0).getServiceAddress());
    }

    @Test
    void duplicateProductAndReviewIdThrowsInvalidInput() {
        service.createReview(review(1, 1));
        assertThrows(InvalidInputException.class, () -> service.createReview(review(1, 1)));
    }

    @Test
    void getReviewsRejectsNonPositiveId() {
        assertThrows(InvalidInputException.class, () -> service.getReviews(0));
    }

    @Test
    void getReviewsReturnsEmptyForUnknownProduct() {
        assertTrue(service.getReviews(999).isEmpty());
    }

    @Test
    void deleteReviewsRemovesAllForProduct() {
        service.createReview(review(1, 1));
        service.createReview(review(1, 2));

        service.deleteReviews(1);

        assertTrue(service.getReviews(1).isEmpty());
    }

    @Test
    void deleteReviewRemovesOnlyTheMatchingOne() {
        service.createReview(review(1, 1));
        service.createReview(review(1, 2));

        service.deleteReview(1, 1);

        List<Review> remaining = service.getReviews(1);
        assertEquals(1, remaining.size());
        assertEquals(2, remaining.get(0).getReviewId());
    }
}
