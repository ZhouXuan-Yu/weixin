package com.example.weixin;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainWeixin extends FragmentActivity {

    private RadioGroup group;
    private RadioButton weixinButton;
    private RadioButton friendsButton;
    private RadioButton findButton;
    private RadioButton profileButton; // 个人资料按钮，替换原来的游戏按钮
    
    private WeixinFragment weixinFragment;
    private FriendFragment friendFragment;
    private FindFragment findFragment;
    private ProfileFragment profileFragment; // 个人资料页面，替换原来的游戏页面
    
    private Fragment currentFragment; // 当前显示的Fragment
    private TextView title; // 标题栏
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_weixin);
        
        initViews();
        setupListeners();
        
        // 默认选中微信页面
        weixinButton.setChecked(true);
        switchFragment(weixinFragment);
        title.setText("微信");
    }
    
    private void initViews() {
        group = findViewById(R.id.main_tab);
        weixinButton = findViewById(R.id.main_tab_weixin);
        friendsButton = findViewById(R.id.main_tab_friends);
        findButton = findViewById(R.id.main_tab_find);
        profileButton = findViewById(R.id.main_tab_profile); // 修改为个人资料按钮
        title = findViewById(R.id.main_title);
        
        // 初始化Fragment
        weixinFragment = new WeixinFragment();
        friendFragment = new FriendFragment();
        findFragment = new FindFragment();
        profileFragment = new ProfileFragment(); // 修改为个人资料Fragment
    }
    
    private void setupListeners() {
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.main_tab_weixin) {
                    switchFragment(weixinFragment);
                    title.setText("微信");
                } else if (checkedId == R.id.main_tab_friends) {
                    switchFragment(friendFragment);
                    title.setText("通讯录");
                } else if (checkedId == R.id.main_tab_find) {
                    switchFragment(findFragment);
                    title.setText("发现");
                } else if (checkedId == R.id.main_tab_profile) { // 修改为个人资料按钮
                    switchFragment(profileFragment);
                    title.setText("我");
                }
            }
        });
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