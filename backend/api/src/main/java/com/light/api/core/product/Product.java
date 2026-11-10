package com.light.api.core.product;

public class Product {

    private  int productId;
    private  String name;
    private  int weight;
    private  String brand;
    private  String category;
    private  String image;
    private  String description;
    private  float rating;
    private  float price;
    private  int countInStock;
    private String serviceAddress;

    public Product() {
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
        this.serviceAddress = null;
    }

    public Product(int productId, String name, int weight, String brand, String category, String image, String description,
                   float rating, float price, int countInStock, String serviceAddress) {
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
        this.serviceAddress = serviceAddress;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getCountInStock() {
        return countInStock;
    }

    public void setCountInStock(int countInStock) {
        this.countInStock = countInStock;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}
