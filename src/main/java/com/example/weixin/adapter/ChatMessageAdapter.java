package com.example.weixin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weixin.ChatMessageModel;
import com.example.weixin.R;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private List<ChatMessageModel> messageList;

    public ChatMessageAdapter(List<ChatMessageModel> messageList) {
        this.messageList = messageList;
    }

    public void addMessage(ChatMessageModel message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    public void updateMessageStatus(String messageId, int status) {
        for (int i = 0; i < messageList.size(); i++) {
            ChatMessageModel message = messageList.get(i);
            if (message.getMessageId().equals(messageId)) {
                message.setStatus(status);
                notifyItemChanged(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_message_item, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessageModel message = messageList.get(position);

        if (message.isIncoming()) {
            // 显示接收消息
            holder.receivedLayout.setVisibility(View.VISIBLE);
            holder.sentLayout.setVisibility(View.GONE);
            
            holder.receivedName.setText(message.getSenderName());
            holder.receivedMessage.setText(message.getContent());
            holder.receivedTime.setText(message.getTime());
        } else {
            // 显示发送消息
            holder.receivedLayout.setVisibility(View.GONE);
            holder.sentLayout.setVisibility(View.VISIBLE);
            
            holder.sentMessage.setText(message.getContent());
            holder.sentTime.setText(message.getTime());
            
            // 设置消息状态
            switch (message.getStatus()) {
                case ChatMessageModel.STATUS_SENDING:
                    holder.messageStatus.setText("发送中...");
                    break;
                case ChatMessageModel.STATUS_SENT:
                    holder.messageStatus.setText("已发送");
                    break;
                case ChatMessageModel.STATUS_DELIVERED:
                    holder.messageStatus.setText("已送达");
                    break;
                case ChatMessageModel.STATUS_FAILED:
                    holder.messageStatus.setText("发送失败");
                    holder.messageStatus.setTextColor(0xFFFF0000);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout receivedLayout, sentLayout;
        TextView receivedName, receivedMessage, receivedTime;
        TextView sentMessage, sentTime, messageStatus;
        ImageView receivedAvatar, sentAvatar;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // 接收消息视图
            receivedLayout = itemView.findViewById(R.id.received_message_layout);
            receivedName = itemView.findViewById(R.id.received_name);
            receivedMessage = itemView.findViewById(R.id.received_message);
            receivedTime = itemView.findViewById(R.id.received_time);
            receivedAvatar = itemView.findViewById(R.id.received_avatar);
            
            // 发送消息视图
            sentLayout = itemView.findViewById(R.id.sent_message_layout);
            sentMessage = itemView.findViewById(R.id.sent_message);
            sentTime = itemView.findViewById(R.id.sent_time);
            messageStatus = itemView.findViewById(R.id.message_status);
            sentAvatar = itemView.findViewById(R.id.sent_avatar);
        }
    }
} 