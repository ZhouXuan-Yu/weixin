package com.example.weixin;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CompassActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    private static final String TAG = "CompassActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_GAME;
    private static final int ANIMATION_DURATION = 250;
    private static final float ALPHA = 0.12f; // 低通滤波系数
    private static final int CALIBRATION_HISTORY_SIZE = 100; // 校准历史记录大小
    private static final float CALIBRATION_THRESHOLD = 50f; // 校准阈值

    private ImageView compassImage;
    private ImageView compassBackground;
    private TextView degreesText;
    private TextView directionText;
    private TextView latitudeView;
    private TextView longitudeView;
    private TextView elevationView;
    private ImageView backButton;
    private TextView accuracyText;
    private CardView calibrationCard;
    private ProgressBar accuracyProgress;
    private TextView magneticFieldText;
    
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Sensor gravitySensor;
    private Sensor rotationVectorSensor;
    private LocationManager locationManager;
    
    private float currentDegree = 0f;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private float[] gravityReading = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];
    
    private boolean isAccelerometerSet = false;
    private boolean isMagnetometerSet = false;
    private boolean isGravitySet = false;
    private boolean useRotationVectorSensor = false;
    
    private int magnetometerAccuracy = 0; // 0-3, 3 is highest
    private boolean needsCalibration = true;
    private boolean isCalibrated = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable calibrationChecker;
    
    // 平滑过滤后的方位角
    private float filteredAzimuth = 0f;
    private float filteredPitch = 0f;
    private float filteredRoll = 0f;
    
    // 磁场强度相关
    private float magneticFieldStrength = 0f;
    private List<Float> magneticFieldHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        
        initViews();
        initSensors();
        setupListeners();
        requestLocationPermissions();
        setupCalibrationCheck();
    }
    
    private void initViews() {
        compassImage = findViewById(R.id.compassImage);
        compassBackground = findViewById(R.id.compassBackground);
        degreesText = findViewById(R.id.degreesText);
        directionText = findViewById(R.id.directionText);
        latitudeView = findViewById(R.id.latitudeView);
        longitudeView = findViewById(R.id.longitudeView);
        elevationView = findViewById(R.id.elevationView);
        backButton = findViewById(R.id.compass_back_button);
        accuracyText = findViewById(R.id.compass_accuracy);
        calibrationCard = findViewById(R.id.calibration_card);
        accuracyProgress = findViewById(R.id.accuracy_progress);
        
        try {
            magneticFieldText = findViewById(R.id.magnetic_field_text);
        } catch (Exception e) {
            // 磁场强度视图可能不存在
            Log.d(TAG, "磁场强度视图不存在");
        }
    }
    
    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        // 检查传感器可用性
        if (rotationVectorSensor != null) {
            useRotationVectorSensor = true;
            Log.d(TAG, "使用旋转矢量传感器");
        }
        
        if (accelerometer == null || magnetometer == null) {
            Toast.makeText(this, "您的设备不支持指南针功能", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    private void setupCalibrationCheck() {
        calibrationChecker = new Runnable() {
            @Override
            public void run() {
                updateCalibrationStatus();
                if (needsCalibration && calibrationCard != null) {
                    if (calibrationCard.getVisibility() != View.VISIBLE) {
                        calibrationCard.setVisibility(View.VISIBLE);
                    }
                } else if (calibrationCard != null) {
                    if (calibrationCard.getVisibility() != View.GONE) {
                        calibrationCard.setVisibility(View.GONE);
                    }
                }
                
                handler.postDelayed(this, 1000);
            }
        };
        
        handler.post(calibrationChecker);
    }
    
    private void updateCalibrationStatus() {
        // 使用磁场历史记录方差来评估校准状态
        if (magneticFieldHistory.size() >= 10) {
            float variance = calculateVariance(magneticFieldHistory);
            isCalibrated = variance < CALIBRATION_THRESHOLD;
            needsCalibration = !isCalibrated;
            
            if (magneticFieldText != null) {
                magneticFieldText.setText(String.format(Locale.getDefault(), "磁场: %.1f μT", magneticFieldStrength));
            }
        }
    }
    
    private float calculateVariance(List<Float> values) {
        if (values.isEmpty()) return 0;
        
        float sum = 0;
        for (float value : values) {
            sum += value;
        }
        float mean = sum / values.size();
        
        float squaredDiffSum = 0;
        for (float value : values) {
            float diff = value - mean;
            squaredDiffSum += diff * diff;
        }
        
        return squaredDiffSum / values.size();
    }
    
    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "需要定位权限才能显示位置信息", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000, // 1秒更新一次
                    0.1f,  // 0.1米变化更新
                    this);
            
            // 尝试使用网络位置提供者
            try {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000,
                        0.1f,
                        this);
            } catch (Exception e) {
                Log.e(TAG, "无法使用网络位置提供者", e);
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // 根据可用传感器注册监听器
        if (useRotationVectorSensor) {
            sensorManager.registerListener(this, rotationVectorSensor, SENSOR_DELAY);
        } else {
            sensorManager.registerListener(this, accelerometer, SENSOR_DELAY);
            sensorManager.registerListener(this, magnetometer, SENSOR_DELAY);
        }
        
        // 如果有重力传感器，优先使用它
        if (gravitySensor != null) {
            sensorManager.registerListener(this, gravitySensor, SENSOR_DELAY);
        }
        
        // 始终监听磁力计用于校准
        if (!useRotationVectorSensor) {
            sensorManager.registerListener(this, magnetometer, SENSOR_DELAY);
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.1f, this);
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0.1f, this);
            } catch (Exception e) {
                Log.e(TAG, "无法使用网络位置提供者", e);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(this);
        handler.removeCallbacks(calibrationChecker);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
                isAccelerometerSet = true;
                break;
                
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
                isMagnetometerSet = true;
                updateMagneticField(event.values);
                break;
                
            case Sensor.TYPE_GRAVITY:
                System.arraycopy(event.values, 0, gravityReading, 0, event.values.length);
                isGravitySet = true;
                break;
                
            case Sensor.TYPE_ROTATION_VECTOR:
                updateFromRotationVector(event.values);
                break;
        }
        
        // 传统方法：使用加速度计和磁力计
        if (!useRotationVectorSensor && isAccelerometerSet && isMagnetometerSet) {
            updateOrientation();
        }
    }
    
    private void updateMagneticField(float[] values) {
        // 计算磁场强度
        magneticFieldStrength = (float) Math.sqrt(
                values[0] * values[0] + values[1] * values[1] + values[2] * values[2]
        );
        
        // 记录磁场历史
        magneticFieldHistory.add(magneticFieldStrength);
        if (magneticFieldHistory.size() > CALIBRATION_HISTORY_SIZE) {
            magneticFieldHistory.remove(0);
        }
    }
    
    private void updateFromRotationVector(float[] rotationVector) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);
        
        float[] orientation = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientation);
        
        float azimuthInRadians = orientation[0];
        float pitchInRadians = orientation[1];
        float rollInRadians = orientation[2];
        
        float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);
        float pitchInDegrees = (float) Math.toDegrees(pitchInRadians);
        float rollInDegrees = (float) Math.toDegrees(rollInRadians);
        
        // 确保方位角在0-360度范围内
        azimuthInDegrees = (azimuthInDegrees + 360) % 360;
        
        // 应用低通滤波器平滑方位角
        filteredAzimuth = lowPassFilter(azimuthInDegrees, filteredAzimuth);
        filteredPitch = lowPassFilter(pitchInDegrees, filteredPitch);
        filteredRoll = lowPassFilter(rollInDegrees, filteredRoll);
        
        updateCompassDisplay();
    }
    
    private void updateOrientation() {
        // 使用重力传感器或加速度传感器
        float[] gravity = isGravitySet ? gravityReading : lastAccelerometer;
        
        boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, lastMagnetometer);
        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientation);
            float azimuthInRadians = orientation[0];
            float pitchInRadians = orientation[1];
            float rollInRadians = orientation[2];
            
            float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);
            float pitchInDegrees = (float) Math.toDegrees(pitchInRadians);
            float rollInDegrees = (float) Math.toDegrees(rollInRadians);
            
            // 确保方位角在0-360度范围内
            azimuthInDegrees = (azimuthInDegrees + 360) % 360;
            
            // 应用低通滤波器平滑方位角
            filteredAzimuth = lowPassFilter(azimuthInDegrees, filteredAzimuth);
            filteredPitch = lowPassFilter(pitchInDegrees, filteredPitch);
            filteredRoll = lowPassFilter(rollInDegrees, filteredRoll);
            
            updateCompassDisplay();
        }
    }
    
    private void updateCompassDisplay() {
        // 创建旋转动画
        RotateAnimation rotateAnimation = new RotateAnimation(
            currentDegree, -filteredAzimuth,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f);
        
        rotateAnimation.setDuration(ANIMATION_DURATION);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setInterpolator(new DecelerateInterpolator());
        compassImage.startAnimation(rotateAnimation);
        
        currentDegree = -filteredAzimuth;
        
        // 更新度数显示 (使用原始角度，不使用过滤后的值，以保持精确度)
        degreesText.setText(String.format(Locale.getDefault(), "%.1f°", filteredAzimuth));
        
        // 更新方向文本
        updateDirectionText(filteredAzimuth);
    }
    
    /**
     * 低通滤波器，用于平滑传感器数据
     */
    private float lowPassFilter(float input, float output) {
        if (Math.abs(input - output) > 180) {
            // 处理跨越360/0度边界的情况
            if (input > output) {
                output += 360;
            } else {
                input += 360;
            }
        }
        output = output + ALPHA * (input - output);
        return output % 360;
    }
    
    private void updateDirectionText(float azimuthInDegrees) {
        String direction;
        
        if (azimuthInDegrees >= 337.5 || azimuthInDegrees < 22.5) {
            direction = "北";
        } else if (azimuthInDegrees >= 22.5 && azimuthInDegrees < 67.5) {
            direction = "东北";
        } else if (azimuthInDegrees >= 67.5 && azimuthInDegrees < 112.5) {
            direction = "东";
        } else if (azimuthInDegrees >= 112.5 && azimuthInDegrees < 157.5) {
            direction = "东南";
        } else if (azimuthInDegrees >= 157.5 && azimuthInDegrees < 202.5) {
            direction = "南";
        } else if (azimuthInDegrees >= 202.5 && azimuthInDegrees < 247.5) {
            direction = "西南";
        } else if (azimuthInDegrees >= 247.5 && azimuthInDegrees < 292.5) {
            direction = "西";
        } else {
            direction = "西北";
        }
        
        directionText.setText(direction);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetometerAccuracy = accuracy;
            updateAccuracyIndicator(accuracy);
            
            // 根据精度确定是否需要校准
            needsCalibration = (accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM);
            isCalibrated = !needsCalibration;
        }
    }
    
    /**
     * 更新精度指示器
     */
    private void updateAccuracyIndicator(final int accuracy) {
        if (accuracyText != null) {
            String accuracyLabel;
            int progressValue = 0;
            
            switch (accuracy) {
                case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                    accuracyLabel = "高精度";
                    progressValue = 100;
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                    accuracyLabel = "中等精度";
                    progressValue = 67;
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                    accuracyLabel = "低精度";
                    progressValue = 33;
                    break;
                case SensorManager.SENSOR_STATUS_UNRELIABLE:
                default:
                    accuracyLabel = "需要校准";
                    progressValue = 0;
                    break;
            }
            
            accuracyText.setText("传感器精度: " + accuracyLabel);
            
            if (accuracyProgress != null) {
                // 使用动画平滑过渡
                ValueAnimator animator = ValueAnimator.ofInt(accuracyProgress.getProgress(), progressValue);
                animator.setDuration(500);
                animator.setInterpolator(new LinearInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (int) animation.getAnimatedValue();
                        accuracyProgress.setProgress(value);
                    }
                });
                animator.start();
            }
        }
    }

    // 将经纬度格式化为度分秒格式
    private String formatDMS(double value, boolean isLatitude) {
        int degrees = (int) value;
        double minutesDouble = (value - degrees) * 60;
        int minutes = (int) minutesDouble;
        double secondsDouble = (minutesDouble - minutes) * 60;
        int seconds = (int) secondsDouble;
        
        String direction;
        if (isLatitude) {
            direction = degrees >= 0 ? "北纬" : "南纬";
        } else {
            direction = degrees >= 0 ? "东经" : "西经";
        }
        
        return String.format(Locale.getDefault(), "%s\n%d°%d′%d″", 
                direction, Math.abs(degrees), Math.abs(minutes), Math.abs(seconds));
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // 更新经纬度和海拔信息
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double altitude = location.getAltitude();
        
        latitudeView.setText(formatDMS(latitude, true));
        longitudeView.setText(formatDMS(longitude, false));
        elevationView.setText(String.format(Locale.getDefault(), "海拔\n%.1f 米", altitude));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // 兼容旧版本API
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        // 位置提供者启用
        Log.d(TAG, "位置提供者启用: " + provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // 位置提供者禁用
        Log.d(TAG, "位置提供者禁用: " + provider);
    }
    
    /**
     * 手动校准指南针
     */
    public void recalibrateCompass(View view) {
        Toast.makeText(this, "请在8字形轨迹移动手机进行校准", Toast.LENGTH_LONG).show();
        magneticFieldHistory.clear();
        needsCalibration = true;
        isCalibrated = false;
    }
} 