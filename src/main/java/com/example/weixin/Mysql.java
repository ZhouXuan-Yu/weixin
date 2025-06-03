package com.example.weixin;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Mysql extends SQLiteOpenHelper {
    public Mysql(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE logins (id INTEGER PRIMARY KEY AUTOINCREMENT, usname TEXT, usphone TEXT, uspwd TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 这里是数据库升级时的操作，可以根据需要进行修改
    }
}
