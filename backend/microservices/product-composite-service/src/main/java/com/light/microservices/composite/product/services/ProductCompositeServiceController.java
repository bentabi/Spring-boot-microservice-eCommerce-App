package com.light.microservices.composite.product.services;

import java.util.List;
import java.util.stream.Collectors;

import com.light.api.composite.product.*;
import com.light.api.core.product.Product;
import com.light.api.core.recommendation.Recommendation;
import com.light.api.core.review.Review;
import com.light.api.exceptions.NotFoundException;
import com.light.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ProductCompositeServiceController implements ProductCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceController.class);

    private final ServiceUtil serviceUtil;
    private ProductCompositeIntegration integration;

    @Autowired
    public ProductCompositeServiceController(
            ServiceUtil serviceUtil, ProductCompositeIntegration integration) {

        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public void createProduct(ProductAggregate body) {

        try {

            LOG.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

            Product product = new Product(body.getProductId(),body.getName(),body.getWeight(),
                    body.getBrand(),body.getCategory(),body.getImage(),body.getDescription(),
                    body.getRating(),body.getPrice(),body.getCountInStock(),null
                    );
            integration.createProduct(product);

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
                    integration.createRecommendation(recommendation);
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
                    integration.createReview(review);
                });
            }

            LOG.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());

        } catch (RuntimeException re) {
            LOG.warn("createCompositeProduct failed", re);
            throw re;
        }
    }


    @Override
    public ProductAggregate getProduct(int productId) {

        LOG.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);

        Product product = integration.getProduct(productId);
        if (product == null) {
            throw new NotFoundException("No product found for productId: " + productId);
        }

        List<Recommendation> recommendations = integration.getRecommendations(productId);

        List<Review> reviews = integration.getReviews(productId);

        LOG.debug("getCompositeProduct: aggregate entity found for productId: {}", productId);

        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    @Override
    public List<ProductAggregate> getAllProducts() {

        LOG.debug("getAllCompositeProducts: retrieving all products");

        List<ProductAggregate> aggregates = integration.getAllProducts().stream()
                .map(p -> createProductAggregate(p, null, null, serviceUtil.getServiceAddress()))
                .collect(Collectors.toList());

        LOG.debug("getAllCompositeProducts: returning {} products", aggregates.size());
        return aggregates;
    }

    @Override
    public List<ProductAggregate> searchProducts(String name) {

        LOG.debug("searchCompositeProducts: searching products for name: {}", name);

        List<ProductAggregate> aggregates = integration.searchByName(name).stream()
                .map(p -> createProductAggregate(p, null, null, serviceUtil.getServiceAddress()))
                .collect(Collectors.toList());

        LOG.debug("searchCompositeProducts: found {} products for name: {}", aggregates.size(), name);
        return aggregates;
    }

    @Override
    public void addReview(int productId, ReviewSummary body) {

        LOG.debug("addReview: adding a review to productId: {}", productId);

        // Assign the next reviewId for this product (reviewId is unique per product)
        int nextReviewId = integration.getReviews(productId).stream()
                .mapToInt(Review::getReviewId)
                .max()
                .orElse(0) + 1;

        integration.createReview(new Review(
                productId, nextReviewId, body.getAuthor(), body.getSubject(), body.getContent(), null));

        LOG.debug("addReview: created reviewId {} for productId: {}", nextReviewId, productId);
    }

    @Override
    public void addRecommendation(int productId, RecommendationSummary body) {

        LOG.debug("addRecommendation: adding a recommendation to productId: {}", productId);

        // Assign the next recommendationId for this product (unique per product)
        int nextRecommendationId = integration.getRecommendations(productId).stream()
                .mapToInt(Recommendation::getRecommendationId)
                .max()
                .orElse(0) + 1;

        integration.createRecommendation(new Recommendation(
                productId, nextRecommendationId, body.getAuthor(), body.getRate(), body.getContent(), null));

        LOG.debug("addRecommendation: created recommendationId {} for productId: {}", nextRecommendationId, productId);
    }

    @Override
    public void deleteReview(int productId, int reviewId) {
        LOG.debug("deleteReview: deleting reviewId {} from productId: {}", reviewId, productId);
        integration.deleteReview(productId, reviewId);
    }

    @Override
    public void deleteRecommendation(int productId, int recommendationId) {
        LOG.debug("deleteRecommendation: deleting recommendationId {} from productId: {}", recommendationId, productId);
        integration.deleteRecommendation(productId, recommendationId);
    }

    @Override
    public void deleteProduct(int productId) {

        LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

        integration.deleteProduct(productId);

        integration.deleteRecommendations(productId);

        integration.deleteReviews(productId);

        LOG.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);
    }

    private ProductAggregate createProductAggregate(
            Product product,
            List<Recommendation> recommendations,
            List<Review> reviews,
            String serviceAddress) {

        // 1. Setup product info — read all fields from the actual product object
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();
        String brand = product.getBrand();
        String category = product.getCategory();
        String image = product.getImage();
        String description = product.getDescription();
        float rating = product.getRating();
        float price = product.getPrice();
        int countInStock = product.getCountInStock();

        // 2. Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
                recommendations.stream()
                        .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                        .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null :
                reviews.stream()
                        .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                        .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight,
               brand,category,image,description,rating,price,countInStock,
                recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}