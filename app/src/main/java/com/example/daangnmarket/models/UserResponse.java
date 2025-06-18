package com.example.daangnmarket.models;

public class UserResponse {
    private int id;
    private String username;
    private String name;
    private String token;

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getToken() { return token; }

    public void setId(int id) {
        this.id = id;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "사용자 [ID: " + id + ", 아이디: " + username + ", 이름: " + name + "]";
    }
}
