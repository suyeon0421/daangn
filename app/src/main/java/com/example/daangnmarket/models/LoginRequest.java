package com.example.daangnmarket.models;

public class LoginRequest {
    private String username;
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void setUsername() {
        this.username = username;
    }

    public void setPassword() {
        this.password = password;
    }

}


