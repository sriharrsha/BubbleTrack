package com.sriharrsha.bubbletrack.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Product {
    private String uid;
    private String imageUrl;
    private Long maxPrice;
    private Long sellingPrice;
    private Date timestamp;
    private String productName;
    private String productUrl;

    public Product() { } // Needed for Firebase

    public Product(String productName, String productUrl, String uid, String imageUrl, Long maxPrice, Long sellingPrice, Date timestamp) {
        this.productName = productName;
        this.productUrl = productUrl;
        this.uid = uid;
        this.imageUrl = imageUrl;
        this.maxPrice = maxPrice;
        this.sellingPrice = sellingPrice;
        this.timestamp = timestamp;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Long maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Long getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(Long sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    @ServerTimestamp
    public Date getTimestamp() { return timestamp; }

    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
