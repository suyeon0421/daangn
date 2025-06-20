package com.example.daangnmarket.activity;

import android.Manifest;
import android.content.Context; // Context import 추가
import android.content.Intent;
import android.content.SharedPreferences; // SharedPreferences import 추가
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.daangnmarket.ApiService;
import com.example.daangnmarket.R;
import com.example.daangnmarket.RetrofitClient;
import com.example.daangnmarket.models.PostRequest;
import com.example.daangnmarket.models.PostResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


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

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    double currentLat = 0.0;
    double currentLng = 0.0;

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //위치 권한 요청
        checkLocationPermission();

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
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longtitude = location.getLongitude();
                    String locationText = "위도: " + String.format("%.4f", latitude) + "\n" +
                            "경도: " + String.format("%.4f", longtitude);
                    tv_location.setText(locationText);
                }
            });
        });

        btn_upload.setOnClickListener(v -> {
            uploadPost();


        });

    }

    private void uploadPost() {
        String title = et_title.getText().toString().trim();
        String description = et_content.getText().toString().trim();
        String priceStr = et_price.getText().toString().trim();
        String locationName = tv_location.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) ||
                TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(locationName)) {
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

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("userId", -1);
        Log.d("UploadActivity", "userId from SharedPreferences: " + currentUserId);
        if (currentUserId == -1) {
            Toast.makeText(this, "로그인 정보가 유효하지 않습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("UploadActivity", "uploadPost() data: title=" + title + ", description=" + description +
                ", price=" + price + ", sellerId=" + currentUserId + ", lat=" + currentLat +
                ", lng=" + currentLng + ", locationName=" + locationName);

        PostRequest postRequest = new PostRequest(
                title,
                description,
                price,
                currentUserId,
                locationName
        );

        apiService.createPost(postRequest).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(UploadActivity.this, "게시물 업로드 성공!", Toast.LENGTH_SHORT).show();
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


    private void checkLocationPermission() {
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission
                            .ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grandResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grandResults.length > 0 && grandResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLastLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            return;
        };

    }
}