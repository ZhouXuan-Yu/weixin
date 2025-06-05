package com.example.weixin;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.weixin.db.UserDBHelper;

public class ProfileFragment extends Fragment {
    
    private ImageView avatarView;
    private TextView usernameText, nicknameText, studentIdText, classText;
    private Button editProfileButton;
    private UserDBHelper dbHelper;
    private View settingsItem, logoutItem;
    private ImageView menuButton;
    
    private String currentUsername = "admin"; // 默认用户
    private String nickname = "";
    private String studentId = "";
    private String className = "";
    private String email = "zhouyuxuan@student.edu.cn";
    private String phone = "18667890123";
    private String major = "智能科学与技术";
    private String college = "信息科学与工程学院";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // 启用选项菜单
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        dbHelper = new UserDBHelper(getActivity());
        
        initViews(view);
        setupListeners();
        
        updateUserData();
        loadUserData();
        
        return view;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // 在Fragment中不直接使用菜单，而是使用自定义的右上角按钮
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }
    
    private void initViews(View view) {
        avatarView = view.findViewById(R.id.profile_avatar);
        usernameText = view.findViewById(R.id.profile_username);
        nicknameText = view.findViewById(R.id.profile_nickname);
        studentIdText = view.findViewById(R.id.profile_student_id);
        classText = view.findViewById(R.id.profile_class);
        editProfileButton = view.findViewById(R.id.btn_edit_profile);
        settingsItem = view.findViewById(R.id.settings_item);
        logoutItem = view.findViewById(R.id.logout_item);
        
        // 添加右上角菜单按钮
        RelativeLayout titleLayout = view.findViewById(R.id.title_layout);
        menuButton = new ImageView(getContext());
        menuButton.setImageResource(R.drawable.menu_icon);
        menuButton.setId(View.generateViewId());
        
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.rightMargin = 16;
        
        menuButton.setLayoutParams(params);
        titleLayout.addView(menuButton);
    }
    
    private void setupListeners() {
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
                startActivity(intent);
            }
        });
        
        // 设置右上角菜单按钮点击事件
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsMenu(v);
            }
        });
        
        settingsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "设置功能", Toast.LENGTH_SHORT).show();
            }
        });
        
        logoutItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                    .setTitle("退出登录")
                    .setMessage("确定要退出登录吗？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getActivity(), Welcome.class);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
            }
        });
    }
    
    // 显示右上角选项菜单
    private void showOptionsMenu(View view) {
        final String[] options = {"个人信息", "学籍信息", "安全设置", "关于"};
        
        new AlertDialog.Builder(getContext())
            .setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            showPersonalInfoDialog();
                            break;
                        case 1:
                            showAcademicInfoDialog();
                            break;
                        case 2:
                            Toast.makeText(getContext(), "安全设置", Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            Toast.makeText(getContext(), "关于", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            })
            .show();
    }
    
    // 显示个人详细信息对话框
    private void showPersonalInfoDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_personal_info, null);
        
        TextView txtUsername = dialogView.findViewById(R.id.txt_dialog_username);
        TextView txtNickname = dialogView.findViewById(R.id.txt_dialog_nickname);
        TextView txtStudentId = dialogView.findViewById(R.id.txt_dialog_student_id);
        TextView txtPhone = dialogView.findViewById(R.id.txt_dialog_phone);
        TextView txtEmail = dialogView.findViewById(R.id.txt_dialog_email);
        
        txtUsername.setText("用户名: " + currentUsername);
        txtNickname.setText("姓名: " + nickname);
        txtStudentId.setText("学号: " + studentId);
        txtPhone.setText("手机: " + phone);
        txtEmail.setText("邮箱: " + email);
        
        new AlertDialog.Builder(getContext())
            .setTitle("个人信息")
            .setView(dialogView)
            .setPositiveButton("确定", null)
            .show();
    }
    
    // 显示学籍信息对话框
    private void showAcademicInfoDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_academic_info, null);
        
        TextView txtStudentId = dialogView.findViewById(R.id.txt_dialog_student_id);
        TextView txtName = dialogView.findViewById(R.id.txt_dialog_name);
        TextView txtCollege = dialogView.findViewById(R.id.txt_dialog_college);
        TextView txtMajor = dialogView.findViewById(R.id.txt_dialog_major);
        TextView txtClass = dialogView.findViewById(R.id.txt_dialog_class);
        
        txtStudentId.setText("学号: " + studentId);
        txtName.setText("姓名: " + nickname);
        txtCollege.setText("学院: " + college);
        txtMajor.setText("专业: " + major);
        txtClass.setText("班级: " + className);
        
        new AlertDialog.Builder(getContext())
            .setTitle("学籍信息")
            .setView(dialogView)
            .setPositiveButton("确定", null)
            .show();
    }
    
    private void updateUserData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserDBHelper.COLUMN_NICKNAME, "周雨轩");
        values.put(UserDBHelper.COLUMN_STUDENT_ID, "202252320116");
        values.put(UserDBHelper.COLUMN_CLASS_NAME, "2022级智能科学与技术一班");
        values.put(UserDBHelper.COLUMN_EMAIL, email);
        values.put(UserDBHelper.COLUMN_PHONE, phone);
        values.put(UserDBHelper.COLUMN_MAJOR, major);
        values.put(UserDBHelper.COLUMN_COLLEGE, college);
        
        db.update(UserDBHelper.TABLE_USER, values, 
                UserDBHelper.COLUMN_USERNAME + " = ?", 
                new String[]{currentUsername});
    }
    
    private void loadUserData() {
        Cursor cursor = dbHelper.getUserByUsername(currentUsername);
        if (cursor != null && cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_USERNAME));
            nickname = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_NICKNAME));
            studentId = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_STUDENT_ID));
            className = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_CLASS_NAME));
            
            // 可能需要添加更多字段的获取
            try {
                email = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_EMAIL));
                phone = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_PHONE));
                major = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_MAJOR));
                college = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_COLLEGE));
            } catch (Exception e) {
                // 可能字段不存在，使用默认值
            }
            
            usernameText.setText("用户名: " + username);
            nicknameText.setText("姓名: " + nickname);
            studentIdText.setText("学号: " + studentId);
            classText.setText("班级: " + className);
            
            cursor.close();
        }
    }
} 