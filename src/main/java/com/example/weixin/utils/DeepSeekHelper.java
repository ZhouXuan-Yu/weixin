package com.example.weixin.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * DeepSeek API助手类
 * 用于调用DeepSeek API进行智能分析
 */
public class DeepSeekHelper {
    private static final String TAG = "DeepSeekHelper";
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String API_KEY = "sk-f628cf420c5e48a5b91d9a1de3140c10";
    private static final String MODEL = "deepseek-chat";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final int TIMEOUT_SECONDS = 30;
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();
    
    /**
     * 分析天气数据并提供建议
     * @param weatherData 天气数据
     * @param callback 回调函数返回分析结果
     */
    public static void analyzeWeatherData(String weatherData, final OnAnalysisCompleteListener callback) {
        String prompt = "请根据以下天气数据，分析今天的天气情况，并给出穿衣建议、出行建议等生活提示。使用简洁明了的语言回复，不超过200字。\n\n" + weatherData;
        
        // 创建请求参数
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", MODEL);
            
            JSONArray messagesArray = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messagesArray.put(userMessage);
            
            jsonBody.put("messages", messagesArray);
            jsonBody.put("temperature", 0.7);
            jsonBody.put("max_tokens", 500);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON request", e);
            callback.onError("创建请求失败: " + e.getMessage());
            return;
        }
        
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();
        
        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "DeepSeek API请求失败", e);
                notifyError(callback, "请求失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        notifyError(callback, "API返回错误码: " + response.code());
                        return;
                    }
                    
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    
                    // 解析返回的内容
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices.length() > 0) {
                        JSONObject choice = choices.getJSONObject(0);
                        JSONObject message = choice.getJSONObject("message");
                        String content = message.getString("content");
                        
                        notifySuccess(callback, content);
                    } else {
                        notifyError(callback, "API返回内容为空");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析API响应失败", e);
                    notifyError(callback, "解析API响应失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 根据天气情况分析未来几天的穿衣建议
     * @param weatherForecast 天气预报数据
     * @param callback 回调函数返回分析结果
     */
    public static void getClothingSuggestions(String weatherForecast, final OnAnalysisCompleteListener callback) {
        String prompt = "请根据以下未来几天的天气预报数据，分析天气变化趋势，并给出衣着搭配建议。使用简洁明了的语言回复，不超过250字。\n\n" + weatherForecast;
        
        // 创建请求参数
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", MODEL);
            
            JSONArray messagesArray = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messagesArray.put(userMessage);
            
            jsonBody.put("messages", messagesArray);
            jsonBody.put("temperature", 0.7);
            jsonBody.put("max_tokens", 800);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON request", e);
            callback.onError("创建请求失败: " + e.getMessage());
            return;
        }
        
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();
        
        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "DeepSeek API请求失败", e);
                notifyError(callback, "请求失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        notifyError(callback, "API返回错误码: " + response.code());
                        return;
                    }
                    
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    
                    // 解析返回的内容
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices.length() > 0) {
                        JSONObject choice = choices.getJSONObject(0);
                        JSONObject message = choice.getJSONObject("message");
                        String content = message.getString("content");
                        
                        notifySuccess(callback, content);
                    } else {
                        notifyError(callback, "API返回内容为空");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析API响应失败", e);
                    notifyError(callback, "解析API响应失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 根据空气质量数据和天气状况给出健康建议
     * @param airQualityData 空气质量数据
     * @param callback 回调函数返回分析结果
     */
    public static void getHealthSuggestions(String airQualityData, final OnAnalysisCompleteListener callback) {
        String prompt = "请根据以下空气质量和天气数据，分析对健康的影响，并给出健康建议，特别是对敏感人群的建议。使用简洁明了的语言回复，不超过200字。\n\n" + airQualityData;
        
        // 创建请求参数
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", MODEL);
            
            JSONArray messagesArray = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messagesArray.put(userMessage);
            
            jsonBody.put("messages", messagesArray);
            jsonBody.put("temperature", 0.7);
            jsonBody.put("max_tokens", 500);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON request", e);
            callback.onError("创建请求失败: " + e.getMessage());
            return;
        }
        
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();
        
        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "DeepSeek API请求失败", e);
                notifyError(callback, "请求失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        notifyError(callback, "API返回错误码: " + response.code());
                        return;
                    }
                    
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    
                    // 解析返回的内容
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices.length() > 0) {
                        JSONObject choice = choices.getJSONObject(0);
                        JSONObject message = choice.getJSONObject("message");
                        String content = message.getString("content");
                        
                        notifySuccess(callback, content);
                    } else {
                        notifyError(callback, "API返回内容为空");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析API响应失败", e);
                    notifyError(callback, "解析API响应失败: " + e.getMessage());
                }
            }
        });
    }
    
    // 在主线程通知回调成功
    private static void notifySuccess(final OnAnalysisCompleteListener callback, final String result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(result);
            }
        });
    }
    
    // 在主线程通知回调失败
    private static void notifyError(final OnAnalysisCompleteListener callback, final String errorMsg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                callback.onError(errorMsg);
            }
        });
    }
    
    /**
     * 分析结果回调接口
     */
    public interface OnAnalysisCompleteListener {
        void onSuccess(String result);
        void onError(String errorMessage);
    }
} 