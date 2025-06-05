package com.example.weixin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class Welcome extends Activity implements View.OnClickListener {
    private Button mBtnLogin, mBtnRegister;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 删除顶部额外标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // 设置状态栏颜色与应用背景一致
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.white));
        
        // 状态栏深色图标
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        
        setContentView(R.layout.welcome);
        
        mBtnLogin = findViewById(R.id.btn_login);
        mBtnRegister = findViewById(R.id.btn_register);
        
        mBtnLogin.setOnClickListener(this);
        mBtnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_login) {
            Intent intent = new Intent(Welcome.this, LoginUser.class);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_register) {
            Intent intent = new Intent(Welcome.this, Reigister.class);
            startActivity(intent);
        }
    }
}
