package com.example.weixin;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView compassImage;
    private TextView directionText;
    private TextView accuracyText;
    private ImageView backButton;
    
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    
    private float currentDegree = 0f;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];
    
    private boolean isAccelerometerSet = false;
    private boolean isMagnetometerSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_compass);
        
        initViews();
        initSensors();
        setupListeners();
    }
    
    private void initViews() {
        compassImage = findViewById(R.id.compass_image);
        directionText = findViewById(R.id.compass_direction);
        accuracyText = findViewById(R.id.compass_accuracy);
        backButton = findViewById(R.id.compass_back_button);
    }
    
    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            isAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            isMagnetometerSet = true;
        }
        
        if (isAccelerometerSet && isMagnetometerSet) {
            boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientation);
                float azimuthInRadians = orientation[0];
                float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);
                azimuthInDegrees = (azimuthInDegrees + 360) % 360;
                
                // 创建旋转动画
                RotateAnimation rotateAnimation = new RotateAnimation(
                    currentDegree, -azimuthInDegrees,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
                
                rotateAnimation.setDuration(250);
                rotateAnimation.setFillAfter(true);
                compassImage.startAnimation(rotateAnimation);
                
                currentDegree = -azimuthInDegrees;
                
                // 更新方位文字
                updateDirectionText(azimuthInDegrees);
            }
        }
    }
    
    private void updateDirectionText(float degree) {
        String direction;
        
        if (degree >= 337.5 || degree < 22.5) {
            direction = "北";
        } else if (degree >= 22.5 && degree < 67.5) {
            direction = "东北";
        } else if (degree >= 67.5 && degree < 112.5) {
            direction = "东";
        } else if (degree >= 112.5 && degree < 157.5) {
            direction = "东南";
        } else if (degree >= 157.5 && degree < 202.5) {
            direction = "南";
        } else if (degree >= 202.5 && degree < 247.5) {
            direction = "西南";
        } else if (degree >= 247.5 && degree < 292.5) {
            direction = "西";
        } else {
            direction = "西北";
        }
        
        directionText.setText(direction + " " + Math.round(degree) + "°");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            String accuracyLabel;
            
            switch (accuracy) {
                case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                    accuracyLabel = "高";
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                    accuracyLabel = "中";
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                    accuracyLabel = "低";
                    break;
                default:
                    accuracyLabel = "不可靠";
                    break;
            }
            
            accuracyText.setText("传感器精度: " + accuracyLabel);
        }
    }
} 