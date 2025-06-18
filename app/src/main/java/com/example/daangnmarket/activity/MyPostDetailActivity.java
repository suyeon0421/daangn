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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPostDetailActivity extends AppCompatActivity {
    ImageView iv_detail_image;
    TextView tv_detail_title, tv_detail_status, tv_detail_description, tv_detail_price, tv_detail_location, tv_detail_location_name; // 가격, 위치 TextView 추가
    Button btn_delete, btn_mark_as_sold;
    // sellerName은 이 액티비티에서 직접적으로 사용되지 않아 제거하거나 필요한 경우에만 추가.

    private ApiService apiService;
    private static final String TAG = "MyPostDetailActivity";

    private int currentPostId; // 현재 게시물의 ID를 저장할 변수 (삭제/판매완료 시 사용)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail_my); // XML 파일명 확인: activity_post_detail_my.xml

        // 툴바 설정 (선택 사항, 레이아웃에 툴바가 있다면)
        Toolbar toolbar = findViewById(R.id.toolbar); // activity_post_detail_my.xml에 toolbar id가 있다면
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("내 판매글 상세"); // 툴바 제목 설정
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 활성화
        }
        // 뒤로가기 버튼 클릭 리스너
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }


        apiService = RetrofitClient.getInstance().getApiService();

        iv_detail_image = findViewById(R.id.iv_detail_image);
        tv_detail_title = findViewById(R.id.tv_detail_title);
        tv_detail_status = findViewById(R.id.tv_detail_status);
        tv_detail_description = findViewById(R.id.tv_detail_description);
        tv_detail_price = findViewById(R.id.tv_detail_price); // 가격 TextView 바인딩
        tv_detail_location = findViewById(R.id.tv_detail_location); // 위치 TextView 바인딩
        tv_detail_location_name = findViewById(R.id.tv_detail_location_name);
        btn_mark_as_sold = findViewById(R.id.btn_mark_as_sold);

        // MyPostAdapter에서 PostResponse 객체 전체를 "myPost" 키로 넘겨주고 있으므로, 이를 받습니다.
        PostResponse post = (PostResponse) getIntent().getSerializableExtra("myPost");

        if (post != null) {
            currentPostId = post.getId(); // 게시물 ID 저장

            // UI에 데이터 바인딩
            tv_detail_title.setText(post.getTitle());
            tv_detail_description.setText(post.getDescription());
            tv_detail_status.setText(post.getStatus());
            tv_detail_price.setText(String.format("%,d원", post.getPrice())); // 가격 포맷팅
            tv_detail_location.setText("위도: " + String.format("%.4f", post.getLatitude()) + "\n" +
                    "경도: " + String.format("%.4f", post.getLongitude()));
            tv_detail_location_name.setText(post.getLocation_name()); // 위치 설정

            // Glide로 이미지 로드
            String baseUrl = "https://swu-carrot.replit.app/";
            String imageUrl = post.getImage_url();

            if (post.getImage_url() != null && !post.getImage_url().isEmpty()) {
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
            int productIdFromIntent = getIntent().getIntExtra("product_id", -1); // 혹시 모를 대비
            if (productIdFromIntent != -1) {
                currentPostId = productIdFromIntent;
                loadPostDetail(productIdFromIntent); // API 호출로 상세 정보 로드
            } else {
                Toast.makeText(this, "게시물 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        // 판매 완료 버튼
        btn_mark_as_sold.setOnClickListener(v -> {
            if (currentPostId != 0) {
                // 게시물 상태를 "판매완료"로 업데이트
                updatePostStatus(currentPostId, "판매완료");
            } else {
                Toast.makeText(MyPostDetailActivity.this, "게시물 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void loadPostDetail(int productId) {
        apiService.getPost(productId).enqueue(new Callback<PostResponse>() { // <-- getPost 사용!
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PostResponse post = response.body();
                    currentPostId = post.getId(); // ID 다시 저장
                    // UI에 데이터 바인딩
                    tv_detail_title.setText(post.getTitle());
                    tv_detail_description.setText(post.getDescription());
                    tv_detail_status.setText(post.getStatus());
                    tv_detail_price.setText(String.format("%,d원", post.getPrice())); // 가격 포맷팅
                    tv_detail_location.setText(post.getLocation_name()); // 위치 설정

                    if (post.getImage_url() != null && !post.getImage_url().isEmpty()) {
                        Glide.with(MyPostDetailActivity.this)
                                .load(post.getImage_url())
                                .placeholder(R.drawable.default_image)
                                .into(iv_detail_image);
                    } else {
                        iv_detail_image.setImageResource(R.drawable.default_image);
                    }
                    Log.d(TAG, "API로 게시물 상세 로드 성공: " + post.getTitle());
                } else {
                    Toast.makeText(MyPostDetailActivity.this, "게시물 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "API로 게시물 상세 로드 실패: " + response.code() + " " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "에러 바디: " + response.errorBody().string());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "에러 바디 읽기 실패", e);
                    }
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
        apiService.updateProductStatus(currentPostId, requestBody).enqueue(new Callback<List<PostResponse>>() { // 여기를 수정했습니다!
            @Override
            public void onResponse(Call<List<PostResponse>> call, Response<List<PostResponse>> response) { // 여기도 수정했습니다!
                if (response.isSuccessful() && response.body() != null) {
                    List<PostResponse> updatedPosts = response.body(); // List<PostResponse>로 받습니다.

                    PostResponse updatedCurrentPost = null;
                    for (PostResponse post : updatedPosts) {
                        if (post.getId() == currentPostId) {
                            updatedCurrentPost = post;
                            break;
                        }
                    }

                    if (updatedCurrentPost != null) {
                        // UI 업데이트: 찾은 게시물의 상태로 업데이트
                        tv_detail_status.setText(updatedCurrentPost.getStatus());

                        // MyPostActivity로 결과 전달
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("updated_post_id", updatedCurrentPost.getId());
                        resultIntent.putExtra("updated_post_status", updatedCurrentPost.getStatus());
                        setResult(RESULT_OK, resultIntent);

                        finish(); // 상태 업데이트 후 상세 화면 닫기
                    }

                }
            }

            @Override
            public void onFailure(Call<List<PostResponse>> call, Throwable t) { // 여기도 수정했습니다!
                Toast.makeText(MyPostDetailActivity.this, "상태 변경에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}