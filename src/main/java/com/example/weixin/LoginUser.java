package com.example.weixin;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginUser extends AppCompatActivity {

    EditText logPhone, logPasswd;
    Button logButton;
    MyHandler handler;
    Mysql mysql;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_phone);

        logPhone = findViewById(R.id.log_phone);
        logPasswd = findViewById(R.id.log_passwd);
        logButton = findViewById(R.id.log_button);

        handler = new MyHandler();
        mysql = new Mysql(this, "Userinfo", null, 1);
        db = mysql.getWritableDatabase();


        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = logPhone.getText().toString();
                String password = logPasswd.getText().toString();

                boolean loginSuccessful = checkLoginCredentials(phoneNumber, password);

                if (loginSuccessful) {
                    // 登录成功的处理逻辑
                    Message message = handler.obtainMessage(1);
                    handler.sendMessage(message);
                } else {
                    // 登录失败的处理逻辑
                    Message message = handler.obtainMessage(2);
                    handler.sendMessage(message);
                }
            }
        });
    }

    private boolean checkLoginCredentials(String phoneNumber, String password) {
        String[] columns = {"usname"};
        String selection = "usphone=? AND uspwd=?";
        String[] selectionArgs = {phoneNumber, password};
        Cursor cursor = db.query("logins", columns, selection, selectionArgs, null, null, null);
        boolean loginSuccessful = cursor.getCount() > 0;
        cursor.close();
        return loginSuccessful;
    }

    private void loginSuccess() {
        Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginUser.this, MainWeixin.class);
        startActivity(intent);
        finish();
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Log.i("aa", msg.what + "");
                    loginSuccess();
                    break;
                case 2:
                    Log.i("aa", msg.what + "");
                    new AlertDialog.Builder(LoginUser.this)
                            .setTitle("登录失败")
                            .setMessage("用户名或密码错误，请重新填写")
                            .setPositiveButton("确定", null)
                            .show();
                    break;
            }
        }
    }
}
