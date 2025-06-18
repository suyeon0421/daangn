package com.example.daangnmarket.models;

public class PostRequest {
    private String title;
    private String description;
    private int price;
    private int seller_id;
    private double latitude;
    private double longitude;
    private String location_name;
    private String image;

    public PostRequest(String title, String description, int price, int seller_id,
                       double latitude, double longitude, String location_name, String image) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.seller_id = seller_id;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.location_name = location_name;
        this.image = image;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getPrice() { return price; }
    public int getSeller_id() { return seller_id; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getLocation_name() { return location_name; }
    public String getImage() { return image; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public void setPrice(int price) {
        this.price = price;
    }
    public void setSeller_id(int seller_id) {
        this.seller_id = seller_id;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }
    public void setImage(String image) {
        this.image = image;
    }


}
