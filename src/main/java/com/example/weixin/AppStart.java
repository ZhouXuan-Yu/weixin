package com.example.weixin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;

public class AppStart extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 删除顶部额外标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // 设置状态栏颜色与应用背景一致
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.white));
        
        // 状态栏深色图标
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        
        setContentView(R.layout.app_start);
        
        // 在UI线程中延迟跳转到欢迎页面
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(AppStart.this, Welcome.class);
                startActivity(intent);
                finish();
            }
        }, 2000); // 延迟2秒
    }
}