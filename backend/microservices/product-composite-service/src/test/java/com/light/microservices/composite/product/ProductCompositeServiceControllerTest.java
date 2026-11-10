package com.light.microservices.composite.product;

import com.light.api.composite.product.ProductAggregate;
import com.light.api.composite.product.RecommendationSummary;
import com.light.api.composite.product.ReviewSummary;
import com.light.api.core.product.Product;
import com.light.api.core.recommendation.Recommendation;
import com.light.api.core.review.Review;
import com.light.api.exceptions.NotFoundException;
import com.light.microservices.composite.product.services.ProductCompositeIntegration;
import com.light.microservices.composite.product.services.ProductCompositeServiceController;
import com.light.util.http.ServiceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ProductCompositeServiceController. Covers the aggregate mapping
 * (real fields, not hardcoded), create fan-out, the server-assigned id logic in
 * addReview / addRecommendation, and the delete delegations.
 */
class ProductCompositeServiceControllerTest {

    private ProductCompositeIntegration integration;
    private ProductCompositeServiceController controller;

    private Product product(int id, String name) {
        return new Product(id, name, 2, "Acme", "Tools", "img", "desc", 4, 9.0f, 5, "prod-addr");
    }

    @BeforeEach
    void setup() {
        integration = mock(ProductCompositeIntegration.class);
        ServiceUtil serviceUtil = mock(ServiceUtil.class);
        when(serviceUtil.getServiceAddress()).thenReturn("composite-addr");
        controller = new ProductCompositeServiceController(serviceUtil, integration);
    }

    // ---------- createProduct ----------

    @Test
    void createProductFansOutToProductReviewAndRecommendation() {
        List<RecommendationSummary> recs = Arrays.asList(new RecommendationSummary(1, "a", 5, "c"));
        List<ReviewSummary> revs = Arrays.asList(new ReviewSummary(1, "a", "s", "c"));
        ProductAggregate body = new ProductAggregate(50, "New", 1, "Acme", "Tools",
                "img", "desc", 4, 9.0f, 5, recs, revs, null);

        controller.createProduct(body);

        ArgumentCaptor<Product> cap = ArgumentCaptor.forClass(Product.class);
        verify(integration).createProduct(cap.capture());
        assertEquals(50, cap.getValue().getProductId());
        verify(integration).createRecommendation(any(Recommendation.class));
        verify(integration).createReview(any(Review.class));
    }

    @Test
    void createProductWithNoChildrenOnlyCreatesProduct() {
        ProductAggregate body = new ProductAggregate(51, "Bare", 1, "Acme", "Tools",
                "img", "desc", 4, 9.0f, 5, null, null, null);

        controller.createProduct(body);

        verify(integration).createProduct(any(Product.class));
    }

    // ---------- getProduct ----------

    @Test
    void getProductReturnsAggregateWithRealFieldsAndChildren() {
        when(integration.getProduct(1)).thenReturn(product(1, "Widget"));
        when(integration.getRecommendations(1)).thenReturn(
                Arrays.asList(new Recommendation(1, 1, "auth", 5, "c", "rec-addr")));
        when(integration.getReviews(1)).thenReturn(
                Arrays.asList(new Review(1, 1, "auth", "subj", "c", "rev-addr")));

        ProductAggregate agg = controller.getProduct(1);

        assertEquals(1, agg.getProductId());
        assertEquals("Acme", agg.getBrand());      // real field, not hardcoded
        assertEquals("Tools", agg.getCategory());
        assertEquals(1, agg.getRecommendations().size());
        assertEquals(1, agg.getReviews().size());
    }

    @Test
    void getProductThrowsNotFoundWhenMissing() {
        when(integration.getProduct(999)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> controller.getProduct(999));
    }

    // ---------- getAllProducts / searchProducts ----------

    @Test
    void getAllProductsMapsToAggregates() {
        when(integration.getAllProducts()).thenReturn(
                Arrays.asList(product(1, "A"), product(2, "B")));

        List<ProductAggregate> result = controller.getAllProducts();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getProductId());
        assertEquals("Acme", result.get(0).getBrand());
    }

    @Test
    void searchProductsMapsToAggregates() {
        when(integration.searchByName("ph")).thenReturn(
                Arrays.asList(product(1, "Phone")));

        List<ProductAggregate> result = controller.searchProducts("ph");

        assertEquals(1, result.size());
        assertEquals("Phone", result.get(0).getName());
    }

    // ---------- addReview / addRecommendation (next-id logic) ----------

    @Test
    void addReviewAssignsNextReviewIdAsMaxPlusOne() {
        when(integration.getReviews(100)).thenReturn(Arrays.asList(
                new Review(100, 1, "a", "s", "c", null),
                new Review(100, 2, "a", "s", "c", null)));

        controller.addReview(100, new ReviewSummary(0, "Eve", "Nice", "Good"));

        ArgumentCaptor<Review> cap = ArgumentCaptor.forClass(Review.class);
        verify(integration).createReview(cap.capture());
        assertEquals(3, cap.getValue().getReviewId());
        assertEquals(100, cap.getValue().getProductId());
        assertEquals("Eve", cap.getValue().getAuthor());
    }

    @Test
    void addReviewAssignsIdOneWhenNoExistingReviews() {
        when(integration.getReviews(5)).thenReturn(Collections.emptyList());

        controller.addReview(5, new ReviewSummary(0, "Eve", "Nice", "Good"));

        ArgumentCaptor<Review> cap = ArgumentCaptor.forClass(Review.class);
        verify(integration).createReview(cap.capture());
        assertEquals(1, cap.getValue().getReviewId());
    }

    @Test
    void addRecommendationAssignsNextIdAsMaxPlusOne() {
        when(integration.getRecommendations(100)).thenReturn(Arrays.asList(
                new Recommendation(100, 1, "a", 5, "c", null),
                new Recommendation(100, 7, "a", 4, "c", null)));

        controller.addRecommendation(100, new RecommendationSummary(0, "Frank", 5, "Great"));

        ArgumentCaptor<Recommendation> cap = ArgumentCaptor.forClass(Recommendation.class);
        verify(integration).createRecommendation(cap.capture());
        assertEquals(8, cap.getValue().getRecommendationId()); // max(1,7)+1
        assertEquals(5, cap.getValue().getRate());
        assertEquals("Frank", cap.getValue().getAuthor());
    }

    @Test
    void addRecommendationAssignsIdOneWhenNoExisting() {
        when(integration.getRecommendations(5)).thenReturn(Collections.emptyList());

        controller.addRecommendation(5, new RecommendationSummary(0, "Frank", 5, "Great"));

        ArgumentCaptor<Recommendation> cap = ArgumentCaptor.forClass(Recommendation.class);
        verify(integration).createRecommendation(cap.capture());
        assertEquals(1, cap.getValue().getRecommendationId());
    }

    // ---------- deletes ----------

    @Test
    void deleteProductDeletesProductReviewsAndRecommendations() {
        controller.deleteProduct(7);
        verify(integration).deleteProduct(7);
        verify(integration).deleteRecommendations(7);
        verify(integration).deleteReviews(7);
    }

    @Test
    void deleteReviewDelegatesToIntegration() {
        controller.deleteReview(100, 3);
        verify(integration).deleteReview(100, 3);
    }

    @Test
    void deleteRecommendationDelegatesToIntegration() {
        controller.deleteRecommendation(100, 3);
        verify(integration).deleteRecommendation(100, 3);
    }
}
