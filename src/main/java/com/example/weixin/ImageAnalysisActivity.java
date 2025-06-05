package com.example.weixin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weixin.adapter.ChatMessageAdapter;
import com.example.weixin.model.ChatMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageAnalysisActivity extends AppCompatActivity {

    private static final String TAG = "ImageAnalysisActivity";
    private static final String API_KEY = "sk-8ecbfb7922bc425bafb971616f5a7674";
    
    // 更新为兼容模式URL
    private static final String DASHSCOPE_API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    
    // 当前使用的API URL - 如果DashScope不可用，可以切换到OpenAI
    private static final String API_URL = DASHSCOPE_API_URL;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private ImageView btnBack;
    private RecyclerView chatRecyclerView;
    private EditText inputMessage;
    private ImageButton btnSend, btnUploadImage;
    private ProgressBar progressBar;
    private TextView questionAuthor, questionStory, questionReview;

    private final List<ChatMessage> messageList = new ArrayList<>();
    private ChatMessageAdapter adapter;
    private File currentImageFile = null;
    private String currentImageBase64 = null;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // 使用更高效的线程池配置
    private final ExecutorService executor = new ThreadPoolExecutor(
            1, // 核心线程数
            2, // 最大线程数 
            60L, // 空闲线程存活时间
            TimeUnit.SECONDS, // 时间单位
            new LinkedBlockingQueue<>(), // 工作队列
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
    );
    
    // OkHttp客户端
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    
    // 活动结果处理器
    private final ActivityResultLauncher<Intent> imagePickerLauncher = 
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), 
                    result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    handleSelectedImage(selectedImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_analysis);

        initViews();
        setupRecyclerView();
        setupListeners();
        
        // 添加欢迎消息
        addReceivedMessage("你好！我是郑工智能体，请上传一张图片，我可以帮你分析它。");
        
        // 检查API配置
        checkApiConfiguration();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        inputMessage = findViewById(R.id.input_message);
        btnSend = findViewById(R.id.btn_send);
        btnUploadImage = findViewById(R.id.btn_upload_image);
        progressBar = findViewById(R.id.progress_bar);
        
        // 预设问题按钮
        questionAuthor = findViewById(R.id.question_author);
        questionStory = findViewById(R.id.question_story);
        questionReview = findViewById(R.id.question_review);
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter(this, messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // 输入框监听器
        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 当输入框有文本时显示发送按钮，否则显示语音按钮
                btnSend.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 发送按钮点击事件
        btnSend.setOnClickListener(v -> {
            String message = inputMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                sendMessage(message);
            }
        });

        // 上传图片按钮点击事件
        btnUploadImage.setOnClickListener(v -> openImagePicker());
        
        // 预设问题点击事件
        questionAuthor.setOnClickListener(v -> {
            if (currentImageFile != null) {
                sendMessage(questionAuthor.getText().toString());
            } else {
                Toast.makeText(this, "请先上传图片", Toast.LENGTH_SHORT).show();
            }
        });
        
        questionStory.setOnClickListener(v -> {
            if (currentImageFile != null) {
                sendMessage(questionStory.getText().toString());
            } else {
                Toast.makeText(this, "请先上传图片", Toast.LENGTH_SHORT).show();
            }
        });
        
        questionReview.setOnClickListener(v -> {
            if (currentImageFile != null) {
                sendMessage(questionReview.getText().toString());
            } else {
                Toast.makeText(this, "请先上传图片", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void handleSelectedImage(Uri imageUri) {
        try {
            // 将选择的图片转换为文件
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            
            // 检查图像尺寸，如果太大则调整大小
            bitmap = resizeBitmapIfNeeded(bitmap, 1280); // 最大宽度1280像素
            
            File imageFile = createImageFile(bitmap);
            
            // 更新当前图片文件
            currentImageFile = imageFile;
            
            // 转换为Base64
            currentImageBase64 = bitmapToBase64(bitmap);
            
            // 添加图片消息
            addSentImageMessage(imageFile);
            
            // 提示用户输入问题 - 不再自动发送分析请求
            addReceivedMessage("图片已上传。请输入您想问的问题，然后点击发送按钮。");
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to process selected image", e);
            Toast.makeText(this, "处理图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    // 调整图像大小以减小编码后的数据量
    private Bitmap resizeBitmapIfNeeded(Bitmap bitmap, int maxWidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        Log.d(TAG, "原始图片尺寸: " + width + "x" + height);
        
        // 计算原始大小（以KB为单位）
        int originalSizeKB = estimateBitmapSizeKB(bitmap);
        Log.d(TAG, "原始图片大小约为: " + originalSizeKB + "KB");
        
        // 如果图片已经足够小，不需要调整
        if (width <= maxWidth && originalSizeKB <= 1024) {
            return bitmap;
        }
        
        // 计算缩放比例
        float scale = (float) maxWidth / width;
        
        // 如果图片非常大，可能需要更激进的缩放
        if (originalSizeKB > 4096) { // 大于4MB
            scale = scale * 0.5f; // 额外缩小50%
        } else if (originalSizeKB > 2048) { // 大于2MB
            scale = scale * 0.7f; // 额外缩小30%
        }
        
        int targetWidth = Math.round(width * scale);
        int targetHeight = Math.round(height * scale);
        
        // 确保宽度不超过最大值
        if (targetWidth > maxWidth) {
            float adjustScale = (float) maxWidth / targetWidth;
            targetWidth = maxWidth;
            targetHeight = Math.round(targetHeight * adjustScale);
        }
        
        Log.d(TAG, "调整后的图片尺寸: " + targetWidth + "x" + targetHeight);
        
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        
        // 计算调整后的大小
        int newSizeKB = estimateBitmapSizeKB(resizedBitmap);
        Log.d(TAG, "调整后的图片大小约为: " + newSizeKB + "KB");
        
        return resizedBitmap;
    }
    
    // 估算Bitmap大小（KB）
    private int estimateBitmapSizeKB(Bitmap bitmap) {
        return bitmap.getAllocationByteCount() / 1024;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        
        try {
            // 使用JPEG格式和适当的压缩率
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream);
            
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            // 使用NO_WRAP避免换行，确保Base64字符串是连续的
            String base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP);
            
            // 检查是否有任何非法字符
            if (base64String.contains("\n") || base64String.contains("\r")) {
                Log.w(TAG, "Base64字符串包含换行符，可能影响API请求");
                base64String = base64String.replaceAll("[\n\r]", "");
            }
            
            // 记录Base64字符串的长度，用于调试
            Log.d(TAG, "Base64字符串长度: " + base64String.length());
            
            return base64String;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "关闭ByteArrayOutputStream出错", e);
            }
        }
    }

    private File createImageFile(Bitmap bitmap) throws IOException {
        String fileName = "img_" + UUID.randomUUID().toString() + ".jpg";
        File file = new File(getExternalFilesDir(null), fileName);
        
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        fos.flush();
        fos.close();
        
        return file;
    }

    private void sendMessage(String message) {
        // 清空输入框
        inputMessage.setText("");
        
        // 添加发送的消息到列表
        addSentMessage(message);
        
        // 如果已上传图片，则发送到API进行分析
        if (currentImageFile != null) {
            analyzeImage(message);
        } else {
            addReceivedMessage("请先上传一张图片，我才能回答您的问题。");
        }
    }
    
    private void addSentMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(ChatMessage.TYPE_SENT, message);
        adapter.addMessage(chatMessage);
        scrollToBottom();
    }
    
    private void addSentImageMessage(File imageFile) {
        ChatMessage chatMessage = new ChatMessage(ChatMessage.TYPE_SENT, imageFile);
        adapter.addMessage(chatMessage);
        scrollToBottom();
    }
    
    private void addReceivedMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(ChatMessage.TYPE_RECEIVED, message);
        adapter.addMessage(chatMessage);
        scrollToBottom();
    }
    
    private void scrollToBottom() {
        chatRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
    }

    private void analyzeImage(String question) {
        if (currentImageFile == null || currentImageBase64 == null) {
            addReceivedMessage("无法处理图像，请重新上传。");
            return;
        }

        // 显示进度条
        progressBar.setVisibility(View.VISIBLE);
        
        // 添加等待提示消息
        addReceivedMessage("正在分析图像，请稍候...");

        // 在后台线程执行API调用
        executor.execute(() -> {
            try {
                Log.d(TAG, "开始分析图像，问题: " + question);
                long startTime = System.currentTimeMillis();
                
                String result = performImageAnalysis(currentImageBase64, question);
                
                long endTime = System.currentTimeMillis();
                Log.d(TAG, "图像分析完成，耗时: " + (endTime - startTime) + "ms");
                
                mainHandler.post(() -> {
                    // 移除之前的等待消息
                    messageList.remove(messageList.size() - 1);
                    adapter.notifyItemRemoved(messageList.size());
                    
                    // 添加结果消息
                    addReceivedMessage(result);
                    progressBar.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                Log.e(TAG, "图像分析失败", e);
                final String errorMsg = e.getMessage();
                mainHandler.post(() -> {
                    // 移除之前的等待消息
                    messageList.remove(messageList.size() - 1);
                    adapter.notifyItemRemoved(messageList.size());
                    
                    // 添加错误消息并进行分类处理
                    String errorInfo = "分析失败";
                    
                    if (errorMsg.contains("400")) {
                        if (errorMsg.contains("InvalidParameter") || errorMsg.contains("modal")) {
                            errorInfo += "：API请求格式错误。错误详情：" + errorMsg;
                            Log.e(TAG, "请求格式错误: " + errorMsg);
                        } else {
                            errorInfo += "：参数错误。错误详情：" + errorMsg;
                        }
                    } else if (errorMsg.contains("401") || errorMsg.contains("403")) {
                        errorInfo += "：API密钥无效或未授权访问。请检查您的API密钥。";
                    } else if (errorMsg.contains("404")) {
                        errorInfo += "：API服务未找到。请确认API地址正确。";
                    } else if (errorMsg.contains("413") || errorMsg.contains("payload")) {
                        errorInfo += "：图像文件过大。请上传较小的图像或降低图像质量。";
                    } else if (errorMsg.contains("429")) {
                        errorInfo += "：请求频率过高。请稍后再试。";
                    } else if (errorMsg.contains("500") || errorMsg.contains("502") || errorMsg.contains("503")) {
                        errorInfo += "：服务器错误。请稍后再试。";
                    } else if (errorMsg.contains("timeout") || errorMsg.contains("timed out")) {
                        errorInfo += "：请求超时。请检查网络连接。";
                    } else {
                        errorInfo += "：" + errorMsg;
                    }
                    
                    addReceivedMessage(errorInfo);
                    progressBar.setVisibility(View.GONE);
                    
                    // 提供用户操作建议
                    if (errorMsg.contains("InvalidParameter") || errorMsg.contains("modal")) {
                        addReceivedMessage("提示：这是API请求格式问题，请联系开发人员修复。");
                    } else if (errorMsg.contains("413") || errorMsg.contains("payload") || 
                            errorMsg.contains("too large")) {
                        addReceivedMessage("提示：您可以尝试上传较小的图片或降低图片质量后再试。");
                    }
                });
            }
        });
    }

    private String performImageAnalysis(String imageBase64, String question) throws IOException, JSONException {
        // 根据当前使用的API选择不同的请求构建方法
        if (API_URL.equals(DASHSCOPE_API_URL)) {
            return performDashScopeImageAnalysis(imageBase64, question);
        } else {
            return performOpenAIImageAnalysis(imageBase64, question);
        }
    }
    
    // DashScope API 调用实现
    private String performDashScopeImageAnalysis(String imageBase64, String question) throws IOException, JSONException {
        // 构建请求JSON - 完全按照官方Python示例格式
        JSONObject jsonRequest = new JSONObject();
        
        // 模型参数
        jsonRequest.put("model", "qwen-vl-max");
        
        // 构建消息数组
        JSONArray messagesArray = new JSONArray();
        
        // 系统消息 - 使用数组形式的content
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        JSONArray systemContentArray = new JSONArray();
        JSONObject systemTextObj = new JSONObject();
        systemTextObj.put("type", "text");
        systemTextObj.put("text", "You are a helpful assistant that analyzes images and answers questions about them in Chinese.");
        systemContentArray.put(systemTextObj);
        systemMessage.put("content", systemContentArray);
        messagesArray.put(systemMessage);
        
        // 用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        
        // 构建用户内容数组 - 图片和文本分开
        JSONArray userContentArray = new JSONArray();
        
        // 图片部分 - 使用image_url对象格式
        JSONObject imageUrlObj = new JSONObject();
        imageUrlObj.put("type", "image_url");
        JSONObject urlObj = new JSONObject();
        urlObj.put("url", "data:image/jpeg;base64," + imageBase64);
        imageUrlObj.put("image_url", urlObj);
        userContentArray.put(imageUrlObj);
        
        // 文本部分
        JSONObject textObj = new JSONObject();
        textObj.put("type", "text");
        textObj.put("text", question);
        userContentArray.put(textObj);
        
        userMessage.put("content", userContentArray);
        messagesArray.put(userMessage);
        
        // 添加消息到请求
        jsonRequest.put("messages", messagesArray);
        
        String requestString = jsonRequest.toString();
        // 仅打印请求结构，不打印完整的Base64以避免日志过长
        String logRequestString = requestString.replaceAll("(data:image\\/jpeg;base64,)[^\"]+", "$1[BASE64_DATA]");
        Log.d(TAG, "DashScope请求结构: " + logRequestString);
        
        // 构建HTTP请求
        RequestBody requestBody = RequestBody.create(requestString, JSON);
        Request request = new Request.Builder()
                .url(DASHSCOPE_API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-DashScope-SSE", "disable")  // 禁用服务器发送事件，使用普通响应
                .post(requestBody)
                .build();
        
        // 执行请求
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "No response body";
            
            if (!response.isSuccessful()) {
                Log.e(TAG, "API请求失败: " + response.code() + " " + responseBody);
                throw new IOException("API调用失败: " + response.code() + " " + responseBody);
            }
            
            Log.d(TAG, "API响应: " + responseBody);
            
            try {
                // 解析响应 - 兼容模式格式与OpenAI格式一致
                JSONObject jsonResponse = new JSONObject(responseBody);
                
                if (jsonResponse.has("choices") && jsonResponse.getJSONArray("choices").length() > 0) {
                    JSONObject choice = jsonResponse.getJSONArray("choices").getJSONObject(0);
                    if (choice.has("message")) {
                        JSONObject message = choice.getJSONObject("message");
                        if (message.has("content")) {
                            return message.getString("content");
                        }
                    }
                }
                
                // 如果没有找到预期的结构，返回原始响应
                return "无法解析API响应: " + responseBody;
            } catch (JSONException e) {
                Log.e(TAG, "解析JSON响应失败", e);
                return "解析响应失败: " + e.getMessage() + "\n原始响应: " + responseBody;
            }
        } catch (Exception e) {
            Log.e(TAG, "API请求失败", e);
            return "API请求失败: " + e.getMessage();
        }
    }
    
    // OpenAI API 调用实现
    private String performOpenAIImageAnalysis(String imageBase64, String question) throws IOException, JSONException {
        // 构建请求JSON
        JSONObject jsonRequest = new JSONObject();
        
        // 模型参数
        jsonRequest.put("model", "gpt-4-vision-preview");
        jsonRequest.put("max_tokens", 1000);
        
        // 构建消息数组
        JSONArray messagesArray = new JSONArray();
        
        // 系统消息
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个专业的图像分析助手，用中文回答关于图片的问题。");
        messagesArray.put(systemMessage);
        
        // 用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        
        // 构建用户内容数组
        JSONArray contentArray = new JSONArray();
        
        // 文本部分
        JSONObject textContent = new JSONObject();
        textContent.put("type", "text");
        textContent.put("text", question);
        contentArray.put(textContent);
        
        // 图片部分
        JSONObject imageContent = new JSONObject();
        imageContent.put("type", "image_url");
        
        JSONObject imageUrl = new JSONObject();
        imageUrl.put("url", "data:image/jpeg;base64," + imageBase64);
        
        imageContent.put("image_url", imageUrl);
        contentArray.put(imageContent);
        
        userMessage.put("content", contentArray);
        messagesArray.put(userMessage);
        
        // 添加消息到请求
        jsonRequest.put("messages", messagesArray);
        
        Log.d(TAG, "OpenAI请求JSON: " + jsonRequest.toString());
        
        // 构建HTTP请求
        RequestBody requestBody = RequestBody.create(jsonRequest.toString(), JSON);
        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();
        
        // 执行请求
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                Log.e(TAG, "OpenAI API请求失败: " + response.code() + " " + errorBody);
                throw new IOException("OpenAI API调用失败: " + response.code() + " " + response.message());
            }
            
            String responseBody = response.body().string();
            Log.d(TAG, "OpenAI API响应: " + responseBody);
            
            try {
                // 解析响应
                JSONObject jsonResponse = new JSONObject(responseBody);
                
                if (jsonResponse.has("choices") && jsonResponse.getJSONArray("choices").length() > 0) {
                    JSONObject choice = jsonResponse.getJSONArray("choices").getJSONObject(0);
                    if (choice.has("message")) {
                        JSONObject message = choice.getJSONObject("message");
                        if (message.has("content")) {
                            return message.getString("content");
                        }
                    }
                }
                
                // 如果没有找到预期的结构，返回原始响应
                return "无法解析OpenAI API响应: " + responseBody;
            } catch (JSONException e) {
                Log.e(TAG, "解析OpenAI JSON响应失败", e);
                return "解析响应失败: " + e.getMessage() + "\n原始响应: " + responseBody;
            }
        } catch (Exception e) {
            Log.e(TAG, "OpenAI API请求失败", e);
            return "OpenAI API请求失败: " + e.getMessage();
        }
    }

    // 检查API配置
    private void checkApiConfiguration() {
        // 后台线程执行检查
        executor.execute(() -> {
            try {
                // 构建简单的测试请求
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("model", "qwen-vl-max");
                jsonRequest.put("messages", new JSONArray());
                
                Request request = new Request.Builder()
                        .url(DASHSCOPE_API_URL)
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create("{\"model\":\"qwen-vl-max\",\"messages\":[{\"role\":\"user\",\"content\":[{\"type\":\"text\",\"text\":\"hello\"}]}]}", JSON))
                        .build();
                
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "";
                        Log.e(TAG, "API连接测试失败: " + response.code() + " " + errorBody);
                        
                        if (response.code() == 401 || response.code() == 403) {
                            mainHandler.post(() -> {
                                addReceivedMessage("警告：API密钥验证失败。请确保使用有效的API密钥。");
                            });
                        } else if (response.code() == 404) {
                            mainHandler.post(() -> {
                                addReceivedMessage("警告：API地址无法访问。请检查网络连接或API地址是否正确。");
                            });
                        }
                    } else {
                        Log.d(TAG, "API连接测试成功");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "API连接测试异常", e);
                mainHandler.post(() -> {
                    addReceivedMessage("警告：无法连接到API服务。请检查网络连接和API配置。");
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
} 