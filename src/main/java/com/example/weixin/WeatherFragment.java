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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

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

public class WeatherFragment extends Fragment implements View.OnClickListener {
    
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
    
    // 图表组件
    private LineChart temperatureChart, pressureChart;
    private BarChart humidityChart;
    private PieChart weatherDistributionChart;
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
        View rootView = inflater.inflate(R.layout.weather_main_new, container, false);
        
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
        
        // 图表
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
        if (view.getId() == R.id.btnSearch) {
            String input = searchEditText.getText().toString().trim();
            if (!input.isEmpty()) {
                searchCity(input);
            } else {
                Toast.makeText(getContext(), "请输入城市名称", Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.btnBJ) {
            currentLocation = "北京";
            fetchWeatherData("101010100");
        } else if (view.getId() == R.id.btnSH) {
            currentLocation = "上海";
            fetchWeatherData("101020100");
        } else if (view.getId() == R.id.btnTJ) {
            currentLocation = "天津";
            fetchWeatherData("101030100");
        }
    }
    
    private void searchCity(String cityName) {
        String apiUrl = "https://geoapi.qweather.com/v2/city/lookup?location=" + cityName + "&key=" + API_KEY;
        
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(apiUrl).build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "搜索失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    String cityId = parseCityId(responseBody);
                    if (cityId != null) {
                        getActivity().runOnUiThread(() -> {
                            currentLocation = cityName;
                            fetchWeatherData(cityId);
                        });
                    } else {
                        getActivity().runOnUiThread(() -> 
                            Toast.makeText(getContext(), "未找到该城市", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    getActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "搜索失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    
    private String parseCityId(String cityData) {
        try {
            JSONObject jsonObject = new JSONObject(cityData);
            JSONArray locationArray = jsonObject.getJSONArray("location");
            if (locationArray.length() > 0) {
                JSONObject locationObject = locationArray.getJSONObject(0);
                return locationObject.getString("id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void fetchWeatherData(String locationId) {
        // 获取当前天气
        fetchCurrentWeather(locationId);
        // 获取3天预报
        fetchForecastWeather(locationId);
    }
    
    private void fetchCurrentWeather(String locationId) {
        String apiUrl = "https://devapi.qweather.com/v7/weather/now?location=" + locationId + "&key=" + API_KEY;
        
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(apiUrl).build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "获取当前天气失败", Toast.LENGTH_SHORT).show());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    rawWeatherData = responseBody; // 保存原始天气数据用于AI分析
                    currentWeather = parseCurrentWeather(responseBody);
                    getActivity().runOnUiThread(() -> updateCurrentWeatherUI());
                }
            }
        });
    }
    
    private void fetchForecastWeather(String locationId) {
        String apiUrl = "https://devapi.qweather.com/v7/weather/3d?location=" + locationId + "&key=" + API_KEY;
        
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(apiUrl).build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "获取预报天气失败", Toast.LENGTH_SHORT).show());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    rawForecastData = responseBody; // 保存原始预报数据用于AI分析
                    weatherDataList = parseForecastWeather(responseBody);
                    getActivity().runOnUiThread(() -> {
                        updateForecastUI();
                        updateCharts();
                        showAllCards();
                    });
                }
            }
        });
    }
    
    private CurrentWeather parseCurrentWeather(String weatherData) {
        try {
            JSONObject jsonObject = new JSONObject(weatherData);
            JSONObject nowObject = jsonObject.getJSONObject("now");
            
            CurrentWeather weather = new CurrentWeather();
            weather.setObsTime(nowObject.optString("obsTime", ""));
            weather.setTemp(nowObject.optString("temp", "0"));
            weather.setFeelsLike(nowObject.optString("feelsLike", "0"));
            weather.setIcon(nowObject.optString("icon", ""));
            weather.setText(nowObject.optString("text", ""));
            weather.setWindDir(nowObject.optString("windDir", ""));
            weather.setWindScale(nowObject.optString("windScale", ""));
            weather.setWindSpeed(nowObject.optString("windSpeed", ""));
            weather.setHumidity(nowObject.optString("humidity", "0"));
            weather.setPrecip(nowObject.optString("precip", "0"));
            weather.setPressure(nowObject.optString("pressure", "0"));
            weather.setVis(nowObject.optString("vis", "0"));
            weather.setCloud(nowObject.optString("cloud", "0"));
            
            return weather;
        } catch (JSONException e) {
            e.printStackTrace();
            return new CurrentWeather();
        }
    }
    
    private List<WeatherData> parseForecastWeather(String weatherData) {
        List<WeatherData> dataList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(weatherData);
            JSONArray dailyArray = jsonObject.getJSONArray("daily");
            
            for (int i = 0; i < dailyArray.length(); i++) {
                JSONObject dailyObject = dailyArray.getJSONObject(i);
                
                WeatherData data = new WeatherData();
                data.setFxDate(dailyObject.optString("fxDate", ""));
                data.setTempMax(dailyObject.optString("tempMax", "0"));
                data.setTempMin(dailyObject.optString("tempMin", "0"));
                data.setTextDay(dailyObject.optString("textDay", ""));
                data.setTextNight(dailyObject.optString("textNight", ""));
                data.setIconDay(dailyObject.optString("iconDay", ""));
                data.setIconNight(dailyObject.optString("iconNight", ""));
                data.setHumidity(dailyObject.optString("humidity", "0"));
                data.setPrecip(dailyObject.optString("precip", "0"));
                data.setPressure(dailyObject.optString("pressure", "0"));
                data.setWindScaleDay(dailyObject.optString("windScaleDay", ""));
                data.setWindDirDay(dailyObject.optString("windDirDay", ""));
                data.setVis(dailyObject.optString("vis", "0"));
                data.setUvIndex(dailyObject.optString("uvIndex", "0"));
                data.setCloud(dailyObject.optString("cloud", "0"));
                data.setSunrise(dailyObject.optString("sunrise", ""));
                data.setSunset(dailyObject.optString("sunset", ""));
                
                dataList.add(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return dataList;
    }
    
    private void updateCurrentWeatherUI() {
        if (currentWeather != null) {
            tvCurrentLocation.setText(currentLocation);
            tvCurrentTemp.setText(currentWeather.getTemp() + "°");
            tvCurrentCondition.setText(currentWeather.getText());
            tvFeelsLike.setText("体感温度: " + currentWeather.getFeelsLike() + "°");
            tvCurrentHumidity.setText(currentWeather.getHumidity() + "%");
            tvCurrentPressure.setText(currentWeather.getPressure() + "hPa");
            tvCurrentVis.setText(currentWeather.getVis() + "km");
            
            // 更新天气图标
            ImageView weatherIcon = getView().findViewById(R.id.weatherIcon);
            if (weatherIcon != null) {
                weatherIcon.setImageResource(getWeatherIcon(currentWeather.getText()));
                // 应用旋转动画效果到天气图标
                if (currentWeather.getText() != null && 
                    (currentWeather.getText().contains("风") || currentWeather.getText().contains("阵雨"))) {
                    AnimationUtils.rotateWeatherIcon(weatherIcon, 10000);
                }
            }
        }
    }
    
    private void updateForecastUI() {
        if (forecastAdapter != null) {
            forecastAdapter.updateData(weatherDataList);
        }
    }
    
    private void updateCharts() {
        setupTemperatureChart();
        setupHumidityChart();
        setupPressureChart();
        setupWeatherDistributionChart();
    }
    
    private void setupTemperatureChart() {
        if (weatherDataList.isEmpty()) return;
        
        List<Entry> maxTempEntries = new ArrayList<>();
        List<Entry> minTempEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        for (int i = 0; i < weatherDataList.size(); i++) {
            WeatherData data = weatherDataList.get(i);
            maxTempEntries.add(new Entry(i, data.getTempMaxFloat()));
            minTempEntries.add(new Entry(i, data.getTempMinFloat()));
            labels.add(formatShortDate(data.getFxDate()));
        }
        
        LineDataSet maxTempDataSet = new LineDataSet(maxTempEntries, "最高温度");
        maxTempDataSet.setColor(Color.RED);
        maxTempDataSet.setCircleColor(Color.RED);
        maxTempDataSet.setLineWidth(2f);
        maxTempDataSet.setCircleRadius(4f);
        maxTempDataSet.setValueTextSize(10f);
        
        LineDataSet minTempDataSet = new LineDataSet(minTempEntries, "最低温度");
        minTempDataSet.setColor(Color.BLUE);
        minTempDataSet.setCircleColor(Color.BLUE);
        minTempDataSet.setLineWidth(2f);
        minTempDataSet.setCircleRadius(4f);
        minTempDataSet.setValueTextSize(10f);
        
        LineData lineData = new LineData(maxTempDataSet, minTempDataSet);
        temperatureChart.setData(lineData);
        
        XAxis xAxis = temperatureChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        
        Description desc = new Description();
        desc.setText("三天温度趋势");
        temperatureChart.setDescription(desc);
        
        temperatureChart.invalidate();
    }
    
    private void setupHumidityChart() {
        if (weatherDataList.isEmpty()) return;
        
        List<BarEntry> humidityEntries = new ArrayList<>();
        List<BarEntry> precipEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        for (int i = 0; i < weatherDataList.size(); i++) {
            WeatherData data = weatherDataList.get(i);
            humidityEntries.add(new BarEntry(i, data.getHumidityFloat()));
            precipEntries.add(new BarEntry(i, data.getPrecipFloat()));
            labels.add(formatShortDate(data.getFxDate()));
        }
        
        BarDataSet humidityDataSet = new BarDataSet(humidityEntries, "湿度(%)");
        humidityDataSet.setColor(Color.CYAN);
        
        BarDataSet precipDataSet = new BarDataSet(precipEntries, "降水量(mm)");
        precipDataSet.setColor(Color.BLUE);
        
        BarData barData = new BarData(humidityDataSet, precipDataSet);
        barData.setBarWidth(0.3f);
        
        humidityChart.setData(barData);
        humidityChart.groupBars(0f, 0.4f, 0f);
        
        XAxis xAxis = humidityChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        
        Description desc = new Description();
        desc.setText("湿度和降水量对比");
        humidityChart.setDescription(desc);
        
        humidityChart.invalidate();
    }
    
    private void setupPressureChart() {
        if (weatherDataList.isEmpty()) return;
        
        List<Entry> pressureEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        for (int i = 0; i < weatherDataList.size(); i++) {
            WeatherData data = weatherDataList.get(i);
            pressureEntries.add(new Entry(i, data.getPressureFloat()));
            labels.add(formatShortDate(data.getFxDate()));
        }
        
        LineDataSet pressureDataSet = new LineDataSet(pressureEntries, "气压(hPa)");
        pressureDataSet.setColor(Color.GREEN);
        pressureDataSet.setCircleColor(Color.GREEN);
        pressureDataSet.setLineWidth(3f);
        pressureDataSet.setCircleRadius(5f);
        pressureDataSet.setValueTextSize(10f);
        
        LineData lineData = new LineData(pressureDataSet);
        pressureChart.setData(lineData);
        
        XAxis xAxis = pressureChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        
        Description desc = new Description();
        desc.setText("气压变化趋势");
        pressureChart.setDescription(desc);
        
        pressureChart.invalidate();
    }
    
    private void setupWeatherDistributionChart() {
        if (weatherDataList.isEmpty()) return;
        
        Map<String, Integer> weatherCount = new HashMap<>();
        for (WeatherData data : weatherDataList) {
            String weather = data.getTextDay();
            weatherCount.put(weather, weatherCount.getOrDefault(weather, 0) + 1);
        }
        
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : weatherCount.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }
        
        PieDataSet dataSet = new PieDataSet(entries, "天气状况分布");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);
        
        PieData pieData = new PieData(dataSet);
        weatherDistributionChart.setData(pieData);
        
        Description desc = new Description();
        desc.setText("三天天气状况统计");
        weatherDistributionChart.setDescription(desc);
        
        weatherDistributionChart.invalidate();
    }
    
    private void showAllCards() {
        // 应用动画效果，逐个显示卡片
        AnimationUtils.fadeIn(currentWeatherCard, 500, 0);
        AnimationUtils.fadeIn(temperatureChartCard, 500, 100);
        AnimationUtils.fadeIn(humidityChartCard, 500, 200);
        AnimationUtils.fadeIn(pressureChartCard, 500, 300);
        AnimationUtils.fadeIn(weatherDistributionCard, 500, 400);
        AnimationUtils.fadeIn(forecastCard, 500, 500);
        AnimationUtils.fadeIn(aiSuggestionCard, 500, 600);
    }
    
    private String formatShortDate(String dateStr) {
        try {
            String[] parts = dateStr.split("-");
            if (parts.length == 3) {
                return parts[1] + "/" + parts[2];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }
    
    // 添加获取天气图标的方法
    private int getWeatherIcon(String weatherText) {
        if (weatherText == null) return android.R.drawable.ic_menu_help;
        
        if (weatherText.contains("晴")) {
            return R.drawable.weather_icon_sunny;
        } else if (weatherText.contains("多云")) {
            return R.drawable.weather_icon_cloudy;
        } else if (weatherText.contains("雨")) {
            if (weatherText.contains("雷")) {
                return R.drawable.weather_icon_thunder;
            }
            return R.drawable.weather_icon_rainy;
        } else if (weatherText.contains("雪")) {
            return R.drawable.weather_icon_snow;
        } else if (weatherText.contains("风") || weatherText.contains("飓风")) {
            return R.drawable.weather_icon_windy;
        } else if (weatherText.contains("雾") || weatherText.contains("霾")) {
            return R.drawable.weather_icon_cloudy; // 暂时用多云图标替代
        }
        
        // 默认图标
        return R.drawable.weather_icon_sunny;
    }
    
    /**
     * 提交用户反馈
     */
    private void submitFeedback() {
        String feedbackText = etFeedbackText.getText().toString().trim();
        String feedbackType = isPositiveFeedback ? "正面" : "负面";
        
        // 构建反馈数据
        JSONObject feedbackData = new JSONObject();
        try {
            feedbackData.put("type", feedbackType);
            feedbackData.put("text", feedbackText);
            feedbackData.put("location", currentLocation);
            feedbackData.put("timestamp", System.currentTimeMillis());
            
            // 这里可以将反馈数据发送到服务器或保存在本地
            // 模拟发送反馈
            Toast.makeText(getContext(), "感谢您的反馈！", Toast.LENGTH_SHORT).show();
            
            // 隐藏反馈组件
            feedbackLayout.setVisibility(View.GONE);
            
            // 清除输入
            etFeedbackText.setText("");
            
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "提交反馈失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 获取AI天气建议
     */
    private void getAiWeatherSuggestions() {
        if (getActivity() == null) return;
        
        // 应用动画效果
        AnimationUtils.applyPulseAnimation(btnGetSuggestions);
        
        // 准备天气数据字符串
        StringBuilder weatherInfo = new StringBuilder();
        weatherInfo.append("当前位置: ").append(currentLocation).append("\n");
        
        if (currentWeather != null) {
            weatherInfo.append("当前温度: ").append(currentWeather.getTemp()).append("°C\n");
            weatherInfo.append("体感温度: ").append(currentWeather.getFeelsLike()).append("°C\n");
            weatherInfo.append("天气状况: ").append(currentWeather.getText()).append("\n");
            weatherInfo.append("湿度: ").append(currentWeather.getHumidity()).append("%\n");
            weatherInfo.append("气压: ").append(currentWeather.getPressure()).append("hPa\n");
            weatherInfo.append("能见度: ").append(currentWeather.getVis()).append("km\n");
            weatherInfo.append("风向风力: ").append(currentWeather.getWindDir())
                     .append(" ").append(currentWeather.getWindScale()).append("级\n");
        }
        
        // 添加预报数据
        if (!weatherDataList.isEmpty()) {
            weatherInfo.append("\n未来天气预报:\n");
            for (WeatherData data : weatherDataList) {
                weatherInfo.append(formatShortDate(data.getDate())).append(": ");
                weatherInfo.append("白天 ").append(data.getTextDay()).append(", ");
                weatherInfo.append("夜间 ").append(data.getTextNight()).append(", ");
                weatherInfo.append("温度 ").append(data.getTempMin()).append("-").append(data.getTempMax()).append("°C, ");
                weatherInfo.append("降水量 ").append(data.getPrecip()).append("mm, ");
                weatherInfo.append("湿度 ").append(data.getHumidity()).append("%, ");
                weatherInfo.append("紫外线指数 ").append(data.getUvIndex()).append("\n");
            }
        }
        
        // 显示加载状态
        tvAiAnalysisTitle.setText("正在分析天气数据...");
        tvWeatherSuggestion.setText("正在生成天气建议...");
        tvClothingSuggestion.setText("正在生成穿衣建议...");
        tvHealthSuggestion.setText("正在生成健康建议...");
        btnGetSuggestions.setEnabled(false);
        
        // 优化后的天气分析提示词
        String weatherPrompt = "作为专业气象分析师，请根据以下天气数据，分析当前天气情况，并提供详细的出行建议、需要注意的安全事项等。" +
                              "考虑温度、湿度、风力和降水等因素，评估可能的风险和影响。" +
                              "使用简洁专业的语言，突出重点，不超过200字。\n\n" + weatherInfo.toString();
        
        // 优化后的穿衣建议提示词
        String clothingPrompt = "作为专业时尚顾问，请根据以下天气数据，分析天气变化趋势，提供实用的穿衣搭配建议。" +
                               "考虑温度变化、湿度、降水和UV指数等，推荐适合的衣物类型、厚度、材质和配饰。" +
                               "如果天气多变，请提供灵活的穿搭方案。使用具体、实用的建议，不超过250字。\n\n" + weatherInfo.toString();
        
        // 优化后的健康建议提示词
        String healthPrompt = "作为医疗健康专家，请根据以下天气数据，分析天气对人体健康的可能影响，尤其关注" +
                             "气温变化、空气质量、湿度、气压等对不同人群的影响。提供针对性的健康建议，" +
                             "特别是对老人、儿童、慢性病患者等敏感人群。建议应具体、科学，包含预防措施，不超过200字。\n\n" + weatherInfo.toString();
        
        // 1. 获取天气建议
        DeepSeekHelper.analyzeWeatherData(weatherPrompt, new DeepSeekHelper.OnAnalysisCompleteListener() {
            @Override
            public void onSuccess(String result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvWeatherSuggestion.setText(result);
                        tvAiAnalysisTitle.setText("AI智能分析 - " + currentLocation);
                    });
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvWeatherSuggestion.setText("获取天气建议失败: " + errorMessage);
                        AnimationUtils.shakeView(tvWeatherSuggestion);
                    });
                }
            }
        });
        
        // 2. 获取穿衣建议
        DeepSeekHelper.getClothingSuggestions(clothingPrompt, new DeepSeekHelper.OnAnalysisCompleteListener() {
            @Override
            public void onSuccess(String result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvClothingSuggestion.setText(result);
                    });
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvClothingSuggestion.setText("获取穿衣建议失败: " + errorMessage);
                        AnimationUtils.shakeView(tvClothingSuggestion);
                    });
                }
            }
        });
        
        // 3. 获取健康建议
        DeepSeekHelper.getHealthSuggestions(healthPrompt, new DeepSeekHelper.OnAnalysisCompleteListener() {
            @Override
            public void onSuccess(String result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvHealthSuggestion.setText(result);
                        btnGetSuggestions.setEnabled(true);
                        btnGetSuggestions.clearAnimation();
                        
                        // 显示反馈组件
                        feedbackLayout.setVisibility(View.VISIBLE);
                        AnimationUtils.fadeIn(feedbackLayout, 500, 0);
                    });
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvHealthSuggestion.setText("获取健康建议失败: " + errorMessage);
                        btnGetSuggestions.setEnabled(true);
                        btnGetSuggestions.clearAnimation();
                        AnimationUtils.shakeView(tvHealthSuggestion);
                    });
                }
            }
        });
    }
}