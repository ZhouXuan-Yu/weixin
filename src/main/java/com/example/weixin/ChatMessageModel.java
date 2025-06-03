package com.example.weixin;

public class ChatMessageModel {
    public static final int STATUS_SENDING = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_DELIVERED = 2;
    public static final int STATUS_FAILED = 3;
    
    private String messageId;
    private String content;
    private String time;
    private boolean isIncoming;
    private String senderName;
    private String senderIp;
    private int status;
    
    public ChatMessageModel(String messageId, String content, String time, boolean isIncoming, 
                           String senderName, String senderIp, int status) {
        this.messageId = messageId;
        this.content = content;
        this.time = time;
        this.isIncoming = isIncoming;
        this.senderName = senderName;
        this.senderIp = senderIp;
        this.status = status;
    }
    
    // Getters and setters
    public String getMessageId() {
        return messageId;
    }
    
    public String getContent() {
        return content;
    }
    
    public String getTime() {
        return time;
    }
    
    public boolean isIncoming() {
        return isIncoming;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public String getSenderIp() {
        return senderIp;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
} 