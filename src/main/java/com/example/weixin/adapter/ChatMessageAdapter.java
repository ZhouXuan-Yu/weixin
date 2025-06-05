package com.example.weixin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weixin.R;
import com.example.weixin.model.ChatMessage;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private final List<ChatMessage> messageList;
    private final Context context;

    public ChatMessageAdapter(Context context, List<ChatMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_item, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        // 重置所有视图的可见性
        holder.sentLayout.setVisibility(View.GONE);
        holder.receivedLayout.setVisibility(View.GONE);
        holder.sentMessage.setVisibility(View.GONE);
        holder.receivedMessage.setVisibility(View.GONE);
        holder.sentImage.setVisibility(View.GONE);
        holder.receivedImage.setVisibility(View.GONE);

        if (message.getType() == ChatMessage.TYPE_SENT) {
            // 发送的消息
            holder.sentLayout.setVisibility(View.VISIBLE);
            
            if (message.isImage()) {
                // 显示图片
                holder.sentImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(message.getImageFile())
                        .into(holder.sentImage);
            } else {
                // 显示文本
                holder.sentMessage.setVisibility(View.VISIBLE);
                holder.sentMessage.setText(message.getMessage());
            }
        } else {
            // 接收的消息
            holder.receivedLayout.setVisibility(View.VISIBLE);
            
            if (message.isImage()) {
                // 显示图片
                holder.receivedImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(message.getImageFile())
                        .into(holder.receivedImage);
            } else {
                // 显示文本
                holder.receivedMessage.setVisibility(View.VISIBLE);
                holder.receivedMessage.setText(message.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void addMessage(ChatMessage message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout sentLayout, receivedLayout;
        TextView sentMessage, receivedMessage;
        ImageView sentImage, receivedImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sentLayout = itemView.findViewById(R.id.sent_layout);
            receivedLayout = itemView.findViewById(R.id.received_layout);
            sentMessage = itemView.findViewById(R.id.sent_message);
            receivedMessage = itemView.findViewById(R.id.received_message);
            sentImage = itemView.findViewById(R.id.sent_image);
            receivedImage = itemView.findViewById(R.id.received_image);
        }
    }
} 