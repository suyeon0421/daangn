package com.example.daangnmarket.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daangnmarket.R;
import com.example.daangnmarket.models.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private ArrayList<Message> messageList;
    private int currentUserId;

    public MessageAdapter(Context context, ArrayList<Message> messageList, int currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutMyMessage;
        TextView tvMyMessage;
        TextView tvMyTime;

        LinearLayout layoutOtherMessage;
        TextView tvOtherMessage;
        TextView tvOtherTime;

        LinearLayout layoutLocationMessage;
        ImageView ivLocationIcon;
        TextView tvLocation;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutMyMessage = itemView.findViewById(R.id.layout_my_message);
            tvMyMessage = itemView.findViewById(R.id.tv_my_message);
            tvMyTime = itemView.findViewById(R.id.tv_my_time);

            layoutOtherMessage = itemView.findViewById(R.id.layout_other_message);
            tvOtherMessage = itemView.findViewById(R.id.tv_other_message);
            tvOtherTime = itemView.findViewById(R.id.tv_other_time);

            layoutLocationMessage = itemView.findViewById(R.id.layout_location_message);
            ivLocationIcon = layoutLocationMessage.findViewById(android.R.id.icon);
            tvLocation = itemView.findViewById(R.id.tv_location);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        // 모든 레이아웃 숨김
        holder.layoutMyMessage.setVisibility(View.GONE);
        holder.layoutOtherMessage.setVisibility(View.GONE);
        holder.layoutLocationMessage.setVisibility(View.GONE);

        String timestamp = message.getTimestamp();
        String formattedTime = "";

        try {
            // 서버에서 받은 시간은 UTC라고 가정
            SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            serverFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            // 한국 시간으로 변환
            SimpleDateFormat displayFormat = new SimpleDateFormat("a hh:mm", Locale.KOREA);
            displayFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            Date date = serverFormat.parse(timestamp);
            formattedTime = displayFormat.format(date);
        } catch (Exception e) {
            formattedTime = timestamp; // 파싱 실패 시 원본 출력
        }

        if (message.isLocationMessage()) {
            holder.layoutLocationMessage.setVisibility(View.VISIBLE);
            holder.tvLocation.setText(message.getLocationName());
        } else if (message.getSenderId() == currentUserId) {
            // 내 메시지
            holder.layoutMyMessage.setVisibility(View.VISIBLE);
            holder.tvMyMessage.setText(message.getContent());
            holder.tvMyTime.setText(formattedTime);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.layoutMyMessage.getLayoutParams();
            params.gravity = Gravity.END;
            holder.layoutMyMessage.setLayoutParams(params);
        } else {
            // 상대방 메시지
            holder.layoutOtherMessage.setVisibility(View.VISIBLE);
            holder.tvOtherMessage.setText(message.getContent());
            holder.tvOtherTime.setText(formattedTime);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.layoutOtherMessage.getLayoutParams();
            params.gravity = Gravity.START;
            holder.layoutOtherMessage.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void addMessage(Message message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    public void setMessages(List<Message> newMessages) {
        messageList.clear();
        messageList.addAll(newMessages);
        notifyDataSetChanged();
    }
}
