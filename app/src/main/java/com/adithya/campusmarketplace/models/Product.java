package com.adithya.campusmarketplace.models;

import java.io.Serializable;

/**
 * Product model representing campus marketplace items (Buy, Sell, Exchange, Donate).
 * Stored in Firestore collection: "products"
 */
public class Product implements Serializable {
    private String productId;
    private String sellerId;
    private String title;
    private String description;
    private double price;
    private String category;
    private String condition;
    private String sellerName;
    private String phone;
    private long createdAt;
    private boolean isWishlisted;
    private String imageBase64; // Stored compressed image Base64 string

    // Required empty constructor for Firestore deserialization
    public Product() {
    }

    public Product(String productId, String sellerId, String title, String description,
                   double price, String category, String condition, String sellerName,
                   String phone, long createdAt) {
        this.productId = productId;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.sellerName = sellerName;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public Product(String productId, String sellerId, String title, String description,
                   double price, String category, String condition, String sellerName,
                   String phone, long createdAt, String imageBase64) {
        this.productId = productId;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.sellerName = sellerName;
        this.phone = phone;
        this.createdAt = createdAt;
        this.imageBase64 = imageBase64;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isWishlisted() {
        return isWishlisted;
    }

    public void setWishlisted(boolean wishlisted) {
        isWishlisted = wishlisted;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}
