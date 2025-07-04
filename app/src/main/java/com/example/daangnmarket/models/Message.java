package com.example.daangnmarket.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Message implements Serializable {
    public static final int TYPE_ME = 0;
    public static final int TYPE_OTHER = 1;
    public static final int TYPE_LOCATION = 2;

    @SerializedName("id") // <-- 이 부분에 @SerializedName 추가 (서버 JSON에 "id"가 있음)
    private int id;
    @SerializedName("product_id")
    private int productId;
    @SerializedName("sender_id")
    private int senderId;
    @SerializedName("sender_name")
    private String senderName;
    @SerializedName("receiver_id")
    private int receiverId;
    @SerializedName("receiver_name")
    private String receiverName;
    @SerializedName("content") // <-- 이 부분에 @SerializedName 추가 (서버 JSON에 "content"가 있음)
    private String content;
    @SerializedName("latitude") // <-- 이 부분에 @SerializedName 추가 (서버 JSON에 "latitude"가 있음)
    private double latitude;
    @SerializedName("longitude") // <-- 이 부분에 @SerializedName 추가 (서버 JSON에 "longitude"가 있음)
    private double longitude;
    @SerializedName("location_name")
    private String locationName;
    @SerializedName("timestamp") // <-- 이 부분에 @SerializedName 추가 (서버 JSON에 "timestamp"가 있음)
    private String timestamp; // 서버에서 받은 원본 시간 문자열

    private int messageType;
    private boolean isLocationMessage;


    // 일반 메시지 생성자
    public Message (int id, int productId, int senderId, String senderName,
                    int receiverId, String receiverName, String content, String timestamp) {
        this.id = id;
        this.productId = productId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.content = content;
        this.timestamp = timestamp;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.locationName = null;
        this.isLocationMessage = false;
    }

    // 위치 메시지 생성자
    public Message (int id, int productId, int senderId, String senderName,
                    int receiverId, String receiverName, double latitude, double longitude,
                    String locationName, String timestamp) {
        this.id = id;
        this.productId = productId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.timestamp = timestamp;
        this.content = null;
        this.isLocationMessage = true;
    }

    // Getter 및 Setter (모든 필드에 대해 존재함)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public int getMessageType() { return messageType; }
    public void setMessageType(int messageType) { this.messageType = messageType; }
    public boolean isLocationMessage() { return isLocationMessage; }
    public void setLocationMessage(boolean locationMessage) { isLocationMessage = locationMessage; }

    // 디버깅을 위한 toString() 메서드 추가 (필수 아님, 하지만 디버깅에 매우 유용)
    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", productId=" + productId +
                ", senderId=" + senderId +
                ", senderName='" + senderName + '\'' +
                ", receiverId=" + receiverId +
                ", receiverName='" + receiverName + '\'' +
                ", content='" + content + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", locationName='" + locationName + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", messageType=" + messageType +
                ", isLocationMessage=" + isLocationMessage +
                '}';
    }
}