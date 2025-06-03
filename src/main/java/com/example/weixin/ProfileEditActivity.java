package com.example.weixin;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weixin.db.UserDBHelper;

public class ProfileEditActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView titleText;
    private EditText usernameEdit, nicknameEdit, studentIdEdit, classNameEdit, passwordEdit;
    private Button saveButton;
    
    private UserDBHelper dbHelper;
    private String currentUsername = "admin"; // 当前假设用户是admin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        
        dbHelper = new UserDBHelper(this);
        
        initViews();
        setupListeners();
        loadUserData();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.edit_back_button);
        titleText = findViewById(R.id.edit_title);
        
        usernameEdit = findViewById(R.id.et_username);
        passwordEdit = findViewById(R.id.et_password);
        nicknameEdit = findViewById(R.id.et_nickname);
        studentIdEdit = findViewById(R.id.et_student_id);
        classNameEdit = findViewById(R.id.et_class_name);
        
        saveButton = findViewById(R.id.btn_save);
        
        titleText.setText("编辑个人资料");
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });
    }
    
    private void loadUserData() {
        Cursor cursor = dbHelper.getUserByUsername(currentUsername);
        if (cursor != null && cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_USERNAME));
            String password = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_PASSWORD));
            String nickname = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_NICKNAME));
            String studentId = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_STUDENT_ID));
            String className = cursor.getString(cursor.getColumnIndex(UserDBHelper.COLUMN_CLASS_NAME));
            
            usernameEdit.setText(username);
            passwordEdit.setText(password);
            nicknameEdit.setText(nickname);
            studentIdEdit.setText(studentId);
            classNameEdit.setText(className);
            
            cursor.close();
        }
    }
    
    private void saveUserData() {
        String username = usernameEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();
        String nickname = nicknameEdit.getText().toString().trim();
        String studentId = studentIdEdit.getText().toString().trim();
        String className = classNameEdit.getText().toString().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        
        long result = dbHelper.saveUser(username, password, nickname, studentId, className);
        if (result != -1) {
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }
} 