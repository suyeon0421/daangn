package com.example.daangnmarket.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daangnmarket.ApiService;
import com.example.daangnmarket.R;
import com.example.daangnmarket.RetrofitClient;
import com.example.daangnmarket.adapter.MessageAdapter;
import com.example.daangnmarket.models.Message;
import com.example.daangnmarket.models.MessageSendRequest;
import com.example.daangnmarket.models.PostResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recycler_view_chat;
    private TextView tv_chat_title;
    private ImageView iv_back;
    private EditText et_message;
    private Button btn_send;
    private ImageButton btn_location;

    private MessageAdapter messageAdapter;
    private ArrayList<Message> messageList = new ArrayList<>();

    private ApiService apiService;
    private int currentUserId;
    private int otherUserId;
    private int currentProductId;
    private PostResponse post;

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_USER_ID = "userId";
    private static final String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ChatActivity", "onCreate 호출됨");
        setContentView(R.layout.activity_chat);


        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt(KEY_USER_ID, -1);

        Intent intent = getIntent();
        String chatTitle = intent.getStringExtra("CHAT_TITLE");
        currentProductId = intent.getIntExtra("product_id", -1);
        otherUserId = intent.getIntExtra("OTHER_USER_ID", -1);

        if (currentUserId == -1 || currentProductId == -1 || otherUserId == -1) {
            Toast.makeText(this, "채팅 정보를 불러올 수 없습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "필수 채팅 정보 부족: currentUserId=" + currentUserId + ", productId=" + currentProductId + ", otherUserId=" + otherUserId);
            finish();
            return;
        }


        apiService = RetrofitClient.getInstance().getApiService();

        recycler_view_chat = findViewById(R.id.recycler_view_chat);
        iv_back = findViewById(R.id.iv_back);
        et_message = findViewById(R.id.et_message);
        btn_send = findViewById(R.id.btn_send);
        btn_location = findViewById(R.id.btn_location);
        tv_chat_title = findViewById(R.id.tv_chat_title);

        tv_chat_title.setText(chatTitle);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recycler_view_chat.setLayoutManager(layoutManager);

        messageAdapter = new MessageAdapter(this, messageList, currentUserId);
        recycler_view_chat.setAdapter(messageAdapter);

        loadLocattionDetails(currentProductId);

        loadMessages();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageContent = et_message.getText().toString().trim();
                if (!messageContent.isEmpty()) {
                    sendMessage(messageContent, 0.0, 0.0, null); // 일반 메시지 전송
                    et_message.setText("");
                }
            }
        });

        btn_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post != null) {
                    Toast.makeText(ChatActivity.this,
                            "위도: " + String.format("%.4f", post.getLatitude()) +
                                    ", 경도: " + String.format("%.4f", post.getLongitude()),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMessages(); // 액티비티가 다시 활성화될 때마다 메시지 로드
    }

    //위치 정보를 불러오기 위한 메소드
    private void loadLocattionDetails(int productId) {
        apiService.getPost(productId).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    post = response.body(); // PostResponse 객체 초기화
                    Log.d(TAG, "상품 상세 정보 로드 성공: " + post.getTitle());
                    // 여기서 로드된 위치 정보로 UI 업데이트를 할 수도 있습니다.
                } else {
                    String errorMsg = "상품 상세 정보 로드 실패: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBodyString = response.errorBody().string();
                            errorMsg += ", 에러 바디: " + errorBodyString;
                            Log.e(TAG, "상품 상세 정보 로드 실패 - 에러 바디: " + errorBodyString);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "상품 상세 정보 로드 실패 에러 바디 읽기 중 예외 발생", e);
                        errorMsg += ", 에러 바디 읽기 실패: " + e.getMessage();
                    }
                    Toast.makeText(ChatActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                String failureMsg = "상품 상세 정보 로드 네트워크 실패: " + t.getMessage();
                Toast.makeText(ChatActivity.this, failureMsg, Toast.LENGTH_LONG).show();
                Log.e(TAG, "상품 상세 정보 로드 onFailure: " + t.getMessage(), t);
            }
        });
    }

    private void sendMessage(String content, double latitude, double longitude, String locationName) {
        MessageSendRequest request;


        if (content != null && !content.isEmpty()) {
            request = new MessageSendRequest(currentProductId, currentUserId, otherUserId, content);
        } else if (locationName != null && !locationName.isEmpty()) {
            request = new MessageSendRequest(currentProductId, currentUserId, otherUserId, latitude, longitude, locationName);
        } else {
            Toast.makeText(this, "전송할 메시지 내용이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.sendMessage(request).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Message sentMessage = response.body();


                            // 서버에서 받은 메시지에 타입 설정
                    if (sentMessage.getLocationName() != null && !sentMessage.getLocationName().isEmpty()) {
                        sentMessage.setMessageType(Message.TYPE_LOCATION);
                        sentMessage.setLocationMessage(true);
                    } else if (sentMessage.getSenderId() == currentUserId) {
                        sentMessage.setMessageType(Message.TYPE_ME);
                    } else {
                        sentMessage.setMessageType(Message.TYPE_OTHER);
                    }

                    messageAdapter.addMessage(sentMessage);
                    recycler_view_chat.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                    Log.d(TAG, "메시지 전송 성공: " + sentMessage.getContent());
                } else {
                    String errorMsg = "메시지 전송 실패: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "메시지 전송 실패 에러 바디 읽기 실패", e);
                    }
                    Toast.makeText(ChatActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, errorMsg);
                }
            }



            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "서버 연결 오류로 메시지 전송 실패: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "메시지 전송 onFailure: " + t.getMessage(), t);
            }
        });
    }

    private void loadMessages() {
        apiService.getMessagesBetweenUsers(currentProductId, currentUserId, otherUserId)
                .enqueue(new Callback<List<Message>>() {
                    @Override
                    public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                        if (response.isSuccessful() && response.body() != null) {

                            List<Message> fetchedMessages = response.body();

                            // 서버에서 메시지가 아예 없는 경우를 대비하여 null 체크
                            if (fetchedMessages == null) {
                                fetchedMessages = new ArrayList<>();
                            }

                            // 메시지 타입 설정 (기존 로직 유지)
                            for (Message msg : fetchedMessages) {
                                if (msg.getLocationName() != null && !msg.getLocationName().isEmpty()) {
                                    msg.setMessageType(Message.TYPE_LOCATION);
                                    msg.setLocationMessage(true);
                                } else if (msg.getSenderId() == currentUserId) {
                                    msg.setMessageType(Message.TYPE_ME);
                                } else {
                                    msg.setMessageType(Message.TYPE_OTHER);
                                }
                            }

                            // 메시지 리스트 업데이트 및 어댑터에 알림
                            messageList.clear(); // 기존 메시지 삭제
                            messageList.addAll(fetchedMessages); // 새 메시지 추가
                            messageAdapter.notifyDataSetChanged(); // 어댑터 전체 갱신

                            // ***** 핵심 수정 부분: 메시지가 있을 때만 스크롤 *****
                            if (!messageList.isEmpty()) { // messageList.size() > 0 과 동일
                                recycler_view_chat.smoothScrollToPosition(messageList.size() - 1);
                                Log.d(TAG, "메시지 로드 성공 및 스크롤: " + messageList.size() + "개");
                            } else {
                                Log.d(TAG, "메시지 로드 성공: 메시지 없음.");
                                // 메시지가 없을 경우 스크롤하지 않음 (오류 방지)
                            }
                            // *************************************************

                        } else {
                            String errorMsg = "메시지 로드 실패: 응답 코드 " + response.code();
                            try {
                                if (response.errorBody() != null) {
                                    String errorBodyString = response.errorBody().string();
                                    errorMsg += ", 에러 바디: " + errorBodyString;
                                    Log.e(TAG, "메시지 로드 실패 - 에러 바디: " + errorBodyString);
                                }
                            } catch (IOException e) {
                                Log.e(TAG, "메시지 로드 실패 에러 바디 읽기 중 예외 발생", e);
                                errorMsg += ", 에러 바디 읽기 실패: " + e.getMessage();
                            }
                            Toast.makeText(ChatActivity.this, errorMsg, Toast.LENGTH_LONG).show(); // 사용자에게 좀 더 자세한 메시지
                            Log.e(TAG, errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Message>> call, Throwable t) {
                        Toast.makeText(ChatActivity.this, "서버 연결 오류로 메시지 로드 실패: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "메시지 로드 onFailure: " + t.getMessage(), t);
                    }
                });
    }
}