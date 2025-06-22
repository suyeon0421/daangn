package com.example.daangnmarket.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import com.example.daangnmarket.ApiService;
import com.example.daangnmarket.R;
import com.example.daangnmarket.RetrofitClient;
import com.example.daangnmarket.models.RegisterRequest;
import com.example.daangnmarket.models.UserResponse;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    private EditText et_username, et_password, et_name;
    private Button btn_signup, btn_login;

    private ApiService apiService;

    // SharedPreferences를 위한 상수 정의 (LoginActivty와 동일해야 함)
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_AUTH_TOKEN = "authToken";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NAME = "name";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        apiService = RetrofitClient.getInstance().getApiService();

        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        et_name = findViewById(R.id.et_name);
        btn_signup = findViewById(R.id.btn_signup);
        btn_login = findViewById(R.id.btn_login);

        setupClickListeners();

    }


    private void setupClickListeners() {
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerUser() {
        String username = et_username.getText().toString().trim();
        String password = et_password.getText().toString().trim();
        String name = et_name.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name)) {
            Toast.makeText(SignupActivity.this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest request = new RegisterRequest(username, password, name);

        Call<UserResponse> call = apiService.registerUser(request);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    // 회원가입 성공 시
                    Toast.makeText(SignupActivity.this, "회원가입에 성공하였습니다.", Toast.LENGTH_SHORT).show();

                    // 1. SharedPreferences에 사용자 정보 저장 (선택 사항: 회원가입 후 자동 로그인 시)
                    saveLoginInfo(user);

                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // 실패 경우 (예: 이미 존재하는 유저)
                    String errorMessage = "회원가입에 실패했습니다.";
                    if (response.errorBody() != null) {
                        try {
                            // 서버에서 내려주는 구체적인 에러 메시지를 파싱하여 보여줄 수 있음
                            // 예: JSONObject errorJson = new JSONObject(response.errorBody().string());
                            // errorMessage = errorJson.optString("message", "회원가입에 실패했습니다.");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    et_username.setText("");
                    et_password.setText("");
                    et_name.setText("");
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "서버 연결에 실패하였습니다. " + t.getMessage(), Toast.LENGTH_LONG).show();
                et_username.setText("");
                et_password.setText("");
                et_name.setText("");
            }
        });
    }

    // SharedPreferences에 로그인 정보 저장하는 메서드 (LoginActivity와 중복되므로 유틸리티 클래스로 분리 가능)
    private void saveLoginInfo(UserResponse user) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_AUTH_TOKEN, user.getToken());
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_NAME, user.getName());
        Log.d("LoginInfo", "Token saved: " + user.getToken());


        editor.apply();
    }
}



