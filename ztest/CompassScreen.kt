package com.example.wxwork.ui.compass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.*

/**
 * 指南针页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreen(
    onNavigateBack: () -> Unit,
    viewModel: CompassViewModel = viewModel()
) {    val context = LocalContext.current
    val compassData by viewModel.compassState.collectAsState()
    val isCalibrating by viewModel.isCalibrating.collectAsState()
    val locationInfo by viewModel.locationInfo.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // 请求权限和启动传感器
    LaunchedEffect(Unit) {
        viewModel.startCompass()
    }
    
    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopCompass()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 顶部应用栏
        TopAppBar(
            title = {
                Text(
                    text = "指南针",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.recalibrateCompass() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "重新校准",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black
            )
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 指南针主体
                Box(
                    modifier = Modifier.size(280.dp),
                    contentAlignment = Alignment.Center
                ) {                    CompassView(
                        azimuth = compassData.azimuth,
                        isCalibrated = compassData.isCalibrated
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                  // 方向信息
                DirectionInfo(
                    direction = compassData.direction,
                    azimuth = compassData.azimuth
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 传感器信息
                SensorInfo(
                    compassData = compassData,
                    locationInfo = locationInfo ?: com.example.wxwork.data.entity.LocationInfo(),
                    isActive = true // 可以从 sensorManager 获取实际状态
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                  // 校准提示
                if (!compassData.isCalibrated) {
                    CalibrationHint()
                }
            }
        }
    }
}

/**
 * 指南针视图组件
 */
@Composable
fun CompassView(
    azimuth: Float,
    isCalibrated: Boolean
) {
    Canvas(
        modifier = Modifier
            .size(280.dp)
            .clip(CircleShape)
    ) {
        val center = size.center
        val radius = size.minDimension / 2f
        
        // 绘制外圆
        drawCircle(
            color = Color.White,
            radius = radius,
            center = center,
            style = Stroke(width = 4.dp.toPx())
        )
        
        // 绘制度数刻度
        drawDegreeMarks(center, radius)
        
        // 绘制方向标记
        drawDirectionMarks(center, radius)
        
        // 绘制指南针指针
        rotate(degrees = -azimuth, pivot = center) {
            drawCompassNeedle(center, radius, isCalibrated)
        }
        
        // 绘制中心圆点
        drawCircle(
            color = Color.White,
            radius = 8.dp.toPx(),
            center = center
        )
    }
}

/**
 * 绘制度数刻度
 */
private fun DrawScope.drawDegreeMarks(center: Offset, radius: Float) {
    for (i in 0 until 360 step 10) {
        val angle = Math.toRadians(i.toDouble())
        val startRadius = if (i % 30 == 0) radius - 30.dp.toPx() else radius - 15.dp.toPx()
        val strokeWidth = if (i % 30 == 0) 3.dp.toPx() else 1.dp.toPx()
        
        val startX = center.x + startRadius * cos(angle - PI/2).toFloat()
        val startY = center.y + startRadius * sin(angle - PI/2).toFloat()
        val endX = center.x + radius * cos(angle - PI/2).toFloat()
        val endY = center.y + radius * sin(angle - PI/2).toFloat()
        
        drawLine(
            color = Color.White,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = strokeWidth
        )
    }
}

/**
 * 绘制方向标记
 */
private fun DrawScope.drawDirectionMarks(center: Offset, radius: Float) {
    val directions = listOf(
        Pair("N", 0),
        Pair("E", 90),
        Pair("S", 180),
        Pair("W", 270)
    )
    
    val paint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        textSize = 24.sp.toPx()
        color = android.graphics.Color.WHITE
        textAlign = android.graphics.Paint.Align.CENTER
    }
    
    directions.forEach { (text, degree) ->
        val angle = Math.toRadians(degree.toDouble())
        val textRadius = radius - 50.dp.toPx()
        val x = center.x + textRadius * cos(angle - PI/2).toFloat()
        val y = center.y + textRadius * sin(angle - PI/2).toFloat() + 8.dp.toPx()
        
        drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
    }
}

/**
 * 绘制指南针指针
 */
private fun DrawScope.drawCompassNeedle(center: Offset, radius: Float, isCalibrated: Boolean) {
    val needleLength = radius * 0.7f
    val needleWidth = 8.dp.toPx()
    
    // 北方指针（红色）
    val northPath = Path().apply {
        moveTo(center.x, center.y - needleLength)
        lineTo(center.x - needleWidth, center.y)
        lineTo(center.x, center.y - needleWidth)
        lineTo(center.x + needleWidth, center.y)
        close()
    }
    
    drawPath(
        path = northPath,
        color = if (isCalibrated) Color.Red else Color.Red.copy(alpha = 0.5f)
    )
    
    // 南方指针（白色）
    val southPath = Path().apply {
        moveTo(center.x, center.y + needleLength)
        lineTo(center.x - needleWidth, center.y)
        lineTo(center.x, center.y + needleWidth)
        lineTo(center.x + needleWidth, center.y)
        close()
    }
    
    drawPath(
        path = southPath,
        color = if (isCalibrated) Color.White else Color.White.copy(alpha = 0.5f)
    )
}

/**
 * 方向信息组件
 */
@Composable
fun DirectionInfo(
    direction: com.example.wxwork.data.entity.CompassDirection,
    azimuth: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = direction.displayName,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "${azimuth.toInt()}°",
                fontSize = 24.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * 传感器信息组件
 */
@Composable
fun SensorInfo(
    compassData: com.example.wxwork.data.entity.CompassData,
    locationInfo: com.example.wxwork.data.entity.LocationInfo,
    isActive: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("传感器状态", if (isActive) "运行中" else "已停止")
                InfoItem("校准状态", if (compassData.isCalibrated) "已校准" else "需要校准")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("磁场强度", "${compassData.magneticField.toInt()} μT")
                InfoItem("精度", when (compassData.accuracy) {
                    3 -> "高"
                    2 -> "中"
                    1 -> "低"
                    else -> "未知"
                })
            }
            
            if (locationInfo.latitude != 0.0 && locationInfo.longitude != 0.0) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoItem("纬度", "%.4f°".format(locationInfo.latitude))
                    InfoItem("经度", "%.4f°".format(locationInfo.longitude))
                }
            }
        }
    }
}

/**
 * 信息项组件
 */
@Composable
fun InfoItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 校准提示组件
 */
@Composable
fun CalibrationHint() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFA500).copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFFA500),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "需要校准",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFA500)
                )
                Text(
                    text = "请以8字形移动设备进行校准",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
