package com.light.microservices.composite.product.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.light.api.core.product.Product;
import com.light.api.core.recommendation.Recommendation;
import com.light.api.core.review.Review;
import com.light.api.exceptions.InvalidInputException;
import com.light.api.exceptions.NotFoundException;
import com.light.util.http.HttpErrorInfo;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Unit tests for {@link ProductCompositeIntegration}. The downstream HTTP calls are
 * exercised against a mocked {@link RestTemplate}, covering the happy paths, the
 * {@code HttpClientErrorException} translation (404 -> NotFound, 422 -> InvalidInput,
 * other -> rethrow) and the resilience fallback that returns an empty list when
 * recommendations / reviews fail.
 */
class ProductCompositeIntegrationTest {

    private RestTemplate restTemplate;
    private ObjectMapper mapper;
    private ProductCompositeIntegration integration;

    @BeforeEach
    void setup() {
        restTemplate = mock(RestTemplate.class);
        mapper = mock(ObjectMapper.class);
        integration = new ProductCompositeIntegration(
                restTemplate, mapper,
                "product", 7001,
                "recommendation", 7002,
                "review", 7003);
    }

    private Product product(int id) {
        return new Product(id, "name", 1, "brand", "cat", "img", "desc", 4, 9.0f, 5, null);
    }

    // ---------- product happy paths ----------

    @Test
    void createProductDelegatesToPost() {
        Product body = product(1);
        when(restTemplate.postForObject(contains("/product"), eq(body), eq(Product.class)))
                .thenReturn(body);

        assertSame(body, integration.createProduct(body));
    }

    @Test
    void getProductReturnsBody() {
        Product p = product(1);
        when(restTemplate.getForObject(contains("/product/1"), eq(Product.class))).thenReturn(p);

        assertSame(p, integration.getProduct(1));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllProductsReturnsBody() {
        List<Product> products = Arrays.asList(product(1), product(2));
        when(restTemplate.exchange(anyString(), eq(GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(products, HttpStatus.OK));

        assertEquals(2, integration.getAllProducts().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchByNameBuildsUrlWithEncodedQueryParam() {
        when(restTemplate.exchange(anyString(), eq(GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(Arrays.asList(product(1)), HttpStatus.OK));

        integration.searchByName("phone case");

        ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(url.capture(), eq(GET), isNull(), any(ParameterizedTypeReference.class));
        assertTrue(url.getValue().contains("/product/search"), url.getValue());
        assertTrue(url.getValue().contains("name=phone"), url.getValue());
    }

    @Test
    void deleteProductCallsDelete() {
        integration.deleteProduct(5);
        verify(restTemplate).delete(contains("/product/5"));
    }

    // ---------- error translation (via getProduct) ----------

    @Test
    void getProductTranslatesNotFoundAndUsesParsedMessage() throws Exception {
        when(restTemplate.getForObject(anyString(), eq(Product.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        when(mapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "/product/9", "No product found"));

        NotFoundException ex = assertThrows(NotFoundException.class, () -> integration.getProduct(9));
        assertEquals("No product found", ex.getMessage());
    }

    @Test
    void getProductTranslatesUnprocessableEntityToInvalidInput() throws Exception {
        when(restTemplate.getForObject(anyString(), eq(Product.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY));
        when(mapper.readValue(anyString(), eq(HttpErrorInfo.class)))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "/product/9", "bad input"));

        assertThrows(InvalidInputException.class, () -> integration.getProduct(9));
    }

    @Test
    void getProductRethrowsUnexpectedHttpError() {
        when(restTemplate.getForObject(anyString(), eq(Product.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_GATEWAY));

        assertThrows(HttpClientErrorException.class, () -> integration.getProduct(9));
    }

    // ---------- recommendations ----------

    @Test
    @SuppressWarnings("unchecked")
    void getRecommendationsReturnsBody() {
        List<Recommendation> recs = Arrays.asList(new Recommendation(1, 1, "a", 5, "c", null));
        when(restTemplate.exchange(anyString(), eq(GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(recs, HttpStatus.OK));

        assertEquals(1, integration.getRecommendations(1).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getRecommendationsReturnsEmptyListOnError() {
        when(restTemplate.exchange(anyString(), eq(GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("downstream unavailable"));

        assertTrue(integration.getRecommendations(1).isEmpty());
    }

    @Test
    void createRecommendationDelegatesToPost() {
        Recommendation body = new Recommendation(1, 1, "a", 5, "c", null);
        when(restTemplate.postForObject(contains("/recommendation"), eq(body), eq(Recommendation.class)))
                .thenReturn(body);

        assertSame(body, integration.createRecommendation(body));
    }

    @Test
    void deleteRecommendationsCallsDelete() {
        integration.deleteRecommendations(3);
        verify(restTemplate).delete(contains("productId=3"));
    }

    @Test
    void deleteRecommendationCallsDelete() {
        integration.deleteRecommendation(3, 7);
        verify(restTemplate).delete(contains("/recommendation/3/7"));
    }

    // ---------- reviews ----------

    @Test
    @SuppressWarnings("unchecked")
    void getReviewsReturnsBody() {
        List<Review> reviews = Arrays.asList(new Review(1, 1, "a", "s", "c", null));
        when(restTemplate.exchange(anyString(), eq(GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(reviews, HttpStatus.OK));

        assertEquals(1, integration.getReviews(1).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getReviewsReturnsEmptyListOnError() {
        when(restTemplate.exchange(anyString(), eq(GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("downstream unavailable"));

        assertTrue(integration.getReviews(1).isEmpty());
    }

    @Test
    void createReviewDelegatesToPost() {
        Review body = new Review(1, 1, "a", "s", "c", null);
        when(restTemplate.postForObject(contains("/review"), eq(body), eq(Review.class)))
                .thenReturn(body);

        assertSame(body, integration.createReview(body));
    }

    @Test
    void deleteReviewsCallsDelete() {
        integration.deleteReviews(3);
        verify(restTemplate).delete(contains("productId=3"));
    }

    @Test
    void deleteReviewCallsDelete() {
        integration.deleteReview(3, 7);
        verify(restTemplate).delete(contains("/review/3/7"));
    }
}
