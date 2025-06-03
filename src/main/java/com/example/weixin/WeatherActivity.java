package com.example.weixin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class WeatherActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_main);
        
        initView();
        setupListeners();
        
        // 在Activity的布局中添加WeatherFragment
        if (savedInstanceState == null) {
            // 只有在Activity首次创建时添加Fragment，避免重复添加
            WeatherFragment weatherFragment = new WeatherFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.weather_fragment_container, weatherFragment);
            fragmentTransaction.commit();
        }
    }
    
    private void initView() {
        backButton = findViewById(R.id.weather_back_button);
        titleText = findViewById(R.id.weather_title);
        
        titleText.setText("天气");
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 返回上一页
            }
        });
    }
} 