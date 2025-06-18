package com.example.daangnmarket.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daangnmarket.ApiService;
import com.example.daangnmarket.R;
import com.example.daangnmarket.RetrofitClient;
import com.example.daangnmarket.adapter.MainPostAdapter;
import com.example.daangnmarket.models.PostResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MainPostAdapter postAdapter;
    private ApiService apiService;
    private FloatingActionButton fab_Add_Post;
    private ArrayList<PostResponse> mainPostList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        apiService = RetrofitClient.getInstance().getApiService();

        recyclerView = findViewById(R.id.recycler_view_posts);
        postAdapter = new MainPostAdapter(MainActivity.this,mainPostList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);

        PostResponse newPost = (PostResponse) getIntent().getSerializableExtra("new_post");
        if (newPost != null) {
            mainPostList.add(newPost);
            postAdapter.notifyItemInserted(mainPostList.size() -1);
        }


        // 글쓰기 버튼 클릭 처리
        fab_Add_Post = findViewById(R.id.fab_add_post);
        fab_Add_Post.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, UploadActivity.class);
            startActivity(intent);
        });

        loadAllPosts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllPosts();
    }
    private void loadAllPosts() {
        apiService.getAllPost().enqueue(new Callback<List<PostResponse>> () {
            @Override
            public void onResponse(Call<List<PostResponse>> call, Response<List<PostResponse>> response) {
                if (response.isSuccessful () && response.body() != null) {
                    mainPostList.clear();
                    List<PostResponse> fetchedPosts = response.body();
                    //역순 정렬
                    Collections.reverse(fetchedPosts);
                    mainPostList.addAll(fetchedPosts);
                    postAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this,"게시물을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<PostResponse>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "서버 연결 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // 메뉴(main_menu.xml) 연결
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    // 메뉴 항목 클릭 처리
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_my_posts) {
            Intent intent = new Intent(MainActivity.this, MyPostActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
