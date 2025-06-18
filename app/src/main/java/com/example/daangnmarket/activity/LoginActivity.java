package com.example.daangnmarket.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.daangnmarket.ApiService;
import com.example.daangnmarket.R;
import com.example.daangnmarket.RetrofitClient;
import com.example.daangnmarket.models.LoginRequest;
import com.example.daangnmarket.models.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText et_username, et_password;
    Button btn_login;
    private ApiService apiService;

    private static final String PREFS_NAME = "MyPrefsFile"; // SharedPreferences 파일명
    private static final String KEY_AUTH_TOKEN = "authToken"; // 토큰 저장 키
    private static final String KEY_USER_ID = "userId"; // 사용자 ID 저장 키 (필요시)
    private static final String KEY_USERNAME = "username"; // 사용자 아이디 저장 키 (필요시)
    private static final String KEY_NAME = "name"; // 사용자 이름(닉네임) 저장 키 (필요시)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = RetrofitClient.getInstance().getApiService();

        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);

        setupClickListeners();

        checkLoginStatus();
    }

    private void saveLoginInfo(UserResponse user) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_AUTH_TOKEN, user.getToken());
        editor.putInt(KEY_USER_ID, user.getId()); // 필요시 저장
        editor.putString(KEY_USERNAME, user.getUsername()); // 필요시 저장
        editor.putString(KEY_NAME, user.getName()); // 필요시 저장

        editor.apply(); // 비동기적으로 저장 (대부분의 경우 apply 사용)
        // editor.commit(); // 동기적으로 저장 (즉시 필요할 경우 사용)
    }


    private void setupClickListeners() {
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();

            }
        });
    }

    private void loginUser() {
        String username = et_username.getText().toString().trim();
        String password = et_password.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(username, password);
        Call<UserResponse> call = apiService.loginUser(request);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String authToken = response.body().getToken(); // ← 로그인 응답에서 받은 토큰
                    int userId = response.body().getId(); // ← 로그인 응답에서 받은 사용자 ID

                    // 🔥 여기서 SharedPreferences에 저장
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefsFile", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("authToken", authToken); // 🔐 토큰 저장
                    editor.putInt("userId", userId); // 👤 사용자 ID 저장
                    editor.apply();
                    //로그인 성공 시
                    UserResponse user = response.body();
                    Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "아이디 혹은 비밀번호를 다시 입력하세요.", Toast.LENGTH_SHORT).show();
                    et_username.setText("");
                    et_password.setText("");
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "서버 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                et_username.setText("");
                et_password.setText("");
            }
        });
    }

    private void checkLoginStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String authToken = sharedPreferences.getString(KEY_AUTH_TOKEN, null); // 토큰이 없으면 null 반환


        if (authToken != null && !authToken.isEmpty()) {
            // 토큰이 존재하면 이미 로그인된 상태로 간주
            Toast.makeText(this, "자동 로그인 되었습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // 로그인 액티비티 종료
        }
    }
}
    


