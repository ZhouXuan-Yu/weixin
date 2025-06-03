package com.example.weixin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Reigister extends AppCompatActivity {
    EditText regName, regPhone, regPasswd;
    Button regButton;
    Mysql mysql;
    SQLiteDatabase db;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        regName = findViewById(R.id.reg_name);
        regPhone = findViewById(R.id.reg_phone);
        regPasswd = findViewById(R.id.reg_passwd);
        regButton = findViewById(R.id.reg_button);

        mysql = new Mysql(this, "Userinfo", null, 1);
        db = mysql.getReadableDatabase();
        sp = getSharedPreferences("useinfo", MODE_PRIVATE);

        regButton.setOnClickListener(new View.OnClickListener() {
            boolean flag = true; //判断用户是否已存在的标志位

            @Override
            public void onClick(View v) {
                String name = regName.getText().toString(); //用户名
                String phone = regPhone.getText().toString(); //手机号
                String password = regPasswd.getText().toString(); //密码

                if (name.equals("") || phone.equals("") || password.equals("")) {
                    Toast.makeText(Reigister.this, "用户名、手机号或密码不能为空！", Toast.LENGTH_LONG).show();
                } else {
                    Cursor cursor = db.query("logins", new String[]{"usname"}, null, null, null, null, null);

                    while (cursor.moveToNext()) {
                        if (cursor.getString(0).equals(name)) {
                            flag = false;
                            break;
                        }
                    }

                    if (flag) { //判断用户是否已存在
                        ContentValues cv = new ContentValues();
                        cv.put("usname", name);
                        cv.put("usphone", phone);
                        cv.put("uspwd", password);
                        db.insert("logins", null, cv);

                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("usname", name);
                        editor.putString("usphone", phone);
                        editor.putString("uspwd", password);
                        editor.commit();

                        Intent intent = new Intent(Reigister.this, LoginUser.class);
                        startActivity(intent);

                        Toast.makeText(Reigister.this, "注册成功！", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(Reigister.this, "用户已存在！", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
    public void rigister_activity_back(View v) {
        /*跳转到微信启动页*/
        Intent intent = new Intent();
        intent.setClass(com.example.weixin.Reigister.this, Welcome.class);
        startActivity(intent);
        com.example.weixin.Reigister.this.finish(); //结束当前activity
    }
}
