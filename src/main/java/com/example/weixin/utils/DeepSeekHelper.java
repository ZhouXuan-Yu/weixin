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
    
    // 用户反馈类型
    public static final int FEEDBACK_POSITIVE = 1;
    public static final int FEEDBACK_NEGATIVE = 0;
    
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
    
    /**
     * 生成旅游或户外活动建议
     * @param weatherData 天气数据
     * @param location 地点
     * @param callback 回调函数返回分析结果
     */
    public static void getTravelSuggestions(String weatherData, String location, final OnAnalysisCompleteListener callback) {
        String prompt = "作为旅游专家，我需要你为计划前往" + location + "的游客提供专业建议。" +
                       "根据以下天气数据，分析适合的旅游活动、景点推荐、注意事项和必备物品。" +
                       "考虑天气条件对各类旅游体验的影响，如室内/室外景点选择、交通方式建议等。" +
                       "提供具体、实用的建议，语言简洁，总字数不超过300字。\n\n" + weatherData;
        
        sendApiRequest(prompt, callback);
    }
    
    /**
     * 生成运动健身建议
     * @param weatherData 天气数据
     * @param callback 回调函数返回分析结果
     */
    public static void getSportsSuggestions(String weatherData, final OnAnalysisCompleteListener callback) {
        String prompt = "作为专业运动教练和健康顾问，请根据以下天气数据，为用户提供今日适合的运动建议。" +
                       "分析不同运动方式在当前天气条件下的可行性和注意事项，包括室内和室外运动选择。" +
                       "考虑温度、湿度、风力等因素对运动效果和健康的影响，并给出具体的运动时长、强度和时段建议。" +
                       "提供科学、实用的运动方案，语言简洁，不超过250字。\n\n" + weatherData;
        
        sendApiRequest(prompt, callback);
    }
    
    /**
     * 分析天气对情绪/心理的影响
     * @param weatherData 天气数据
     * @param callback 回调函数返回分析结果
     */
    public static void getMoodSuggestions(String weatherData, final OnAnalysisCompleteListener callback) {
        String prompt = "作为心理学专家，请分析以下天气条件可能对人的情绪和心理状态产生的影响，" +
                       "并提供相应的调节建议。考虑阳光、温度、气压、湿度等因素与情绪之间的科学关联，" +
                       "针对可能出现的季节性情绪变化（如季节性抑郁等），提供实用的心理调适方法、" +
                       "活动建议和生活调整策略。建议应具体、科学且易于实施，总字数不超过250字。\n\n" + weatherData;
        
        sendApiRequest(prompt, callback);
    }
    
    /**
     * 提交用户反馈以改进分析质量
     * @param feedbackType 反馈类型（正面/负面）
     * @param originalPrompt 原始提示
     * @param aiResponse AI响应内容
     * @param userFeedback 用户反馈内容
     * @param callback 回调函数返回处理结果
     */
    public static void submitFeedback(int feedbackType, String originalPrompt, String aiResponse, 
            String userFeedback, final OnFeedbackSubmitListener callback) {
        String feedbackString = feedbackType == FEEDBACK_POSITIVE ? "正面" : "负面";
        
        // 创建请求参数
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", MODEL);
            
            JSONArray messagesArray = new JSONArray();
            
            // 系统消息
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个AI反馈分析师，负责改进天气分析系统的质量。");
            messagesArray.put(systemMessage);
            
            // 用户原始提示
            JSONObject originalPromptMsg = new JSONObject();
            originalPromptMsg.put("role", "user");
            originalPromptMsg.put("content", "原始提示：" + originalPrompt);
            messagesArray.put(originalPromptMsg);
            
            // AI响应
            JSONObject aiResponseMsg = new JSONObject();
            aiResponseMsg.put("role", "assistant");
            aiResponseMsg.put("content", "AI回复：" + aiResponse);
            messagesArray.put(aiResponseMsg);
            
            // 用户反馈
            JSONObject userFeedbackMsg = new JSONObject();
            userFeedbackMsg.put("role", "user");
            userFeedbackMsg.put("content", "用户提供了" + feedbackString + "反馈：" + userFeedback + 
                    "\n请分析这个反馈，给出具体如何改进AI回复质量的建议。");
            messagesArray.put(userFeedbackMsg);
            
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
                notifyFeedbackError(callback, "请求失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        notifyFeedbackError(callback, "API返回错误码: " + response.code());
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
                        
                        notifyFeedbackSuccess(callback);
                    } else {
                        notifyFeedbackError(callback, "API返回内容为空");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析API响应失败", e);
                    notifyFeedbackError(callback, "解析API响应失败: " + e.getMessage());
                }
            }
        });
    }
    
    // 统一API请求方法
    private static void sendApiRequest(String prompt, final OnAnalysisCompleteListener callback) {
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
    
    // 在主线程通知反馈成功
    private static void notifyFeedbackSuccess(final OnFeedbackSubmitListener callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess();
            }
        });
    }
    
    // 在主线程通知反馈失败
    private static void notifyFeedbackError(final OnFeedbackSubmitListener callback, final String errorMsg) {
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
    
    /**
     * 反馈提交回调接口
     */
    public interface OnFeedbackSubmitListener {
        void onSuccess();
        void onError(String errorMessage);
    }
} 