package com.example.weixin.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "weixin_user.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USER = "user_info";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_NICKNAME = "nickname";
    public static final String COLUMN_STUDENT_ID = "student_id";
    public static final String COLUMN_CLASS_NAME = "class_name";
    public static final String COLUMN_AVATAR = "avatar";

    // 建表语句
    private static final String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
            COLUMN_PASSWORD + " TEXT NOT NULL, " +
            COLUMN_NICKNAME + " TEXT, " +
            COLUMN_STUDENT_ID + " TEXT, " +
            COLUMN_CLASS_NAME + " TEXT, " +
            COLUMN_AVATAR + " TEXT" +
            ");";

    public UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER);
        
        // 添加默认用户
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, "admin");
        values.put(COLUMN_PASSWORD, "123456");
        values.put(COLUMN_NICKNAME, "周雨轩");
        values.put(COLUMN_STUDENT_ID, "202252320116");
        values.put(COLUMN_CLASS_NAME, "2022级智能科学与技术一班");
        db.insert(TABLE_USER, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    // 添加或更新用户信息
    public long saveUser(String username, String password, String nickname, 
                        String studentId, String className) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_NICKNAME, nickname);
        values.put(COLUMN_STUDENT_ID, studentId);
        values.put(COLUMN_CLASS_NAME, className);

        // 检查用户是否已存在
        Cursor cursor = db.query(TABLE_USER, new String[]{COLUMN_ID},
                COLUMN_USERNAME + " = ?", new String[]{username},
                null, null, null);
                
        long id = -1;
        
        if (cursor.moveToFirst()) {
            id = cursor.getLong(0);
            db.update(TABLE_USER, values, COLUMN_ID + " = ?", 
                    new String[]{String.valueOf(id)});
        } else {
            id = db.insert(TABLE_USER, null, values);
        }
        
        cursor.close();
        return id;
    }

    // 验证用户登录
    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, new String[]{COLUMN_ID},
                COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{username, password}, null, null, null);
                
        boolean isValid = cursor.moveToFirst();
        cursor.close();
        return isValid;
    }

    // 获取用户信息
    public Cursor getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USER, null,
                COLUMN_USERNAME + " = ?", new String[]{username},
                null, null, null);
    }
} 