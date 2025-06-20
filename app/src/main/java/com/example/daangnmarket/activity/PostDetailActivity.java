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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailActivity extends AppCompatActivity {
    ImageView iv_product_image;
    TextView tv_title, tv_price, tv_location, tv_content, tv_seller_name, tv_location_name;
    Button btn_chat, btn_mark_sold, btn_delete;
    String sellerName; // sellerName은 UI 업데이트 후 저장해도 됨

    private ApiService apiService;
    private static final String TAG = "PostDetailActivity"; // 로그 태그
    private PostResponse post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // 툴바 설정 (선택 사항, 레이아웃에 툴바가 있다면)
        Toolbar toolbar = findViewById(R.id.toolbar); // activity_post_detail_my.xml에 toolbar id가 있다면
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 활성화
        }
        // 뒤로가기 버튼 클릭 리스너
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        apiService = RetrofitClient.getInstance().getApiService();

        iv_product_image = findViewById(R.id.iv_product_image);
        tv_title = findViewById(R.id.tv_title);
        tv_price = findViewById(R.id.tv_price);
        tv_location = findViewById(R.id.tv_location);
        tv_location_name = findViewById(R.id.tv_location_name);
        tv_content = findViewById(R.id.tv_content);
        tv_seller_name = findViewById(R.id.tv_seller_name);
        btn_chat = findViewById(R.id.btn_chat);
        btn_mark_sold = findViewById(R.id.btn_mark_sold);
        btn_delete = findViewById(R.id.btn_delete);

        // Intent에서 product_id를 가져옵니다.
        int productId = getIntent().getIntExtra("product_id", -1);

        if (productId != -1) {
            // product_id가 유효하면 API 호출
            loadPostDetail(productId);
        } else {
            Toast.makeText(this, "게시물 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish(); // 유효한 ID가 없으면 액티비티 종료
        }

        btn_chat.setOnClickListener(v -> {
            Intent intent = new Intent(PostDetailActivity.this, ChatActivity.class);
            intent.putExtra("CHAT_TITLE", sellerName);
            intent.putExtra("product_id", productId); // <- 반드시 필요
            intent.putExtra("OTHER_USER_ID", post.getSeller_id()); // <- 반드시 필요
            startActivity(intent);
        });

        try (FileInputStream fis = openFileInput("last_post.txt")) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            Log.d(TAG, "저장된 마지막 게시글 제목: " + sb.toString());
        } catch (IOException e) {
            Log.e(TAG, "내부 저장소 읽기 실패", e);
        }

    }

    // 특정 게시물 상세 정보를 불러오는 메서드
    private void loadPostDetail(int productId) {
        apiService.getPost(productId).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    post = response.body();
                    // UI에 데이터 바인딩
                    tv_title.setText(post.getTitle());

                    // 마지막에 본 게시물 내부 저장소에 추가
                    String filename = "last_post.txt";
                    String content = post.getTitle();

                    try (FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE)) {
                        fos.write(content.getBytes());
                        Log.d(TAG, "게시글 제목을 내부 저장소에 저장함: " + content);
                    } catch (IOException e) {
                        Log.e(TAG, "내부 저장소 저장 실패", e);
                    }


                    tv_location.setText("위도: " + String.format("%.4f", post.getLatitude()) + "\n" +
                            "경도: " + String.format("%.4f", post.getLongitude()));
                    tv_location_name.setText(post.getLocation_name());
                    tv_price.setText(String.format("%,d원", post.getPrice())); // 가격 포맷팅
                    tv_content.setText(post.getDescription());
                    tv_seller_name.setText(post.getSeller_name());

                    // sellerName 변수에 저장 (채팅 버튼 등에서 사용하기 위함)
                    sellerName = post.getSeller_name();

                    // Glide로 이미지 로드
                    String baseUrl = "https://swu-carrot.replit.app/";
                    String imageUrl = post.getImage_url();

                    if (post.getImage_url() != null && !post.getImage_url().isEmpty()) {
                        String fullUrl = baseUrl + imageUrl;
                        Glide.with(PostDetailActivity.this)
                                .load(fullUrl)
                                .placeholder(R.drawable.default_image)
                                .into(iv_product_image);
                    } else {
                        iv_product_image.setImageResource(R.drawable.default_image);
                    }
                    Log.d(TAG, "게시물 상세 로드 성공: " + post.getTitle());
                } else {
                    Toast.makeText(PostDetailActivity.this, "게시물 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "게시물 상세 로드 실패: " + response.code() + " " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "에러 바디: " + response.errorBody().string());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "에러 바디 읽기 실패", e);
                    }
                    finish(); // 실패 시 액티비티 종료
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                Toast.makeText(PostDetailActivity.this, "서버 연결 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "게시물 상세 로드 onFailure: " + t.getMessage(), t);
                finish(); // 네트워크 오류 시 액티비티 종료
            }
        });
    }
}