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

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;

public interface ApiService {
    @POST("users/register")
    Call<UserResponse> registerUser(@Body RegisterRequest request);
    @POST("users/login")
    Call<UserResponse> loginUser(@Body LoginRequest request);

    @Multipart
    @POST("/products")
    Call<PostResponse> createProduct(
            @Part MultipartBody.Part image, // 이미지 파일 파트 (서버의 'image' 필드명과 일치)
            @Part("title") RequestBody title, // "title" 필드
            @Part("description") RequestBody description, // "description" 필드
            @Part("price") RequestBody price, // "price" 필드
            @Part("seller_id") RequestBody sellerId, // "seller_id" 필드
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("location_name") RequestBody locationName);

    @GET("/products")
    Call<List<PostResponse>> getAllPost();

    @GET("/products/{product_id}")
    Call<PostResponse> getPost(@Path("product_id") int productId);

    @GET("/products/seller/{seller_id}")
    Call<List<PostResponse>> getSellerPost(@Path("seller_id") int sellerId);

    @PATCH("/products/{product_id}/status") // @Path("product_id")는 그대로 유지
    Call<PostResponse> updateProductStatus(@Path("product_id") int productId, @Body StatusRequest request);

    @POST("/messages") Call<Message> sendMessage(@Body MessageSendRequest request);


    @GET("/messages/{product_id}/{user1_id}/{user2_id}")
    Call<List<Message>> getMessagesBetweenUsers(
            @Path("product_id") int product_id,
            @Path("user1_id") int username1, // 경로의 {username1} 부분에 값 삽
            @Path("user2_id") int username2 // 경로의 {username2} 부분에 값
    );
}
