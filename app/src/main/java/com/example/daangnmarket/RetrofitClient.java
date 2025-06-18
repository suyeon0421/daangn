package com.example.daangnmarket;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://swu-carrot.replit.app/";
    private static RetrofitClient instance = null;
    private Retrofit retrofit;
    private ApiService apiService;
    private static Context applicationContext; // 애플리케이션 Context를 저장할 변수

    // 로그인 토큰을 SharedPreferences에서 가져올 때 사용할 키
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_AUTH_TOKEN = "authToken";

    // Application Context를 초기화하는 메서드
    public static void initialize(Context context) {
        applicationContext = context.getApplicationContext();
    }

    private RetrofitClient() {
        if (applicationContext == null) {
            throw new IllegalStateException("RetrofitClient must be initialized with application context using initialize() method.");
        }

        // HTTP 로깅 인터셉터 설정
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 인증 토큰을 추가하는 인터셉터
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request.Builder requestBuilder = originalRequest.newBuilder();

                // SharedPreferences에서 토큰 가져오기
                SharedPreferences sharedPreferences = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                String authToken = sharedPreferences.getString(KEY_AUTH_TOKEN, null);


                // 토큰이 존재하면 Authorization 헤더에 추가
                if (authToken != null && !authToken.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + authToken);
                }

                Request newRequest = requestBuilder.build();
                return chain.proceed(newRequest);
            }
        };

        // OkHttpClient 생성: 로깅 인터셉터와 인증 인터셉터 추가
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor) // 로깅 인터셉터 (디버깅용)
                .addInterceptor(authInterceptor) // 인증 인터셉터 (토큰 추가)
                .build();

        // retrofit 인스턴스 생성
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    // RetrofitClient 인스턴스를 반환한다.
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }
}