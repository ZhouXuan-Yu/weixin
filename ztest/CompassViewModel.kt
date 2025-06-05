package com.example.wxwork.ui.compass

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wxwork.data.entity.CompassData
import com.example.wxwork.data.entity.CompassDirection
import com.example.wxwork.data.entity.LocationInfo
import com.example.wxwork.sensors.CompassSensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 指南针ViewModel
 * 管理指南针相关的状态和业务逻辑
 */
class CompassViewModel(
    private val context: Context
) : ViewModel() {
    
    private val sensorManager = CompassSensorManager(context)
    
    private val _compassState = MutableStateFlow(CompassData())
    val compassState: StateFlow<CompassData> = _compassState.asStateFlow()
    
    private val _isCalibrating = MutableStateFlow(false)
    val isCalibrating: StateFlow<Boolean> = _isCalibrating.asStateFlow()
    
    private val _locationInfo = MutableStateFlow<LocationInfo?>(null)
    val locationInfo: StateFlow<LocationInfo?> = _locationInfo.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        startCompass()
    }
      /**
     * 启动指南针传感器
     */
    fun startCompass() {
        viewModelScope.launch {
            try {
                sensorManager.startListening()
                
                // 监听数据变化
                sensorManager.compassData.collect { compassData ->
                    _compassState.value = compassData
                }
                
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "启动指南针失败: ${e.message}"
            }
        }
    }
      /**
     * 停止指南针传感器
     */
    fun stopCompass() {
        sensorManager.stopListening()
    }
      /**
     * 开始校准
     */
    fun startCalibration() {
        _isCalibrating.value = true
        sensorManager.recalibrate()
    }
    
    /**
     * 停止校准
     */
    fun stopCalibration() {
        _isCalibrating.value = false
        // recalibrate方法会自动处理校准状态
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * 获取当前方向描述
     */
    fun getCurrentDirectionDescription(): String {
        val direction = _compassState.value.direction
        return when (direction) {
            CompassDirection.NORTH -> "正北"
            CompassDirection.NORTHEAST -> "东北"
            CompassDirection.EAST -> "正东"
            CompassDirection.SOUTHEAST -> "东南"
            CompassDirection.SOUTH -> "正南"
            CompassDirection.SOUTHWEST -> "西南"
            CompassDirection.WEST -> "正西"
            CompassDirection.NORTHWEST -> "西北"
        }
    }
    
    /**
     * 获取精度状态描述
     */
    fun getAccuracyDescription(): String {
        return when (_compassState.value.accuracy) {
            0 -> "不可靠"
            1 -> "低精度"
            2 -> "中等精度"
            3 -> "高精度"
            else -> "未知"
        }
    }
    
    /**
     * 是否需要校准
     */
    fun needsCalibration(): Boolean {
        return _compassState.value.accuracy < 2
    }
    
    /**
     * 重新校准指南针
     */
    fun recalibrateCompass() {
        sensorManager.recalibrate()
    }

    override fun onCleared() {
        super.onCleared()
        stopCompass()
    }
}
