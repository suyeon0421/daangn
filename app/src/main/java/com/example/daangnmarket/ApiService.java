package com.example.daangnmarket;

import com.example.daangnmarket.models.LoginRequest;
import com.example.daangnmarket.models.Message;
import com.example.daangnmarket.models.MessageSendRequest;
import com.example.daangnmarket.models.PostRequest;
import com.example.daangnmarket.models.PostResponse;
import com.example.daangnmarket.models.RegisterRequest;
import com.example.daangnmarket.models.StatusRequest;
import com.example.daangnmarket.models.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("users/register")
    Call<UserResponse> registerUser(@Body RegisterRequest request);
    @POST("users/login")
    Call<UserResponse> loginUser(@Body LoginRequest request);

    @POST("/products")
    Call<PostResponse> createPost(@Body PostRequest postRequest);

    @GET("/products/")
    Call<List<PostResponse>> getAllPost();

    @GET("/products/{product_id}")
    Call<PostResponse> getPost(@Path("product_id") int productId);

    @GET("/products/seller/{seller_id}")
    Call<List<PostResponse>> getSellerPost(@Path("seller_id") int sellerId);

    @PATCH("/products/{product_id}/status")
    Call<List<PostResponse>>  updateProductStatus
            (@Path("product_id") int productId,
             @Body StatusRequest statusRequest);

    @POST("/messages") Call<Message> sendMessage(@Body MessageSendRequest request);

    @GET("/messages/{product_id}/{user1_id}/{user2_id}")
    Call<List<Message>> getMessagesBetweenUsers(
            @Path("product_id") int product_id,
            @Path("user1_id") int username1, // 경로의 {username1} 부분에 값 삽
            @Path("user2_id") int username2 // 경로의 {username2} 부분에 값
    );

    @GET("/messages/conversations/{user_id}")
    Call<List<Message>> getUserConversations(@Path("user_id") int user_id);




}
