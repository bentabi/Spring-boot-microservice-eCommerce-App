package com.light.api.composite.product;

import java.util.List;

public class ProductAggregate {
    private final int productId;
    private final String name;
    private final int weight;
    private final String brand;
    private final String category;
    private final String image;
    private final String description;
    private final float rating;
    private final float price;
    private final int countInStock;


    private final List<RecommendationSummary> recommendations;
    private final List<ReviewSummary> reviews;
    private final ServiceAddresses serviceAddresses;


    public ProductAggregate(int productId, String name, int weight, String brand, String category,
                            String image, String description, float rating, float price,
                            int countInStock, List<RecommendationSummary> recommendations,
                            List<ReviewSummary> reviews, ServiceAddresses serviceAddresses) {
        this.productId = productId;
        this.name = name;
        this.weight = weight;
        this.brand = brand;
        this.category = category;
        this.image = image;
        this.description = description;
        this.rating = rating;
        this.price = price;
        this.countInStock = countInStock;
        this.recommendations = recommendations;
        this.reviews = reviews;
        this.serviceAddresses = serviceAddresses;
    }

    public ProductAggregate() {
        productId=0;
        name=null;
        weight = 0;
        this.brand = null;
        this.category = null;
        this.image = null;
        this.description = null;
        this.rating = 0;
        this.price = 0;
        this.countInStock = 0;
        this.recommendations = null;
        this.reviews = null;
        this.serviceAddresses = null;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public String getBrand() {
        return brand;
    }

    public String getCategory() {
        return category;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public float getRating() {
        return rating;
    }

    public float getPrice() {
        return price;
    }

    public int getCountInStock() {
        return countInStock;
    }

    public List<RecommendationSummary> getRecommendations() {
        return recommendations;
    }

    public List<ReviewSummary> getReviews() {
        return reviews;
    }

    public ServiceAddresses getServiceAddresses() {
        return serviceAddresses;
    }
}
