package com.example.wxwork.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import com.example.wxwork.data.entity.CompassData
import com.example.wxwork.data.entity.CompassDirection
import com.example.wxwork.data.entity.LocationInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

/**
 * 指南针传感器管理器
 * 管理方向传感器、磁力传感器、加速度传感器等
 */
class CompassSensorManager(private val context: Context) : SensorEventListener, LocationListener {
    
    companion object {
        private const val TAG = "CompassSensorManager"
        private const val ALPHA = 0.15f // 平滑滤波参数
        private const val CALIBRATION_THRESHOLD = 50f // 校准阈值
    }
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    
    // 传感器
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    private val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val gameRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
    
    // 传感器数据
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val gravityReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    
    // 平滑处理
    private var smoothedAzimuth = 0f
    private var smoothedPitch = 0f
    private var smoothedRoll = 0f
    
    // 校准相关
    private var isCalibrated = false
    private var magneticFieldStrength = 0f
    private val magneticFieldHistory = mutableListOf<Float>()
    
    // 状态流
    private val _compassData = MutableStateFlow(CompassData())
    val compassData: StateFlow<CompassData> = _compassData.asStateFlow()
    
    private val _locationInfo = MutableStateFlow(LocationInfo())
    val locationInfo: StateFlow<LocationInfo> = _locationInfo.asStateFlow()
    
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    
    /**
     * 开始监听传感器
     */
    fun startListening() {
        if (_isActive.value) return
        
        try {
            // 注册传感器监听器
            magnetometer?.let { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            }
            
            accelerometer?.let { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            }
            
            gravitySensor?.let { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            }
            
            rotationVector?.let { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            }
            
            gameRotationVector?.let { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            }
            
            // 请求位置更新
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L, // 1秒更新间隔
                    1f,    // 1米距离间隔
                    this
                )
                
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000L,
                    1f,
                    this
                )
            } catch (e: SecurityException) {
                Log.w(TAG, "缺少位置权限", e)
            }
            
            _isActive.value = true
            Log.d(TAG, "指南针传感器开始监听")
            
        } catch (e: Exception) {
            Log.e(TAG, "启动传感器监听失败", e)
        }
    }
    
    /**
     * 停止监听传感器
     */
    fun stopListening() {
        if (!_isActive.value) return
        
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(this)
        
        _isActive.value = false
        Log.d(TAG, "指南针传感器停止监听")
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                updateOrientation()
            }
            
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
                updateMagneticField(event.values)
                updateOrientation()
            }
            
            Sensor.TYPE_GRAVITY -> {
                System.arraycopy(event.values, 0, gravityReading, 0, gravityReading.size)
            }
            
            Sensor.TYPE_ROTATION_VECTOR -> {
                updateFromRotationVector(event.values)
            }
            
            Sensor.TYPE_GAME_ROTATION_VECTOR -> {
                updateFromGameRotationVector(event.values)
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        when (sensor.type) {
            Sensor.TYPE_MAGNETIC_FIELD -> {
                isCalibrated = accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
                updateCompassData()
            }
        }
    }
    
    /**
     * 更新方向角度
     */
    private fun updateOrientation() {
        // 使用重力传感器或加速度传感器数据
        val gravity = if (gravityReading.any { it != 0f }) gravityReading else accelerometerReading
        
        // 计算旋转矩阵
        val success = SensorManager.getRotationMatrix(
            rotationMatrix, null, gravity, magnetometerReading
        )
        
        if (success) {
            // 获取方向角度
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            
            // 转换为度数
            var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
            
            // 确保方位角在0-360度范围内
            azimuth = (azimuth + 360) % 360
            
            // 平滑处理
            smoothedAzimuth = lowPassFilter(smoothedAzimuth, azimuth, ALPHA)
            smoothedPitch = lowPassFilter(smoothedPitch, pitch, ALPHA)
            smoothedRoll = lowPassFilter(smoothedRoll, roll, ALPHA)
            
            updateCompassData()
        }
    }
    
    /**
     * 使用旋转向量更新方向
     */
    private fun updateFromRotationVector(rotationVector: FloatArray) {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)
        
        val orientationAngles = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        
        var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        azimuth = (azimuth + 360) % 360
        
        smoothedAzimuth = lowPassFilter(smoothedAzimuth, azimuth, ALPHA)
        updateCompassData()
    }
    
    /**
     * 使用游戏旋转向量更新方向（无磁干扰）
     */
    private fun updateFromGameRotationVector(rotationVector: FloatArray) {
        // 这个传感器不受磁场干扰，更适合游戏应用
        updateFromRotationVector(rotationVector)
    }
    
    /**
     * 更新磁场强度信息
     */
    private fun updateMagneticField(values: FloatArray) {
        // 计算磁场强度
        magneticFieldStrength = sqrt(
            values[0] * values[0] + values[1] * values[1] + values[2] * values[2]
        )
        
        // 记录磁场强度历史，用于校准判断
        magneticFieldHistory.add(magneticFieldStrength)
        if (magneticFieldHistory.size > 100) {
            magneticFieldHistory.removeAt(0)
        }
        
        // 检查校准状态
        checkCalibration()
    }
    
    /**
     * 检查校准状态
     */
    private fun checkCalibration() {
        if (magneticFieldHistory.size >= 10) {
            val variance = calculateVariance(magneticFieldHistory)
            isCalibrated = variance < CALIBRATION_THRESHOLD
        }
    }
      /**
     * 计算方差
     */
    private fun calculateVariance(values: List<Float>): Float {
        val mean = values.average().toFloat()
        val sumSquaredDiff = values.sumOf { (it - mean) * (it - mean).toDouble() }
        return (sumSquaredDiff / values.size).toFloat()
    }
    
    /**
     * 低通滤波器
     */
    private fun lowPassFilter(current: Float, new: Float, alpha: Float): Float {
        return current + alpha * (new - current)
    }
    
    /**
     * 更新指南针数据
     */
    private fun updateCompassData() {
        val direction = CompassDirection.fromDegree(smoothedAzimuth)
        
        val newData = CompassData(
            azimuth = smoothedAzimuth,
            pitch = smoothedPitch,
            roll = smoothedRoll,
            magneticField = magneticFieldStrength,
            accuracy = if (isCalibrated) SensorManager.SENSOR_STATUS_ACCURACY_HIGH else SensorManager.SENSOR_STATUS_ACCURACY_LOW,
            direction = direction,
            isCalibrated = isCalibrated
        )
        
        _compassData.value = newData
    }
    
    // LocationListener 实现
    override fun onLocationChanged(location: Location) {
        val newLocationInfo = LocationInfo(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            accuracy = location.accuracy,
            bearing = location.bearing,
            speed = location.speed,
            timestamp = location.time
        )
        
        _locationInfo.value = newLocationInfo
    }
    
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // Deprecated in API 29+
    }
    
    override fun onProviderEnabled(provider: String) {
        Log.d(TAG, "位置提供者启用: $provider")
    }
    
    override fun onProviderDisabled(provider: String) {
        Log.d(TAG, "位置提供者禁用: $provider")
    }
    
    /**
     * 重新校准指南针
     */
    fun recalibrate() {
        magneticFieldHistory.clear()
        isCalibrated = false
        smoothedAzimuth = 0f
        smoothedPitch = 0f
        smoothedRoll = 0f
        
        Log.d(TAG, "指南针重新校准")
    }
    
    /**
     * 检查传感器可用性
     */
    fun checkSensorAvailability(): Map<String, Boolean> {
        return mapOf(
            "magnetometer" to (magnetometer != null),
            "accelerometer" to (accelerometer != null),
            "gravity" to (gravitySensor != null),
            "rotation_vector" to (rotationVector != null),
            "game_rotation_vector" to (gameRotationVector != null),
            "location" to (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
                         locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        )
    }
}
