package com.light.microservices.core.review.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.light.api.core.review.Review;
import com.light.api.exceptions.InvalidInputException;
import com.light.microservices.core.review.persistence.ReviewEntity;
import com.light.microservices.core.review.persistence.ReviewRepository;
import com.light.util.http.ServiceUtil;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Unit tests for {@link ReviewServiceImpl} with mocked repository, mapper and
 * {@link ServiceUtil}. Covers create (incl. data-integrity-violation translation),
 * the productId validation on get, the address-decorated list mapping and the two
 * delete variants (bulk vs. single, the latter filtering by reviewId).
 */
class ReviewServiceImplTest {

    private ReviewRepository repository;
    private ReviewMapper mapper;
    private ServiceUtil serviceUtil;
    private ReviewServiceImpl service;

    private Review api(int reviewId) {
        return new Review(1, reviewId, "author", "subject", "content", null);
    }

    @BeforeEach
    void setup() {
        repository = mock(ReviewRepository.class);
        mapper = mock(ReviewMapper.class);
        serviceUtil = mock(ServiceUtil.class);
        when(serviceUtil.getServiceAddress()).thenReturn("review-addr");
        service = new ReviewServiceImpl(repository, mapper, serviceUtil);
    }

    @Test
    void createReviewPersistsAndReturnsApi() {
        ReviewEntity entity = new ReviewEntity(1, 1, "a", "s", "c");
        Review result = api(1);
        when(mapper.apiToEntity(any())).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.entityToApi(entity)).thenReturn(result);

        assertSame(result, service.createReview(api(1)));
    }

    @Test
    void createReviewTranslatesDataIntegrityViolationToInvalidInput() {
        when(mapper.apiToEntity(any())).thenReturn(new ReviewEntity(1, 1, "a", "s", "c"));
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("dup"));

        assertThrows(InvalidInputException.class, () -> service.createReview(api(1)));
    }

    @Test
    void getReviewsRejectsNonPositiveId() {
        assertThrows(InvalidInputException.class, () -> service.getReviews(0));
    }

    @Test
    void getReviewsReturnsListWithServiceAddress() {
        ReviewEntity entity = new ReviewEntity(1, 1, "a", "s", "c");
        when(repository.findByProductId(1)).thenReturn(Arrays.asList(entity));
        List<Review> mapped = Arrays.asList(api(1));
        when(mapper.entityListToApiList(any())).thenReturn(mapped);

        List<Review> result = service.getReviews(1);

        assertEquals(1, result.size());
        assertEquals("review-addr", result.get(0).getServiceAddress());
    }

    @Test
    void deleteReviewsDeletesAllForProduct() {
        List<ReviewEntity> entities = Arrays.asList(
                new ReviewEntity(1, 1, "a", "s", "c"),
                new ReviewEntity(1, 2, "a", "s", "c"));
        when(repository.findByProductId(1)).thenReturn(entities);

        service.deleteReviews(1);

        verify(repository).deleteAll(entities);
    }

    @Test
    void deleteReviewDeletesOnlyMatchingId() {
        ReviewEntity keep = new ReviewEntity(1, 1, "a", "s", "c");
        ReviewEntity remove = new ReviewEntity(1, 7, "a", "s", "c");
        when(repository.findByProductId(1)).thenReturn(Arrays.asList(keep, remove));

        service.deleteReview(1, 7);

        verify(repository).delete(remove);
        verify(repository, never()).delete(keep);
    }
}
