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
import com.example.daangnmarket.activity.MyPostDetailActivity;
import com.example.daangnmarket.models.PostResponse;

import java.util.ArrayList;

public class MyPostAdapter extends RecyclerView.Adapter<MyPostAdapter.ViewHolder> {
    private Context context;
    private ArrayList<PostResponse> myPostList;

    public MyPostAdapter(Context context, ArrayList<PostResponse> myPostList) {
        this.context = context;
        this.myPostList = myPostList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView tv_title, tv_location, tv_price;

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
        PostResponse post = myPostList.get(position);

        holder.tv_title.setText(post.getTitle());
        holder.tv_location.setText(post.getLocation_name());
        holder.tv_price.setText(String.format("%,d원", post.getPrice()));

        String baseUrl = "https://swu-carrot.replit.app/"; // 실제 API 서버 주소로 교체
        String imageUrl = post.getImage_url();  // "/uploads/product_images/...."

        if (imageUrl != null && !imageUrl.isEmpty()) {
            String fullUrl = baseUrl + imageUrl;

            Glide.with(context)
                    .load(fullUrl)
                    .placeholder(R.drawable.default_image)
                    .into(holder.imageProduct);
        } else {
            holder.imageProduct.setImageResource(R.drawable.default_image);
        }



        // 클릭 이벤트 한 번만 등록하고, 필요한 데이터만 넘김
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MyPostDetailActivity.class);
            intent.putExtra("myPost", post);
            context.startActivity(intent); // startActivity 실행 필수
        });
    }

    @Override
    public int getItemCount() {
        return myPostList.size();
    }
}
