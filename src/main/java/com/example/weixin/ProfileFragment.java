package com.example.weixin;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.weixin.db.UserDBHelper;

public class ProfileFragment extends Fragment {
    
    private ImageView avatarView;
    private TextView usernameText, nicknameText, studentIdText, classText;
    private Button editProfileButton;
    private UserDBHelper dbHelper;
    
    private String currentUsername = "admin"; // 默认用户

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        dbHelper = new UserDBHelper(getActivity());
        
        initViews(view);
        setupListeners();
        
        updateUserData();
        
        return view;
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
    }
    
    private void setupListeners() {
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
                startActivity(intent);
            }
        });
    }
    
    private void updateUserData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserDBHelper.COLUMN_NICKNAME, "周雨轩");
        values.put(UserDBHelper.COLUMN_STUDENT_ID, "202252320116");
        values.put(UserDBHelper.COLUMN_CLASS_NAME, "2022级智能科学与技术一班");
        
        db.update(UserDBHelper.TABLE_USER, values, 
                UserDBHelper.COLUMN_USERNAME + " = ?", 
                new String[]{"admin"});
    }
    
    private void loadUserData() {
        Cursor cursor = dbHelper.getUserByUsername(currentUsername);
        if (cursor != null && cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_USERNAME));
            String nickname = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_NICKNAME));
            String studentId = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_STUDENT_ID));
            String className = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_CLASS_NAME));
            
            usernameText.setText("用户名: " + username);
            nicknameText.setText("昵称: " + nickname);
            studentIdText.setText("学号: " + studentId);
            classText.setText("班级: " + className);
            
            cursor.close();
        }
    }
} 