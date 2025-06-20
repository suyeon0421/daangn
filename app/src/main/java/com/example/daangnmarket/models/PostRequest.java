package com.example.daangnmarket.models;

import com.google.gson.annotations.SerializedName;

public class PostRequest {
    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private int price;

    @SerializedName("seller_id")
    private int sellerId;


    @SerializedName("location_name")
    private String locationName;

    public PostRequest(String title, String description, int price, int sellerId, String locationName) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.sellerId = sellerId;
        this.locationName = locationName;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getPrice() { return price; }
    public int getSellerId() { return sellerId; }
    public String getLocationName() { return locationName; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public void setPrice(int price) {
        this.price = price;
    }
    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }


}
