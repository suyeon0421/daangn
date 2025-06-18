package com.example.daangnmarket.models;

import java.io.Serializable;

public class PostResponse implements Serializable {
    private String title;
    private String description;
    private int price;
    private double latitude;
    private double longitude;
    private String location_name;
    private int id;
    private String image_url;
    private int seller_id;
    private String seller_name;
    private String status;
    private String created_at;

    public PostResponse(String title, String description, int price, int seller_id, int id, String image_url,
                       double latitude, double longitude, String location_name,
                        String seller_name, String status, String created_at) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.seller_id = seller_id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location_name = location_name;
        this.id = id;
        this.image_url = image_url;
        this.seller_name = seller_name;
        this.status = status;
        this.created_at = created_at;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getPrice() { return price; }
    public int getSeller_id() { return seller_id; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getLocation_name() { return location_name; }
    public int getId() { return id; }
    public String getImage_url() { return image_url; }
    public String getSeller_name() { return seller_name; }
    public String getStatus() { return status; }
    public String getCreated_at() { return created_at; }

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
    public void setId(int id) {
        this.id = id;
    }
    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
    public void setSeller_name(String seller_name) {
        this.seller_name = seller_name;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }


}
