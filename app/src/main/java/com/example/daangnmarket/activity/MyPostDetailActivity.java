package com.example.daangnmarket.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.daangnmarket.ApiService;
import com.example.daangnmarket.R;
import com.example.daangnmarket.RetrofitClient;
import com.example.daangnmarket.models.PostResponse;
import com.example.daangnmarket.models.StatusRequest;

import java.io.IOException;
import java.util.List; // List import 유지

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPostDetailActivity extends AppCompatActivity {
    ImageView iv_detail_image;
    TextView tv_detail_title, tv_detail_status, tv_detail_description, tv_detail_price, tv_detail_location, tv_detail_location_name;
    Button btn_delete, btn_mark_as_sold;

    private ApiService apiService;
    private static final String TAG = "MyPostDetailActivity";

    private int currentPostId;
    private String currentPostStatus; // 현재 게시물 상태를 저장하여 UI 업데이트에 활용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail_my);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("내 판매글 상세");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        apiService = RetrofitClient.getInstance().getApiService();

        iv_detail_image = findViewById(R.id.iv_detail_image);
        tv_detail_title = findViewById(R.id.tv_detail_title);
        tv_detail_status = findViewById(R.id.tv_detail_status);
        tv_detail_description = findViewById(R.id.tv_detail_description);
        tv_detail_price = findViewById(R.id.tv_detail_price);
        tv_detail_location = findViewById(R.id.tv_detail_location);
        tv_detail_location_name = findViewById(R.id.tv_detail_location_name);
        btn_mark_as_sold = findViewById(R.id.btn_mark_as_sold);

        PostResponse post = (PostResponse) getIntent().getSerializableExtra("myPost");

        if (post != null) {
            currentPostId = post.getId();
            currentPostStatus = post.getStatus(); // 상태 저장

            tv_detail_title.setText(post.getTitle());
            tv_detail_description.setText(post.getDescription());
            tv_detail_status.setText(post.getStatus());
            tv_detail_price.setText(String.format("%,d원", post.getPrice()));
            tv_detail_location.setText("위도: " + String.format("%.4f", post.getLatitude()) + "\n" +
                    "경도: " + String.format("%.4f", post.getLongitude()));
            tv_detail_location_name.setText(post.getLocation_name());

            String baseUrl = "https://swu-carrot.replit.app/";
            String imageUrl = post.getImage_url();

            if (imageUrl != null && !imageUrl.isEmpty()) {
                String fullUrl = baseUrl + imageUrl;
                Glide.with(MyPostDetailActivity.this)
                        .load(fullUrl)
                        .placeholder(R.drawable.default_image)
                        .into(iv_detail_image);
            } else {
                iv_detail_image.setImageResource(R.drawable.default_image);
            }
            Log.d(TAG, "인텐트로 게시물 상세 로드 성공: " + post.getTitle());

        } else {
            int productIdFromIntent = getIntent().getIntExtra("product_id", -1);
            if (productIdFromIntent != -1) {
                currentPostId = productIdFromIntent;
                loadPostDetail(productIdFromIntent); // API 호출로 상세 정보 로드
            } else {
                Toast.makeText(this, "게시물 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        btn_mark_as_sold.setOnClickListener(v -> {
            if (currentPostId != 0) {
                // 현재 상태가 "판매중"일 때만 "판매완료"로 변경
                // 주의: tv_detail_status.getText().toString() 대신 currentPostStatus를 사용하는 것이 더 정확할 수 있습니다.
                // 만약 현재 상태가 "판매완료"인데 또 "판매완료"로 시도하는 것을 막고 싶다면 이 조건문을 유지.
                if ("판매중".equals(currentPostStatus)) { // 현재 상태 변수 활용
                    updatePostStatus(currentPostId, "판매완료");
                } else if ("판매완료".equals(currentPostStatus)) {
                    Toast.makeText(MyPostDetailActivity.this, "이미 판매완료된 게시물입니다.", Toast.LENGTH_SHORT).show();
                } else {
                    // "예약중" 등 다른 상태일 경우의 처리
                    updatePostStatus(currentPostId, "판매완료"); // 일단 판매완료로 변경 시도
                }
            } else {
                Toast.makeText(MyPostDetailActivity.this, "게시물 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPostDetail(int productId) {
        apiService.getPost(productId).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PostResponse post = response.body();
                    currentPostId = post.getId();
                    currentPostStatus = post.getStatus(); // 상태 저장
                    tv_detail_title.setText(post.getTitle());
                    tv_detail_description.setText(post.getDescription());
                    tv_detail_status.setText(post.getStatus());
                    tv_detail_price.setText(String.format("%,d원", post.getPrice()));
                    tv_detail_location.setText("위도: " + String.format("%.4f", post.getLatitude()) + "\n" +
                            "경도: " + String.format("%.4f", post.getLongitude()));
                    tv_detail_location_name.setText(post.getLocation_name());


                    String baseUrl = "https://swu-carrot.replit.app/";
                    String imageUrl = post.getImage_url();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        String fullUrl = baseUrl + imageUrl;
                        Glide.with(MyPostDetailActivity.this)
                                .load(fullUrl)
                                .placeholder(R.drawable.default_image)
                                .into(iv_detail_image);
                    } else {
                        iv_detail_image.setImageResource(R.drawable.default_image);
                    }
                    Log.d(TAG, "API로 게시물 상세 로드 성공: " + post.getTitle());
                } else {
                    String errorMessage = "게시물 정보를 불러오는데 실패했습니다.";
                    try {
                        if (response.errorBody() != null) {
                            String error = response.errorBody().string();
                            Log.e(TAG, "API로 게시물 상세 로드 실패 에러 바디: " + error);
                            errorMessage += ": " + error;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "에러 바디 읽기 실패", e);
                    }
                    Toast.makeText(MyPostDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "API로 게시물 상세 로드 실패: " + response.code() + " " + response.message());
                    finish();
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                Toast.makeText(MyPostDetailActivity.this, "서버 연결 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "API로 게시물 상세 로드 onFailure: " + t.getMessage(), t);
                finish();
            }
        });
    }

    private void updatePostStatus(int currentPostId, String newStatus) {
        StatusRequest requestBody = new StatusRequest(newStatus);

        apiService.updateProductStatus(currentPostId, requestBody).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        PostResponse updatedPost = response.body();

                        tv_detail_status.setText(updatedPost.getStatus());
                        currentPostStatus = updatedPost.getStatus();

                        Toast.makeText(MyPostDetailActivity.this, "게시물 상태가 '" + updatedPost.getStatus() + "'로 변경되었습니다.", Toast.LENGTH_SHORT).show();

                        // MyPostActivity로 결과 전달
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("updated_post_id", updatedPost.getId());
                        resultIntent.putExtra("updated_post_status", updatedPost.getStatus());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {

                        tv_detail_status.setText(newStatus);
                        currentPostStatus = newStatus; // 현재 상태 변수 업데이트

                        Toast.makeText(MyPostDetailActivity.this, "게시물 상태가 '" + newStatus + "'로 변경되었습니다. (서버 응답 본문 없음)", Toast.LENGTH_LONG).show();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("updated_post_id", currentPostId);
                        resultIntent.putExtra("updated_post_status", newStatus);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                } else {
                    String errorMessage = "상태 변경 실패";
                    try {
                        if (response.errorBody() != null) {
                            String error = response.errorBody().string();
                            Log.e(TAG, "상태 변경 실패 에러 바디: " + error);
                            errorMessage += ": " + error;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "에러 바디 읽기 실패", e);
                    }
                    Toast.makeText(MyPostDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "상태 변경 실패: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {

                Toast.makeText(MyPostDetailActivity.this, "상태 변경 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "상태 변경 onFailure: " + t.getMessage(), t);
                t.printStackTrace();

                finish();
            }
        });
    }
}