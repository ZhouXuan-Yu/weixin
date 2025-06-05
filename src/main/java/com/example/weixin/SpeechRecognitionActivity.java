package com.example.weixin;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.Iterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class SpeechRecognitionActivity extends AppCompatActivity {

    private static final String TAG = "SpeechRecognitionActivity";
    private static final String API_KEY = "sk-8ecbfb7922bc425bafb971616f5a7674";
    // 阿里云通义千问音频模型API端点URL
    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType MEDIA_TYPE_MP3 = MediaType.parse("audio/mpeg");
    
    private ImageView btnBack;
    private Button btnUploadFile;
    private RecyclerView chatRecyclerView;
    private ProgressBar progressBar;
    private TextView statusText;
    
    private final List<ChatMessage> messageList = new ArrayList<>();
    private ChatMessageAdapter adapter;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ExecutorService apiExecutor;
    
    // OkHttp客户端
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    
    // 文件选择器启动器
    private final ActivityResultLauncher<Intent> filePickerLauncher = 
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), 
                    result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedFileUri = result.getData().getData();
                    handleSelectedFile(selectedFileUri);
                }
            });
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_recognition);
        
        // 初始化线程池
        apiExecutor = new ThreadPoolExecutor(
                1, 2, 60L, TimeUnit.SECONDS, 
                new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        initViews();
        setupRecyclerView();
        setupListeners();
        
        // 添加欢迎消息
        addReceivedMessage("你好！我是郑工智能体的语音识别助手，点击下方按钮上传音频文件，我将识别并翻译您的音频内容。");
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnUploadFile = findViewById(R.id.btn_start_stop);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        statusText = findViewById(R.id.status_text);
        
        // 修改按钮文字
        btnUploadFile.setText("上传音频文件");
    }
    
    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter(this, messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnUploadFile.setOnClickListener(v -> openFilePicker());
    }
    
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");  // 只选择音频文件
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(intent);
    }
    
    private void handleSelectedFile(Uri fileUri) {
        try {
            // 获取文件路径
            String filePath = getPathFromUri(fileUri);
            Log.d(TAG, "选择的音频文件URI: " + fileUri + ", 解析的路径: " + filePath);
            
            if (filePath == null) {
                Toast.makeText(this, "无法获取文件路径", Toast.LENGTH_SHORT).show();
                return;
            }
            
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                // 尝试直接从URI创建输入流
                try {
                    // 创建临时文件
                    File tempDir = new File(getExternalFilesDir(null), "temp");
                    if (!tempDir.exists()) {
                        tempDir.mkdirs();
                    }
                    
                    String fileName = "audio_" + System.currentTimeMillis() + ".tmp";
                    File tempFile = new File(tempDir, fileName);
                    
                    // 从URI复制到临时文件
                    try (java.io.InputStream inputStream = getContentResolver().openInputStream(fileUri);
                         java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile)) {
                        
                        if (inputStream == null) {
                            Toast.makeText(this, "无法读取所选文件", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.flush();
                    }
                    
                    // 使用临时文件替代原始文件
                    audioFile = tempFile;
                    filePath = tempFile.getAbsolutePath();
                    Log.d(TAG, "已创建临时文件: " + filePath);
                    
                } catch (Exception e) {
                    Log.e(TAG, "创建临时文件失败", e);
                    Toast.makeText(this, "处理文件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // 获取文件名
            String fileName = audioFile.getName();
            
            // 添加用户消息
            addSentMessage("已上传音频文件: " + fileName);
            
            // 显示进度条和状态
            progressBar.setVisibility(View.VISIBLE);
            statusText.setText("正在处理音频...");
            
            // 保存文件引用为final变量，以便在lambda表达式中使用
            final File finalAudioFile = audioFile;
            Log.d(TAG, "准备处理音频文件: " + finalAudioFile.getAbsolutePath() + ", 大小: " + finalAudioFile.length() + " 字节");
            
            // 在后台线程处理文件
            apiExecutor.execute(() -> {
                try {
                    // 处理文件
                    Log.d(TAG, "开始处理音频文件: " + finalAudioFile.getName());
                    String result = processAudioFile(finalAudioFile);
                    Log.d(TAG, "音频处理完成，结果长度: " + (result != null ? result.length() : 0) + " 字符");
                    
                    mainHandler.post(() -> {
                        addReceivedMessage(result);
                        progressBar.setVisibility(View.GONE);
                        statusText.setText("处理完成");
                    });
                } catch (Exception e) {
                    Log.e(TAG, "处理音频文件失败", e);
                    e.printStackTrace(); // 打印完整堆栈信息
                    mainHandler.post(() -> {
                        addReceivedMessage("处理音频文件失败: " + e.getMessage());
                        progressBar.setVisibility(View.GONE);
                        statusText.setText("处理失败");
                    });
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "处理选择的文件失败", e);
            Toast.makeText(this, "处理文件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    // 从Uri获取文件路径
    private String getPathFromUri(Uri uri) {
        String filePath = null;
        try {
            String scheme = uri.getScheme();
            if (scheme != null) {
                if (scheme.equals("content")) {
                    String[] projection = {MediaStore.Audio.Media.DATA};
                    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                    if (cursor != null) {
                        try {
                            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                            cursor.moveToFirst();
                            filePath = cursor.getString(columnIndex);
                        } catch (Exception e) {
                            Log.e(TAG, "获取文件路径失败", e);
                        } finally {
                            cursor.close();
                        }
                    }
                    
                    // 如果通过MediaStore无法获取路径，尝试其他方式
                    if (filePath == null) {
                        filePath = getFilePathFromDocumentUri(uri);
                    }
                } else if (scheme.equals("file")) {
                    filePath = uri.getPath();
                }
            }
            
            // 如果仍未获取到路径，尝试使用URI的路径
            if (filePath == null) {
                filePath = uri.getPath();
            }
            
            Log.d(TAG, "获取到的文件路径: " + filePath);
        } catch (Exception e) {
            Log.e(TAG, "获取文件路径发生异常", e);
            filePath = uri.getPath();
        }
        
        return filePath;
    }
    
    // 从Document URI获取文件路径
    private String getFilePathFromDocumentUri(Uri uri) {
        try {
            String docId = uri.getLastPathSegment();
            if (docId.contains(":")) {
                String[] split = docId.split(":");
                String type = split[0];
                String id = split[1];
                
                // 处理外部存储
                if ("primary".equalsIgnoreCase(type)) {
                    return getExternalFilesDir(null).getPath() + "/" + id;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "从Document URI获取文件路径失败", e);
        }
        return null;
    }
    
    // 音频处理方法，文件作为final参数传入，确保在lambda中不会被修改
    private String processAudioFile(final File audioFile) throws Exception {
        // 获取文件格式
        String fileExtension = getFileExtension(audioFile.getName());
        String format = getAudioFormat(fileExtension);
        
        Log.d(TAG, "开始处理音频文件: " + audioFile.getName() 
               + ", 路径: " + audioFile.getAbsolutePath()
               + ", 格式: " + format 
               + ", 大小: " + audioFile.length() + " 字节"
               + ", 文件存在: " + audioFile.exists());
        
        // 检查文件读取权限
        if (!audioFile.canRead()) {
            Log.e(TAG, "无法读取音频文件，没有读取权限");
            throw new IOException("无法读取音频文件: 缺少读取权限");
        }
        
        // 检查文件内容
        try (FileInputStream fis = new FileInputStream(audioFile)) {
            byte[] header = new byte[Math.min(1024, (int)audioFile.length())];
            int read = fis.read(header);
            Log.d(TAG, "成功读取文件头 " + read + " 字节");
            // 将文件头的十六进制值打印出来，以便诊断文件格式
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(50, read); i++) {
                sb.append(String.format("%02X ", header[i]));
            }
            Log.d(TAG, "文件头部分内容(HEX): " + sb.toString());
        } catch (Exception e) {
            Log.e(TAG, "读取文件头失败", e);
        }
        
        // 处理音频文件并转换为Base64
        String fileBase64;
        try {
            byte[] fileBytes = new byte[(int) audioFile.length()];
            try (FileInputStream fileInputStream = new FileInputStream(audioFile)) {
                int bytesRead = fileInputStream.read(fileBytes);
                Log.d(TAG, "文件读取完成，读取了 " + bytesRead + " 字节");
                
                // 更新上传进度
                mainHandler.post(() -> {
                    progressBar.setProgress(10);
                    statusText.setText("正在编码音频数据...");
                });
            }
            
            fileBase64 = android.util.Base64.encodeToString(fileBytes, android.util.Base64.NO_WRAP);
            Log.d(TAG, "音频文件已转换为Base64, 长度: " + fileBase64.length());
            
            // 更新上传进度
            mainHandler.post(() -> {
                progressBar.setProgress(30);
                statusText.setText("准备上传音频数据...");
            });
        } catch (Exception e) {
            Log.e(TAG, "转换文件为Base64失败", e);
            throw new IOException("转换文件为Base64失败: " + e.getMessage());
        }
        
        // 使用阿里云通义千问多模态API构建JSON请求
        JSONObject requestJson = new JSONObject();
        try {
            // 设置模型
            requestJson.put("model", "qwen-audio-turbo");
            
            // 设置Input对象，确保请求格式正确
            JSONObject inputObject = new JSONObject();
            
            // 构建消息部分
            JSONArray messagesArray = new JSONArray();
            JSONObject userMessage = new JSONObject();
            
            // 确保"role"字段值为"user"
            userMessage.put("role", "user");
            
            // 构建content部分 - 根据参考代码调整格式
            JSONArray contentArray = new JSONArray();
            
            // 添加音频内容，格式根据参考代码为 { "audio": "音频地址/数据" }
            JSONObject audioItem = new JSONObject();
            // 直接使用字符串作为值，而不是嵌套对象
            audioItem.put("audio", "data:audio/mp3;base64," + fileBase64);
            contentArray.put(audioItem);
            
            // 添加文本提问（第二个元素是包含文本的Map）
            JSONObject textItem = new JSONObject();
            textItem.put("text", "音频里在说什么?");
            contentArray.put(textItem);
            
            // 将content数组添加到message中
            userMessage.put("content", contentArray);
            
            // 将message添加到messages数组
            messagesArray.put(userMessage);
            
            // 将messages添加到input对象
            inputObject.put("messages", messagesArray);
            
            // 将input对象添加到根对象
            requestJson.put("input", inputObject);
            
            // 添加参数设置
            JSONObject parameters = new JSONObject();
            requestJson.put("parameters", parameters);
            
            // 输出完整的JSON请求以便调试
            String requestJsonString = requestJson.toString();
            
            // 打印请求结构的关键部分，以便调试
            Log.d(TAG, "完整的请求JSON: " + requestJsonString);
            Log.d(TAG, "messages[0].role: " + userMessage.getString("role"));
            
            // 打印content数组结构
            JSONArray content = userMessage.getJSONArray("content");
            for (int i = 0; i < content.length(); i++) {
                Object item = content.get(i);
                if (item instanceof JSONObject) {
                    JSONObject jsonItem = (JSONObject) item;
                    Iterator<String> keys = jsonItem.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        Log.d(TAG, "messages[0].content[" + i + "]." + key + " 类型: " + 
                              (jsonItem.get(key) instanceof String ? "String" : 
                              (jsonItem.get(key) instanceof JSONObject ? "JSONObject" : "其他类型")));
                    }
                } else {
                    Log.d(TAG, "messages[0].content[" + i + "] 类型: " + item.getClass().getSimpleName());
                }
            }
            
            Log.d(TAG, "已构建JSON请求，使用模型: qwen-audio-turbo");
        } catch (JSONException e) {
            Log.e(TAG, "构建JSON请求失败", e);
            throw new IOException("构建JSON请求失败: " + e.getMessage());
        }
        
        // 创建JSON格式的请求体
        RequestBody requestBody = RequestBody.create(requestJson.toString(), JSON);
        
        // 构建HTTP请求
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();
                
        Log.d(TAG, "请求URL: " + API_URL);
        Log.d(TAG, "授权头: Bearer " + API_KEY.substring(0, 5) + "...");
        
        Log.d(TAG, "准备发送HTTP请求到: " + API_URL);
        
        // 显示上传进度
        mainHandler.post(() -> statusText.setText("正在上传音频文件..."));
        
        // 执行请求
        long startTime = System.currentTimeMillis();
        try (Response response = client.newCall(request).execute()) {
            long endTime = System.currentTimeMillis();
            Log.d(TAG, "HTTP请求完成，耗时: " + (endTime - startTime) + "ms");
            
            String responseBody = response.body() != null ? response.body().string() : "No response body";
            Log.d(TAG, "HTTP状态码: " + response.code());
            
            if (!response.isSuccessful()) {
                Log.e(TAG, "API请求失败: " + response.code() + " " + responseBody);
                
                // 输出请求信息以便调试
                if (requestBody instanceof RequestBody) {
                    try {
                        Buffer buffer = new Buffer();
                        requestBody.writeTo(buffer);
                        String requestBodyContent = buffer.readUtf8();
                        Log.e(TAG, "请求体内容: " + requestBodyContent);
                    } catch (Exception e) {
                        Log.e(TAG, "无法记录请求体", e);
                    }
                }
                
                // 尝试从错误响应中获取更多信息
                String errorMessage = extractErrorMessage(responseBody);
                throw new IOException("API调用失败 (" + response.code() + "): " + errorMessage);
            }
            
            // 限制日志长度
            String logResponseBody = responseBody;
            if (logResponseBody.length() > 1000) {
                logResponseBody = logResponseBody.substring(0, 1000) + "... (截断)";
            }
            Log.d(TAG, "API响应: " + logResponseBody);
            
            // 解析响应
            String result = parseResponse(responseBody);
            Log.d(TAG, "响应解析完成，结果长度: " + (result != null ? result.length() : 0));
            return result;
        } catch (Exception e) {
            Log.e(TAG, "HTTP请求失败", e);
            throw e;
        }
    }
    
    // 从错误响应中提取错误信息
    private String extractErrorMessage(String errorResponse) {
        try {
            JSONObject errorJson = new JSONObject(errorResponse);
            if (errorJson.has("message")) {
                return errorJson.getString("message");
            } else if (errorJson.has("error") && errorJson.getJSONObject("error").has("message")) {
                return errorJson.getJSONObject("error").getString("message");
            } else {
                return errorResponse;
            }
        } catch (JSONException e) {
            return errorResponse;
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    private String getAudioFormat(String fileExtension) {
        switch (fileExtension) {
            case "mp3":
                return "mp3";
            case "wav":
                return "wav";
            case "aac":
                return "aac";
            case "amr":
                return "amr";
            case "ogg":
                // 可能是opus或speex，这里假设是opus
                return "ogg_opus";
            case "pcm":
                return "pcm";
            default:
                // 默认返回mp3
                return "mp3";
        }
    }
    
    private String parseResponse(String responseJson) {
        try {
            Log.d(TAG, "开始解析响应: " + (responseJson.length() > 100 ? responseJson.substring(0, 100) + "..." : responseJson));
            JSONObject jsonResponse = new JSONObject(responseJson);
            
            StringBuilder resultBuilder = new StringBuilder();
            
            // 获取请求ID (如果有)
            if (jsonResponse.has("request_id")) {
                String requestId = jsonResponse.getString("request_id");
                Log.d(TAG, "请求ID: " + requestId);
            }
            
            // 记录完整的JSON结构，帮助调试
            Log.d(TAG, "JSON响应结构: " + jsonResponseToDebugString(jsonResponse));
            
            // 解析通义千问音频模型的响应格式
            if (jsonResponse.has("output")) {
                JSONObject output = jsonResponse.getJSONObject("output");
                Log.d(TAG, "找到output字段");
                
                // 通义千问格式的响应处理
                if (output.has("choices") && output.getJSONArray("choices").length() > 0) {
                    JSONArray choices = output.getJSONArray("choices");
                    JSONObject firstChoice = choices.getJSONObject(0);
                    
                    if (firstChoice.has("message")) {
                        JSONObject message = firstChoice.getJSONObject("message");
                        
                        if (message.has("content")) {
                            JSONArray content = message.getJSONArray("content");
                            
                            for (int i = 0; i < content.length(); i++) {
                                JSONObject item = content.getJSONObject(i);
                                if (item.has("text")) {
                                    String text = item.getString("text");
                                    Log.d(TAG, "识别文本结果: " + text);
                                    resultBuilder.append(text);
                                }
                            }
                        }
                    }
                    
                    // 检查完成原因
                    if (firstChoice.has("finish_reason")) {
                        String finishReason = firstChoice.getString("finish_reason");
                        Log.d(TAG, "完成原因: " + finishReason);
                    }
                }
                
                // 检查使用情况信息
                if (jsonResponse.has("usage")) {
                    JSONObject usage = jsonResponse.getJSONObject("usage");
                    Log.d(TAG, "使用情况: " + usage.toString());
                    
                    if (usage.has("input_tokens") && usage.has("output_tokens")) {
                        int inputTokens = usage.getInt("input_tokens");
                        int outputTokens = usage.getInt("output_tokens");
                        resultBuilder.append("\n\n(输入令牌: ").append(inputTokens)
                                .append(", 输出令牌: ").append(outputTokens).append(")");
                    }
                }
            } else {
                Log.d(TAG, "响应中没有output字段");
                
                // 尝试检索其他可能的结果格式
                String anyText = findAnyTextField(jsonResponse);
                if (anyText != null) {
                    resultBuilder.append("识别结果: ").append(anyText);
                    Log.d(TAG, "从其他字段找到文本: " + anyText);
                }
            }
            
            String result = resultBuilder.toString();
            if (result.isEmpty()) {
                // 检查是否有其他格式的结果
                if (jsonResponse.has("output") && jsonResponse.getJSONObject("output").has("text")) {
                    String text = jsonResponse.getJSONObject("output").getString("text");
                    Log.d(TAG, "从output.text中找到结果: " + text);
                    return "识别结果: " + text;
                }
                
                // 尝试查找任何可能包含文本的字段
                String anyText = findAnyTextField(jsonResponse);
                if (anyText != null) {
                    Log.d(TAG, "从其他字段找到文本: " + anyText);
                    return "识别结果: " + anyText;
                }
                
                Log.w(TAG, "无法找到任何可用的结果文本");
                return "无法识别音频内容，API返回数据格式不符合预期";
            }
            
            Log.d(TAG, "成功提取结果: " + result);
            return result;
            
        } catch (JSONException e) {
            Log.e(TAG, "解析响应失败", e);
            e.printStackTrace();
            return "解析响应失败: " + e.getMessage() + "\n原始响应: " + 
                  (responseJson.length() > 200 ? responseJson.substring(0, 200) + "..." : responseJson);
        }
    }
    
    // 递归搜索JSON结构中的任何文本字段
    private String findAnyTextField(JSONObject json) {
        try {
            // 遍历所有键
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                // 如果键名包含"text"或"result"字样，可能是文本结果
                if ((key.toLowerCase().contains("text") || key.toLowerCase().contains("result")) 
                    && json.get(key) instanceof String) {
                    return json.getString(key);
                }
                
                // 如果值是JSON对象，递归搜索
                if (json.get(key) instanceof JSONObject) {
                    String result = findAnyTextField(json.getJSONObject(key));
                    if (result != null) return result;
                }
                
                // 如果值是JSON数组，遍历数组元素
                if (json.get(key) instanceof JSONArray) {
                    JSONArray array = json.getJSONArray(key);
                    for (int i = 0; i < array.length(); i++) {
                        if (array.get(i) instanceof JSONObject) {
                            String result = findAnyTextField(array.getJSONObject(i));
                            if (result != null) return result;
                        } else if (array.get(i) instanceof String && 
                                  (key.toLowerCase().contains("text") || key.toLowerCase().contains("result"))) {
                            return array.getString(i);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "搜索文本字段失败", e);
        }
        return null;
    }
    
    // 生成JSON结构的调试字符串
    private String jsonResponseToDebugString(JSONObject json) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            
            Iterator<String> keys = json.keys();
            boolean hasElements = false;
            
            while (keys.hasNext()) {
                hasElements = true;
                String key = keys.next();
                sb.append("  \"").append(key).append("\": ");
                Object value = json.get(key);
                if (value instanceof JSONObject) {
                    sb.append("[JSONObject]");
                } else if (value instanceof JSONArray) {
                    sb.append("[JSONArray with ").append(((JSONArray) value).length()).append(" items]");
                } else if (value instanceof String) {
                    String strValue = json.getString(key);
                    if (strValue.length() > 50) {
                        strValue = strValue.substring(0, 50) + "...";
                    }
                    sb.append("\"").append(strValue).append("\"");
                } else {
                    sb.append(value);
                }
                
                if (keys.hasNext()) {
                    sb.append(",\n");
                } else {
                    sb.append("\n");
                }
            }
            
            if (hasElements) {
                // 不需要删除最后的逗号，因为我们已经在循环中处理好了
            }
            
            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "生成调试字符串失败", e);
            return "[无法生成调试字符串]";
        }
    }
    
    private void addSentMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(ChatMessage.TYPE_SENT, message);
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
    
    @Override
    protected void onDestroy() {
        if (apiExecutor != null) {
            apiExecutor.shutdown();
        }
        
        super.onDestroy();
    }
    
    // 用于跟踪上传进度的RequestBody
    private static class ProgressRequestBody extends RequestBody {
        interface ProgressCallback {
            void onProgressUpdate(long bytesWritten, long contentLength);
        }
        
        private final File file;
        private final MediaType contentType;
        private final ProgressCallback callback;
        
        ProgressRequestBody(File file, MediaType contentType, ProgressCallback callback) {
            this.file = file;
            this.contentType = contentType;
            this.callback = callback;
        }
        
        @Override
        public MediaType contentType() {
            return contentType;
        }
        
        @Override
        public long contentLength() {
            return file.length();
        }
        
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            Source source = null;
            try {
                source = Okio.source(file);
                long total = 0;
                long read;
                
                while ((read = source.read(sink.buffer(), 8192)) != -1) {
                    total += read;
                    sink.flush();
                    callback.onProgressUpdate(total, contentLength());
                }
            } finally {
                if (source != null) {
                    source.close();
                }
            }
        }
    }
} 