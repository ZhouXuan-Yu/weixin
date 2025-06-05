package com.example.weixin;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainWeixin extends FragmentActivity implements View.OnClickListener {

    private LinearLayout tabWeixin;
    private LinearLayout tabFriends;
    private LinearLayout tabFind;
    private LinearLayout tabProfile;
    
    private ImageView imgWeixin;
    private ImageView imgFriends;
    private ImageView imgFind;
    private ImageView imgProfile;
    
    private TextView txtWeixin;
    private TextView txtFriends;
    private TextView txtFind;
    private TextView txtProfile;
    
    private WeixinFragment weixinFragment;
    private FriendFragment friendFragment;
    private FindFragment findFragment;
    private ProfileFragment profileFragment;
    
    private Fragment currentFragment; // 当前显示的Fragment
    private TextView title; // 标题栏
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_weixin);
        
        initViews();
        setupListeners();
        
        // 默认选中微信页面
        updateTabSelection(0);
    }
    
    private void initViews() {
        // 初始化标题栏
        title = findViewById(R.id.main_title);
        
        // 初始化底部导航栏
        tabWeixin = findViewById(R.id.main_tab_weixin);
        tabFriends = findViewById(R.id.main_tab_friends);
        tabFind = findViewById(R.id.main_tab_find);
        tabProfile = findViewById(R.id.main_tab_profile);
        
        // 初始化图标
        imgWeixin = findViewById(R.id.weixin_img);
        imgFriends = findViewById(R.id.contact_img);
        imgFind = findViewById(R.id.find_img);
        imgProfile = findViewById(R.id.self_img);
        
        // 初始化文本
        txtWeixin = findViewById(R.id.weixin_text);
        txtFriends = findViewById(R.id.contact_text);
        txtFind = findViewById(R.id.find_text);
        txtProfile = findViewById(R.id.self_text);
        
        // 初始化Fragment
        weixinFragment = new WeixinFragment();
        friendFragment = new FriendFragment();
        findFragment = new FindFragment();
        profileFragment = new ProfileFragment();
    }
    
    private void setupListeners() {
        tabWeixin.setOnClickListener(this);
        tabFriends.setOnClickListener(this);
        tabFind.setOnClickListener(this);
        tabProfile.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.main_tab_weixin) {
            updateTabSelection(0);
        } else if (v.getId() == R.id.main_tab_friends) {
            updateTabSelection(1);
        } else if (v.getId() == R.id.main_tab_find) {
            updateTabSelection(2);
        } else if (v.getId() == R.id.main_tab_profile) {
            updateTabSelection(3);
        }
    }
    
    /**
     * 更新底部导航栏状态和切换Fragment
     */
    private void updateTabSelection(int index) {
        // 重置所有Tab状态
        resetTabState();
        
        // 根据选中的Tab设置状态和切换Fragment
        switch (index) {
            case 0:
                // 微信Tab
                imgWeixin.setImageResource(R.drawable.tab_weixin_pressed);
                txtWeixin.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                title.setText("微信");
                switchFragment(weixinFragment);
                break;
                
            case 1:
                // 通讯录Tab
                imgFriends.setImageResource(R.drawable.tab_address_pressed);
                txtFriends.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                title.setText("通讯录");
                switchFragment(friendFragment);
                break;
                
            case 2:
                // 发现Tab
                imgFind.setImageResource(R.drawable.tab_find_frd_pressed);
                txtFind.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                title.setText("发现");
                switchFragment(findFragment);
                break;
                
            case 3:
                // 我Tab
                imgProfile.setImageResource(R.drawable.tab_settings_pressed);
                txtProfile.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                title.setText("我");
                switchFragment(profileFragment);
                break;
        }
    }
    
    /**
     * 重置所有Tab状态
     */
    private void resetTabState() {
        imgWeixin.setImageResource(R.drawable.tab_weixin_normal);
        imgFriends.setImageResource(R.drawable.tab_address_normal);
        imgFind.setImageResource(R.drawable.tab_find_frd_normal);
        imgProfile.setImageResource(R.drawable.tab_settings_normal);
        
        txtWeixin.setTextColor(getResources().getColor(android.R.color.darker_gray));
        txtFriends.setTextColor(getResources().getColor(android.R.color.darker_gray));
        txtFind.setTextColor(getResources().getColor(android.R.color.darker_gray));
        txtProfile.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }
    
    /**
     * 切换Fragment
     */
    private void switchFragment(Fragment targetFragment) {
        if (currentFragment == targetFragment) {
            return;
        }
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        
        if (targetFragment.isAdded()) {
            transaction.show(targetFragment);
        } else {
            transaction.add(R.id.main_content, targetFragment);
        }
        
        transaction.commit();
        currentFragment = targetFragment;
    }
}