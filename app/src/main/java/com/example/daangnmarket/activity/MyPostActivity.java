package com.example.daangnmarket.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daangnmarket.ApiService;
import com.example.daangnmarket.R;
import com.example.daangnmarket.RetrofitClient;
import com.example.daangnmarket.adapter.MyPostAdapter;
import com.example.daangnmarket.models.PostResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPostActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MyPostAdapter postAdapter;
    private ArrayList<PostResponse> myPostList = new ArrayList<>(); // 변수명 myPostList로 변경
    private ApiService apiService; // ApiService 추가
    private static final String TAG = "MyPostActivity"; // 로그 태그

    // SharedPreferences에서 사용자 ID를 가져오기 위한 상수
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("내 판매내역"); // 툴바 제목 설정
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 활성화
        }
        // 뒤로가기 버튼 클릭 리스너
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }


        apiService = RetrofitClient.getInstance().getApiService(); // ApiService 초기화

        recyclerView = findViewById(R.id.recycler_view_my_posts);
        postAdapter = new MyPostAdapter(MyPostActivity.this, myPostList); // myPostList 전달
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);

        loadMyPosts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 혹시 MyPostDetailActivity에서 수정/삭제 후 돌아왔을 경우를 대비하여 다시 로드
        loadMyPosts();
    }

    private void loadMyPosts() {
        // SharedPreferences에서 로그인된 사용자 ID 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt(KEY_USER_ID, -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "로그인 정보가 없어 내 게시물을 불러올 수 없습니다.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "사용자 ID를 찾을 수 없음. 로그인 상태 확인 필요.");
            // TODO: 로그인 화면으로 리디렉션하거나, UI에 로그인 필요 메시지 표시
            myPostList.clear(); // 목록 비우기
            postAdapter.notifyDataSetChanged();
            return;
        }

        apiService.getSellerPost(currentUserId).enqueue(new Callback<List<PostResponse>>() {
            @Override
            public void onResponse(Call<List<PostResponse>> call, Response<List<PostResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    myPostList.clear(); // 기존 목록 비우기
                    List<PostResponse> fetchedPosts = response.body();
                    // 최신 게시물이 먼저 보이도록 리스트를 역순으로 정렬 (선택 사항)
                    Collections.reverse(fetchedPosts);
                    myPostList.addAll(fetchedPosts); // 새로운 데이터 추가
                    postAdapter.notifyDataSetChanged(); // 어댑터 갱신
                    Log.d(TAG, "내 게시물 로드 성공: " + myPostList.size() + "개");
                } else {
                    Toast.makeText(MyPostActivity.this, "내 게시물을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "내 게시물 로드 실패: " + response.code() + " " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "에러 바디: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "에러 바디 읽기 실패", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<PostResponse>> call, Throwable t) {
                Toast.makeText(MyPostActivity.this, "서버 연결 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "내 게시물 로드 onFailure: " + t.getMessage(), t);
            }
        });
    }
}