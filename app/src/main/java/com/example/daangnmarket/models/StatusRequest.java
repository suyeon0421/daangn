package com.example.daangnmarket.models;

import com.google.gson.annotations.SerializedName;

public class StatusRequest {
    @SerializedName("status")
    private String status;

    public StatusRequest(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
