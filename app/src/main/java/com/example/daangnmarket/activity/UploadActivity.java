package com.example.daangnmarket.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.daangnmarket.ApiService;
import com.example.daangnmarket.R;
import com.example.daangnmarket.RetrofitClient;
import com.example.daangnmarket.models.PostRequest;
import com.example.daangnmarket.models.PostResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadActivity extends AppCompatActivity {
    private ImageView iv_upload_image;
    private Button btn_add_image, btn_set_location, btn_upload;
    private EditText et_title, et_price, et_content;
    private TextView tv_location;
    private Uri selectedImageUri; // 선택된 이미지의 URI
    private ApiService apiService;

    private ActivityResultLauncher<Intent> galleryLauncher;

    // SharedPreferences에서 사용자 ID를 가져오기 위한 상수
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

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

        iv_upload_image = findViewById(R.id.iv_upload_image);
        btn_add_image = findViewById(R.id.btn_add_image);
        et_title = findViewById(R.id.et_title);
        et_price = findViewById(R.id.et_price);
        tv_location = findViewById(R.id.tv_location);
        btn_set_location = findViewById(R.id.btn_set_location);
        et_content = findViewById(R.id.et_content);
        btn_upload = findViewById(R.id.btn_upload);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            iv_upload_image.setImageURI(selectedImageUri);
                        }
                    }
                });

        btn_add_image.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        });

        btn_set_location.setOnClickListener(v -> {
            Toast.makeText(UploadActivity.this, "위치 설정 기능은 현재 미구현입니다.", Toast.LENGTH_SHORT).show();
            // 임시 위치 설정. 실제 앱에서는 지도 연동 등을 통해 사용자 위치를 받아와야 합니다.
            tv_location.setText("대구광역시 중구");
        });

        btn_upload.setOnClickListener(v -> {
            uploadPost();
        });

    }

    private void uploadPost() {
        String title = et_title.getText().toString().trim();
        String description = et_content.getText().toString().trim(); // description 필드 사용
        String priceStr = et_price.getText().toString().trim();
        String location_name = tv_location.getText().toString().trim();

        // 1. 입력값 유효성 검사
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) ||
                TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(location_name)) {
            Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageUri == null) {
            Toast.makeText(this, "사진을 추가해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int price;
        try {
            price = Integer.parseInt(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "가격은 숫자로 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. 이미지 URI를 String으로 변환 (PostRequest의 'image' 필드에 들어갈 값)
        // 백엔드가 이 URI 문자열을 받아 이미지를 처리할 것으로 가정합니다.
        String imageUrlString = selectedImageUri.toString();

        // 3. PostRequest 객체 생성
        // SharedPreferences에서 로그인된 사용자 ID 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt(KEY_USER_ID, -1); // 기본값 -1, 적절히 처리
        if (currentUserId == -1) {
            Toast.makeText(this, "로그인 정보가 유효하지 않습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: 실제 위경도 데이터 가져오기 (현재는 임시값)
        double currentLatitude = 35.8714;
        double currentLongitude = 128.6014;

        PostRequest postRequest = new PostRequest(
                title,
                description,
                price,
                currentUserId,
                currentLatitude,
                currentLongitude,
                location_name,
                imageUrlString // 이미지 URI를 String으로 변환하여 전달
        );

        // 4. API 호출
        apiService.createPost(postRequest).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(UploadActivity.this, "게시물 업로드 성공!", Toast.LENGTH_SHORT).show();
                    // 게시물 업로드 성공 시, MainActivity로 돌아가서 목록 갱신
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorMessage = "게시물 업로드 실패";
                    if (response.errorBody() != null) {
                        try {
                            String error = response.errorBody().string();
                            Log.e("UploadActivity", "업로드 실패 에러 바디: " + error);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(UploadActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("UploadActivity", "업로드 실패: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                Toast.makeText(UploadActivity.this, "서버 연결 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UploadActivity", "네트워크 오류: " + t.getMessage(), t);
            }
        });
    }
}