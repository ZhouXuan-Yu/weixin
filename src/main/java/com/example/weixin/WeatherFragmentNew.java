package com.example.weixin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weixin.adapter.WeatherForecastAdapter;
import com.example.weixin.model.CurrentWeather;
import com.example.weixin.model.WeatherData;
import com.example.weixin.utils.AnimationUtils;
import com.example.weixin.utils.DeepSeekHelper;
import com.example.weixin.utils.WeatherChartHelper;
import com.example.weixin.utils.PieChartHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WeatherFragmentNew extends Fragment implements View.OnClickListener {
    
    private static final String API_KEY = "9c3b5002d468426b8d310589ed22223b";
    
    // UI组件
    private EditText searchEditText;
    private TextView tvCurrentLocation, tvCurrentTemp, tvCurrentCondition, tvFeelsLike;
    private TextView tvCurrentHumidity, tvCurrentPressure, tvCurrentVis;
    private TextView tvWeatherSuggestion, tvClothingSuggestion, tvHealthSuggestion, tvAiAnalysisTitle;
    private CardView currentWeatherCard, temperatureChartCard, humidityChartCard;
    private CardView pressureChartCard, weatherDistributionCard, forecastCard;
    private CardView aiSuggestionCard;
    private Button btnGetSuggestions;
    private Button btnGoodFeedback, btnBadFeedback, btnSubmitFeedback;
    private EditText etFeedbackText;
    private LinearLayout feedbackLayout;
    private boolean isPositiveFeedback = true;
    
    // 自定义图表组件
    private WeatherChartHelper temperatureChart, pressureChart, humidityChart;
    private PieChartHelper weatherDistributionChart;
    private RecyclerView forecastRecyclerView;
    private WeatherForecastAdapter forecastAdapter;
    
    // 数据
    private List<WeatherData> weatherDataList = new ArrayList<>();
    private CurrentWeather currentWeather;
    private String currentLocation = "未知位置";
    private String rawWeatherData = "";
    private String rawForecastData = "";
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.weather_main_custom, container, false);
        
        initViews(rootView);
        setupRecyclerView();
        setupEventListeners(rootView);
        
        // 默认加载北京天气
        currentLocation = "北京";
        fetchWeatherData("101010100");
        
        return rootView;
    }
    
    private void initViews(View rootView) {
        // 搜索组件
        searchEditText = rootView.findViewById(R.id.etSearch);
        
        // 当前天气卡片
        currentWeatherCard = rootView.findViewById(R.id.currentWeatherCard);
        tvCurrentLocation = rootView.findViewById(R.id.tvCurrentLocation);
        tvCurrentTemp = rootView.findViewById(R.id.tvCurrentTemp);
        tvCurrentCondition = rootView.findViewById(R.id.tvCurrentCondition);
        tvFeelsLike = rootView.findViewById(R.id.tvFeelsLike);
        tvCurrentHumidity = rootView.findViewById(R.id.tvCurrentHumidity);
        tvCurrentPressure = rootView.findViewById(R.id.tvCurrentPressure);
        tvCurrentVis = rootView.findViewById(R.id.tvCurrentVis);
        
        // 图表卡片
        temperatureChartCard = rootView.findViewById(R.id.temperatureChartCard);
        humidityChartCard = rootView.findViewById(R.id.humidityChartCard);
        pressureChartCard = rootView.findViewById(R.id.pressureChartCard);
        weatherDistributionCard = rootView.findViewById(R.id.weatherDistributionCard);
        forecastCard = rootView.findViewById(R.id.forecastCard);
        
        // AI分析建议卡片
        aiSuggestionCard = rootView.findViewById(R.id.aiSuggestionCard);
        tvWeatherSuggestion = rootView.findViewById(R.id.tvWeatherSuggestion);
        tvClothingSuggestion = rootView.findViewById(R.id.tvClothingSuggestion);
        tvHealthSuggestion = rootView.findViewById(R.id.tvHealthSuggestion);
        tvAiAnalysisTitle = rootView.findViewById(R.id.tvAiAnalysisTitle);
        btnGetSuggestions = rootView.findViewById(R.id.btnGetSuggestions);
        
        // 自定义图表
        temperatureChart = rootView.findViewById(R.id.temperatureChart);
        humidityChart = rootView.findViewById(R.id.humidityChart);
        pressureChart = rootView.findViewById(R.id.pressureChart);
        weatherDistributionChart = rootView.findViewById(R.id.weatherDistributionChart);
        forecastRecyclerView = rootView.findViewById(R.id.forecastRecyclerView);
        
        // 初始化反馈相关视图
        feedbackLayout = rootView.findViewById(R.id.feedback_layout);
        btnGoodFeedback = rootView.findViewById(R.id.btnGoodFeedback);
        btnBadFeedback = rootView.findViewById(R.id.btnBadFeedback);
        btnSubmitFeedback = rootView.findViewById(R.id.btnSubmitFeedback);
        etFeedbackText = rootView.findViewById(R.id.etFeedbackText);
    }
    
    private void setupRecyclerView() {
        forecastAdapter = new WeatherForecastAdapter(weatherDataList);
        forecastRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        forecastRecyclerView.setAdapter(forecastAdapter);
    }
    
    private void setupEventListeners(View rootView) {
        Button btnSearch = rootView.findViewById(R.id.btnSearch);
        Button btnBJ = rootView.findViewById(R.id.btnBJ);
        Button btnSH = rootView.findViewById(R.id.btnSH);
        Button btnTJ = rootView.findViewById(R.id.btnTJ);
        
        btnSearch.setOnClickListener(this);
        btnBJ.setOnClickListener(this);
        btnSH.setOnClickListener(this);
        btnTJ.setOnClickListener(this);
        
        btnGetSuggestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentWeather != null && !weatherDataList.isEmpty()) {
                    getAiWeatherSuggestions();
                } else {
                    Toast.makeText(getContext(), "请先获取天气数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        // 设置反馈按钮点击事件
        btnGoodFeedback.setOnClickListener(v -> {
            isPositiveFeedback = true;
            btnGoodFeedback.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
            btnBadFeedback.setBackgroundTintList(getResources().getColorStateList(R.color.design_default_color_error));
            btnBadFeedback.setAlpha(0.5f);
            btnGoodFeedback.setAlpha(1.0f);
            etFeedbackText.setVisibility(View.GONE);
            btnSubmitFeedback.setVisibility(View.VISIBLE);
        });
        
        btnBadFeedback.setOnClickListener(v -> {
            isPositiveFeedback = false;
            btnBadFeedback.setBackgroundTintList(getResources().getColorStateList(R.color.design_default_color_error));
            btnGoodFeedback.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
            btnGoodFeedback.setAlpha(0.5f);
            btnBadFeedback.setAlpha(1.0f);
            etFeedbackText.setVisibility(View.VISIBLE);
            btnSubmitFeedback.setVisibility(View.VISIBLE);
        });
        
        btnSubmitFeedback.setOnClickListener(v -> {
            submitFeedback();
        });
    }
    
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnSearch) {
            String input = searchEditText.getText().toString().trim();
            if (!input.isEmpty()) {
                searchCity(input);
            } else {
                Toast.makeText(getContext(), "请输入城市名称", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.btnBJ) {
            currentLocation = "北京";
            fetchWeatherData("101010100");
        } else if (id == R.id.btnSH) {
            currentLocation = "上海";
            fetchWeatherData("101020100");
        } else if (id == R.id.btnTJ) {
            currentLocation = "天津";
            fetchWeatherData("101030100");
        }
    }
    
    private void searchCity(String cityName) {
        // 城市ID搜索功能实现
        // 此处省略具体实现，与原WeatherFragment类似
        Toast.makeText(getContext(), "搜索城市: " + cityName, Toast.LENGTH_SHORT).show();
    }
    
    private void fetchWeatherData(String locationId) {
        fetchCurrentWeather(locationId);
        fetchForecastWeather(locationId);
    }
    
    private void fetchCurrentWeather(String locationId) {
        // 获取当前天气数据的网络请求
        // 此处省略具体实现，与原WeatherFragment类似
        
        // 模拟数据用于测试
        simulateWeatherData();
    }
    
    private void fetchForecastWeather(String locationId) {
        // 获取预报天气数据的网络请求
        // 此处省略具体实现，与原WeatherFragment类似
        
        // 模拟数据用于测试
        simulateForecastData();
    }
    
    // 模拟天气数据用于测试
    private void simulateWeatherData() {
        currentWeather = new CurrentWeather();
        currentWeather.setLocation(currentLocation);
        currentWeather.setTemperature(23);
        currentWeather.setWeatherText("晴");
        currentWeather.setFeelsLike(25);
        currentWeather.setHumidity(45);
        currentWeather.setPressure(1013);
        currentWeather.setVisibility(10);
        
        updateCurrentWeatherUI();
    }
    
    // 模拟预报数据用于测试
    private void simulateForecastData() {
        weatherDataList.clear();
        
        for (int i = 0; i < 3; i++) {
            WeatherData data = new WeatherData();
            data.setDate("2023-06-" + (20 + i));
            data.setMinTemp(18 + i);
            data.setMaxTemp(28 + i);
            data.setWeatherText(i == 1 ? "多云" : "晴");
            data.setHumidity(45 + i * 5);
            data.setPressure(1013 - i);
            weatherDataList.add(data);
        }
        
        updateForecastUI();
        updateCharts();
    }
    
    private void updateCurrentWeatherUI() {
        if (currentWeather == null) return;
        
        tvCurrentLocation.setText(currentWeather.getLocation());
        tvCurrentTemp.setText(currentWeather.getTemperature() + "°");
        tvCurrentCondition.setText(currentWeather.getWeatherText());
        tvFeelsLike.setText("体感温度: " + currentWeather.getFeelsLike() + "°");
        tvCurrentHumidity.setText(currentWeather.getHumidity() + "%");
        tvCurrentPressure.setText(currentWeather.getPressure() + "hPa");
        tvCurrentVis.setText(currentWeather.getVisibility() + "km");
        
        currentWeatherCard.setVisibility(View.VISIBLE);
    }
    
    private void updateForecastUI() {
        forecastAdapter.notifyDataSetChanged();
        forecastCard.setVisibility(View.VISIBLE);
    }
    
    private void updateCharts() {
        setupTemperatureChart();
        setupHumidityChart();
        setupPressureChart();
        setupWeatherDistributionChart();
        
        showAllCards();
    }
    
    private void setupTemperatureChart() {
        if (temperatureChart == null || weatherDataList.isEmpty()) return;
        
        List<Float> temps = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        
        for (WeatherData data : weatherDataList) {
            temps.add((float) data.getMaxTemp());
            dates.add(formatShortDate(data.getDate()));
        }
        
        temperatureChart.setData(temps, dates);
        temperatureChart.setTitle("温度趋势");
        temperatureChart.setYAxisLabel("温度(°C)");
        temperatureChart.setLineColor(Color.RED);
        
        temperatureChartCard.setVisibility(View.VISIBLE);
    }
    
    private void setupHumidityChart() {
        if (humidityChart == null || weatherDataList.isEmpty()) return;
        
        List<Float> humidity = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        
        for (WeatherData data : weatherDataList) {
            humidity.add((float) data.getHumidity());
            dates.add(formatShortDate(data.getDate()));
        }
        
        humidityChart.setData(humidity, dates);
        humidityChart.setTitle("湿度趋势");
        humidityChart.setYAxisLabel("湿度(%)");
        humidityChart.setLineColor(Color.BLUE);
        humidityChart.setChartType(1); // 1表示柱状图
        humidityChart.setBarColor(Color.rgb(0, 128, 255));
        
        humidityChartCard.setVisibility(View.VISIBLE);
    }
    
    private void setupPressureChart() {
        if (pressureChart == null || weatherDataList.isEmpty()) return;
        
        List<Float> pressure = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        
        for (WeatherData data : weatherDataList) {
            pressure.add((float) data.getPressure());
            dates.add(formatShortDate(data.getDate()));
        }
        
        pressureChart.setData(pressure, dates);
        pressureChart.setTitle("气压趋势");
        pressureChart.setYAxisLabel("气压(hPa)");
        pressureChart.setLineColor(Color.GREEN);
        
        pressureChartCard.setVisibility(View.VISIBLE);
    }
    
    private void setupWeatherDistributionChart() {
        if (weatherDistributionChart == null || weatherDataList.isEmpty()) return;
        
        // 统计不同天气类型的出现次数
        Map<String, Integer> weatherCounts = new HashMap<>();
        for (WeatherData data : weatherDataList) {
            String weatherText = data.getWeatherText();
            weatherCounts.put(weatherText, weatherCounts.getOrDefault(weatherText, 0) + 1);
        }
        
        // 准备饼图数据
        List<PieChartHelper.PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : weatherCounts.entrySet()) {
            entries.add(new PieChartHelper.PieEntry(entry.getValue(), entry.getKey()));
        }
        
        weatherDistributionChart.setEntries(entries);
        weatherDistributionChart.setTitle("天气状况分布");
        weatherDistributionChart.setCenterText("天气分布");
        
        weatherDistributionCard.setVisibility(View.VISIBLE);
    }
    
    private void showAllCards() {
        currentWeatherCard.setVisibility(View.VISIBLE);
        temperatureChartCard.setVisibility(View.VISIBLE);
        humidityChartCard.setVisibility(View.VISIBLE);
        pressureChartCard.setVisibility(View.VISIBLE);
        weatherDistributionCard.setVisibility(View.VISIBLE);
        forecastCard.setVisibility(View.VISIBLE);
        aiSuggestionCard.setVisibility(View.VISIBLE);
    }
    
    private String formatShortDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";
        
        // 简单的日期格式化，例如 "2023-06-20" -> "6/20"
        String[] parts = dateStr.split("-");
        if (parts.length >= 3) {
            return parts[1].replaceFirst("^0+", "") + "/" + parts[2].replaceFirst("^0+", "");
        }
        return dateStr;
    }
    
    private void submitFeedback() {
        String feedbackText = isPositiveFeedback ? "正面反馈" : etFeedbackText.getText().toString();
        // 处理用户反馈
        Toast.makeText(getContext(), "感谢您的反馈！", Toast.LENGTH_SHORT).show();
        feedbackLayout.setVisibility(View.GONE);
    }
    
    private void getAiWeatherSuggestions() {
        // 显示加载状态
        Toast.makeText(getContext(), "正在获取AI智能建议...", Toast.LENGTH_SHORT).show();
        
        // 模拟AI生成的建议
        tvWeatherSuggestion.setText("今天天气晴朗，非常适合户外活动。建议您可以安排户外散步、跑步或者其他户外休闲活动。");
        tvClothingSuggestion.setText("天气温暖，建议穿轻薄的衣物，如短袖T恤、轻便裤子或裙子。外出时可带一件薄外套，以防温度变化。");
        tvHealthSuggestion.setText("天气良好，但紫外线较强，外出时请做好防晒措施，涂抹防晒霜，戴太阳镜和帽子。多喝水保持身体水分。");
        
        // 显示反馈组件
        feedbackLayout.setVisibility(View.VISIBLE);
    }
}
