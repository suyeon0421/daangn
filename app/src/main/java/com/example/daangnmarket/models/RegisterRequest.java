package com.example.daangnmarket.models;

public class RegisterRequest {
    private String username;
    private String password;
    private String name;

    public RegisterRequest(String username, String password, String name) {
        this.username = username;
        this.password = password;
        this.name = name;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }

    public void setUsername() {
        this.username = username;
    }
    public void setPassword() {
        this.password = password;
    }
    public void setName() {
        this.name = name;
    }
}
