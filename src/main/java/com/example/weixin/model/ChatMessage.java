package com.example.weixin.model;

import java.io.File;

public class ChatMessage {
    public static final int TYPE_SENT = 0;
    public static final int TYPE_RECEIVED = 1;
    
    private int type;
    private String message;
    private File imageFile;
    private boolean isImage;
    private long timestamp;
    
    public ChatMessage(int type, String message) {
        this.type = type;
        this.message = message;
        this.isImage = false;
        this.timestamp = System.currentTimeMillis();
    }
    
    public ChatMessage(int type, File imageFile) {
        this.type = type;
        this.imageFile = imageFile;
        this.isImage = true;
        this.timestamp = System.currentTimeMillis();
    }
    
    public int getType() {
        return type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public File getImageFile() {
        return imageFile;
    }
    
    public boolean isImage() {
        return isImage;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
} 