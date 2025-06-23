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
    private ArrayList<Message> messageList; // 메시지 데이터 리스트
    private int currentUserId; // 현재 사용자 ID (보낸 사람 판단용)

    public MessageAdapter(Context context, ArrayList<Message> messageList, int currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    // 뷰홀더 클래스: 뷰를 재활용하기 위한 구조 (배터리 및 메모리 효율에 도움)
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

        holder.layoutMyMessage.setVisibility(View.GONE);
        holder.layoutOtherMessage.setVisibility(View.GONE);
        holder.layoutLocationMessage.setVisibility(View.GONE);

        String timestamp = message.getTimestamp();
        String formattedTime = "";

        try {
            // 서버 시간 (UTC) -> 한국 시간(KST) 포맷팅
            SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            serverFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat displayFormat = new SimpleDateFormat("a hh:mm", Locale.KOREA);
            displayFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            Date date = serverFormat.parse(timestamp);
            formattedTime = displayFormat.format(date);
        } catch (Exception e) {
            formattedTime = timestamp; // 파싱 실패 시 원본 출력
        }

        if (message.isLocationMessage()) {
            // 위치 메시지일 경우 아이콘 + 위치명 표시
            holder.layoutLocationMessage.setVisibility(View.VISIBLE);
            holder.tvLocation.setText(message.getLocationName());
        } else if (message.getSenderId() == currentUserId) {
            // 본인 메시지 (오른쪽 정렬)
            holder.layoutMyMessage.setVisibility(View.VISIBLE);
            holder.tvMyMessage.setText(message.getContent());
            holder.tvMyTime.setText(formattedTime);

            // 정렬을 위해 Gravity 설정 (효율적인 UI 구성)
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.layoutMyMessage.getLayoutParams();
            params.gravity = Gravity.END;
            holder.layoutMyMessage.setLayoutParams(params);
        } else {
            // 상대방 메시지 (왼쪽 정렬)
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
        return messageList.size(); // 데이터 개수 반환 (RecyclerView 최적화 핵심)
    }

    // 메시지 추가 시 리스트에 삽입 및 어댑터 알림
    public void addMessage(Message message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1); // 성능상 전체 갱신 대신 부분 갱신
    }

    // 메시지 리스트 전체 갱신 (드물게 사용)
    public void setMessages(List<Message> newMessages) {
        messageList.clear();
        messageList.addAll(newMessages);
        notifyDataSetChanged(); // 전체 변경 알림 (배터리 효율 고려 시 빈번한 호출은 지양)
    }
}
