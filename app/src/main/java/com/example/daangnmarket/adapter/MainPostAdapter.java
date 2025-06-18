package com.example.daangnmarket.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.daangnmarket.R;
import com.example.daangnmarket.activity.PostDetailActivity;
import com.example.daangnmarket.models.PostResponse;

import java.util.ArrayList;

public class MainPostAdapter extends RecyclerView.Adapter<MainPostAdapter.ViewHolder> {
    private Context context;
    private ArrayList<PostResponse> mainPostList;

    public MainPostAdapter(Context context, ArrayList<PostResponse> mainPostList) {
        this.context = context;
        this.mainPostList = mainPostList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView tv_title, tv_location, tv_price;
        String sellerName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.iv_product_image);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_location = itemView.findViewById(R.id.tv_location);
            tv_price = itemView.findViewById(R.id.tv_price);

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostResponse post = mainPostList.get(position);

        holder.tv_title.setText(post.getTitle());
        holder.tv_location.setText(post.getLocation_name());
        holder.tv_price.setText(post.getPrice() + "원"); // int를 문자열로 변환

        String baseUrl = "https://swu-carrot.replit.app/";
        String imageUrl = post.getImage_url();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            String fullUrl = baseUrl + imageUrl;

            Glide.with(context)
                    .load(fullUrl)
                    .placeholder(R.drawable.default_image)
                    .into(holder.imageProduct);
        } else {
            holder.imageProduct.setImageResource(R.drawable.default_image);
        }

        // 각 게시물 항목 클릭 시 상세 화면으로 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            // PostResponse 객체 전체 대신, 게시물 ID만 전달
            intent.putExtra("product_id", post.getId()); // <-- 여기 수정
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return mainPostList.size();
    }
}