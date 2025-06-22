package com.example.daangnmarket.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView; // ImageView 다시 import
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull; // @NonNull import 추가

import com.example.daangnmarket.ApiService;
import com.example.daangnmarket.R;
import com.example.daangnmarket.RetrofitClient;
import com.example.daangnmarket.models.PostRequest;
import com.example.daangnmarket.models.PostResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadActivity extends AppCompatActivity {
    private ImageView iv_upload_image; // 이미지뷰 유지
    private Button btn_add_image, btn_set_location, btn_upload; // 이미지 추가 버튼 유지
    private EditText et_title, et_price, et_content, et_location_name;
    private TextView tv_location;
    private Uri selectedImageUri; // 선택된 이미지의 URI 유지
    private ApiService apiService;

    private ActivityResultLauncher<Intent> galleryLauncher; // 갤러리 런처 유지

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    double currentLat = 0.0;
    double currentLng = 0.0;

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission(); // 위치 권한 확인 및 초기 위치 가져오기

        apiService = RetrofitClient.getInstance().getApiService();

        iv_upload_image = findViewById(R.id.iv_upload_image); // 이미지뷰 초기화
        btn_add_image = findViewById(R.id.btn_add_image); // 이미지 추가 버튼 초기화
        et_title = findViewById(R.id.et_title);
        et_price = findViewById(R.id.et_price);
        tv_location = findViewById(R.id.tv_location);
        btn_set_location = findViewById(R.id.btn_set_location);
        et_content = findViewById(R.id.et_content);
        btn_upload = findViewById(R.id.btn_upload);
        et_location_name = findViewById(R.id.et_location_name);

        // 갤러리 런처
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

        // 이미지 추가 버튼 클릭 리스너 유지
        btn_add_image.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        });

        btn_set_location.setOnClickListener(v -> {
            // 위치 가져오기
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                checkLocationPermission(); // 다시 권한 요청
                return;
            }
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLng = location.getLongitude();
                    String locationText = "위도: " + String.format("%.4f", currentLat) + "\n" +
                            "경도: " + String.format("%.4f", currentLng);
                    tv_location.setText(locationText);
                    Log.d("UploadActivity", "위치 설정됨: " + currentLat + ", " + currentLng);
                } else {
                    Toast.makeText(this, "위치를 가져올 수 없습니다. GPS를 확인해주세요.", Toast.LENGTH_SHORT).show();
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
        String locationName = et_location_name.getText().toString().trim();

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


        File file = new File(selectedImageUri.getPath()); // Uri에서 실제 파일 경로를 얻는 것은 복잡할 수 있습니다.
        String realPath = getPathFromUri(this, selectedImageUri);
        if (realPath == null) {
            Toast.makeText(this, "이미지 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        File imageFile = new File(realPath);

        RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(selectedImageUri)), imageFile);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

        // 2. 다른 필드들을 RequestBody로 변환
        RequestBody titlePart = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody descriptionPart = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody pricePart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(price));
        RequestBody sellerIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(currentUserId));
        RequestBody latitudePart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(currentLat)); // 현재 currentLat/Lng 값이 0.0인 문제도 확인 필요
        RequestBody longitudePart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(currentLng)); // 현재 currentLat/Lng 값이 0.0인 문제도 확인 필요
        RequestBody locationNamePart = RequestBody.create(MediaType.parse("text/plain"), locationName);


        // 3. ApiService 호출 수정
        apiService.createProduct(
                imagePart,
                titlePart,
                descriptionPart,
                pricePart,
                sellerIdPart,
                latitudePart,
                longitudePart,
                locationNamePart
        ).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(UploadActivity.this, "게시물 업로드 성공!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // 성공 시 MainActivity로 돌아가 게시물 목록을 새로고침하거나 추가할 수 있도록
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

    private String getPathFromUri(Context context, Uri uri) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        android.database.Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        return result;
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
            getLastLocation(); // 권한이 있으면 바로 위치 가져오기 시도
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grandResults.length > 0 && grandResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                getLastLocation(); // 권한 허용 시 위치 가져오기 시도
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
            Log.w("UploadActivity", "위치 권한이 없어 마지막 위치를 가져올 수 없습니다.");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                Log.d("UploadActivity", "초기 위치 설정: 위도=" + currentLat + ", 경도=" + currentLng);
                // 앱 시작 시 초기 위치를 tv_location에 바로 표시하고 싶다면 아래 주석 해제
                // tv_location.setText("현재 위치: " + String.format("%.4f", currentLat) + ", " + String.format("%.4f", currentLng));
            } else {
                Log.w("UploadActivity", "getLastLocation: 마지막 위치 정보를 가져올 수 없습니다. GPS 켜짐 여부 확인.");
                Toast.makeText(this, "현재 위치를 가져올 수 없습니다. GPS를 켜거나 잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
            }
        });
    }
}