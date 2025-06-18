package com.example.daangnmarket.models;

import com.google.gson.annotations.SerializedName;

public class MessageSendRequest {
    @SerializedName("product_id")
    private int productId;
    @SerializedName("sender_id")
    private int senderId;
    @SerializedName("receiver_id")
    private int receiverId;
    private String content;
    private double latitude;
    private double longitude;
    @SerializedName("location_name")
    private String locationName;

    // 1. 일반 메시지 전송을 위한 생성자 (기존)
    public MessageSendRequest(int productId, int senderId, int receiverId, String content) {
        this.productId = productId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        // 위치 관련 필드는 null 또는 기본값으로 설정
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.locationName = null;
    }

    // 2. 위치 메시지 전송을 위한 생성자 (⭐ 이 부분을 추가해야 합니다! ⭐)
    public MessageSendRequest(int productId, int senderId, int receiverId, double latitude, double longitude, String locationName) {
        this.productId = productId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        // 메시지 내용은 null 또는 빈 문자열로 설정
        this.content = null;
    }


    // Getter는 필요에 따라 추가할 수 있지만, 여기서는 Request Body로 사용되므로 필수 아님.
    // Setter는 보통 Request 객체에는 잘 사용되지 않음.
}