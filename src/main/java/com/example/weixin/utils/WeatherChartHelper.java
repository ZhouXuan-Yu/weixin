package com.example.weixin.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 简单的天气图表绘制助手类
 * 用于在无法加载MPAndroidChart库时提供基本图表功能
 */
public class WeatherChartHelper extends View {
    private static final int DEFAULT_LINE_COLOR = Color.BLUE;
    private static final int DEFAULT_DOT_COLOR = Color.RED;
    private static final int DEFAULT_GRID_COLOR = Color.LTGRAY;
    private static final int DEFAULT_TEXT_COLOR = Color.DKGRAY;
    private static final float DEFAULT_LINE_WIDTH = 2.0f;
    private static final float DEFAULT_DOT_RADIUS = 4.0f;
    private static final int DEFAULT_PADDING = 50;

    private List<Float> dataPoints = new ArrayList<>();
    private List<String> xLabels = new ArrayList<>();
    private float maxValue = 100f;
    private float minValue = 0f;
    private int lineColor = DEFAULT_LINE_COLOR;
    private int dotColor = DEFAULT_DOT_COLOR;
    private int gridColor = DEFAULT_GRID_COLOR;
    private int textColor = DEFAULT_TEXT_COLOR;
    private float lineWidth = DEFAULT_LINE_WIDTH;
    private float dotRadius = DEFAULT_DOT_RADIUS;
    private String chartTitle = "";
    private String yAxisLabel = "";
    private boolean showGrid = true;
    private boolean showDots = true;
    private int chartType = 0; // 0=线图, 1=柱状图

    private Paint linePaint;
    private Paint dotPaint;
    private Paint textPaint;
    private Paint gridPaint;
    private Paint barPaint;

    public WeatherChartHelper(Context context) {
        super(context);
        init();
    }

    public WeatherChartHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WeatherChartHelper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(lineColor);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(lineWidth);
        linePaint.setAntiAlias(true);

        dotPaint = new Paint();
        dotPaint.setColor(dotColor);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(30);
        textPaint.setAntiAlias(true);

        gridPaint = new Paint();
        gridPaint.setColor(gridColor);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);
        
        barPaint = new Paint();
        barPaint.setAntiAlias(true);
        barPaint.setColor(Color.GREEN);
    }

    public void setData(List<Float> data, List<String> labels) {
        if (data == null || data.isEmpty()) {
            return;
        }
        
        this.dataPoints = new ArrayList<>(data);
        
        if (labels != null && !labels.isEmpty()) {
            this.xLabels = new ArrayList<>(labels);
        } else {
            this.xLabels.clear();
            for (int i = 0; i < data.size(); i++) {
                this.xLabels.add(String.valueOf(i));
            }
        }
        
        // 计算最大最小值
        maxValue = Float.MIN_VALUE;
        minValue = Float.MAX_VALUE;
        for (Float point : data) {
            if (point > maxValue) maxValue = point;
            if (point < minValue) minValue = point;
        }
        
        // 为了图表美观，增加一点余量
        float range = maxValue - minValue;
        maxValue += range * 0.1f;
        minValue -= range * 0.1f;
        if (minValue < 0 && range > 0) minValue = 0;
        
        invalidate();
    }

    public void setChartType(int type) {
        this.chartType = type;
        invalidate();
    }

    public void setLineColor(int color) {
        this.lineColor = color;
        linePaint.setColor(color);
        invalidate();
    }

    public void setTitle(String title) {
        this.chartTitle = title;
        invalidate();
    }

    public void setYAxisLabel(String label) {
        this.yAxisLabel = label;
        invalidate();
    }

    public void setBarColor(int color) {
        barPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (dataPoints.isEmpty()) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        
        // 绘制标题
        if (chartTitle != null && !chartTitle.isEmpty()) {
            canvas.drawText(chartTitle, width / 2 - textPaint.measureText(chartTitle) / 2, 40, textPaint);
        }

        // 留出边距
        int paddingLeft = DEFAULT_PADDING + (yAxisLabel.isEmpty() ? 0 : 30);
        int paddingRight = DEFAULT_PADDING;
        int paddingTop = DEFAULT_PADDING + (chartTitle.isEmpty() ? 0 : 30);
        int paddingBottom = DEFAULT_PADDING + 30; // 为X轴标签留空间

        // 绘制网格和坐标轴
        if (showGrid) {
            drawGridAndAxes(canvas, width, height, paddingLeft, paddingRight, paddingTop, paddingBottom);
        }
        
        // 绘制Y轴标签
        if (!yAxisLabel.isEmpty()) {
            drawVerticalText(canvas, yAxisLabel, paddingLeft - 40, height / 2, textPaint);
        }
        
        // 根据图表类型绘制数据
        if (chartType == 0) {
            drawLineChart(canvas, width, height, paddingLeft, paddingRight, paddingTop, paddingBottom);
        } else {
            drawBarChart(canvas, width, height, paddingLeft, paddingRight, paddingTop, paddingBottom);
        }
        
        // 绘制X轴标签
        drawXLabels(canvas, width, height, paddingLeft, paddingRight, paddingBottom);
    }
    
    private void drawGridAndAxes(Canvas canvas, int width, int height, int paddingLeft, int paddingRight, int paddingTop, int paddingBottom) {
        // 绘制X轴
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, gridPaint);
        
        // 绘制Y轴
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom, gridPaint);
        
        // 绘制水平网格线
        int gridCount = 5;
        float yStep = (height - paddingTop - paddingBottom) / (float)gridCount;
        for (int i = 0; i <= gridCount; i++) {
            float y = height - paddingBottom - i * yStep;
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint);
            
            // 绘制Y轴刻度值
            float value = minValue + (maxValue - minValue) * i / gridCount;
            String valueStr = String.format("%.1f", value);
            canvas.drawText(valueStr, paddingLeft - textPaint.measureText(valueStr) - 5, y + 10, textPaint);
        }
    }
    
    private void drawLineChart(Canvas canvas, int width, int height, int paddingLeft, int paddingRight, int paddingTop, int paddingBottom) {
        if (dataPoints.size() <= 1) {
            return;
        }
        
        float graphWidth = width - paddingLeft - paddingRight;
        float graphHeight = height - paddingTop - paddingBottom;
        float xStep = graphWidth / (dataPoints.size() - 1);
        
        // 创建路径
        Path path = new Path();
        boolean isFirstPoint = true;
        
        for (int i = 0; i < dataPoints.size(); i++) {
            float value = dataPoints.get(i);
            float x = paddingLeft + i * xStep;
            float y = height - paddingBottom - graphHeight * (value - minValue) / (maxValue - minValue);
            
            if (isFirstPoint) {
                path.moveTo(x, y);
                isFirstPoint = false;
            } else {
                path.lineTo(x, y);
            }
            
            // 绘制点
            if (showDots) {
                canvas.drawCircle(x, y, dotRadius, dotPaint);
            }
        }
        
        // 绘制线
        canvas.drawPath(path, linePaint);
    }
    
    private void drawBarChart(Canvas canvas, int width, int height, int paddingLeft, int paddingRight, int paddingTop, int paddingBottom) {
        if (dataPoints.isEmpty()) {
            return;
        }
        
        float graphWidth = width - paddingLeft - paddingRight;
        float graphHeight = height - paddingTop - paddingBottom;
        float barWidth = graphWidth / dataPoints.size() * 0.7f;
        float spacing = graphWidth / dataPoints.size() * 0.3f;
        
        for (int i = 0; i < dataPoints.size(); i++) {
            float value = dataPoints.get(i);
            float barHeight = graphHeight * (value - minValue) / (maxValue - minValue);
            
            float left = paddingLeft + i * (barWidth + spacing) + spacing/2;
            float top = height - paddingBottom - barHeight;
            float right = left + barWidth;
            float bottom = height - paddingBottom;
            
            canvas.drawRect(left, top, right, bottom, barPaint);
        }
    }
    
    private void drawXLabels(Canvas canvas, int width, int height, int paddingLeft, int paddingRight, int paddingBottom) {
        if (xLabels.isEmpty()) {
            return;
        }
        
        float graphWidth = width - paddingLeft - paddingRight;
        
        if (chartType == 0) {
            // 线图的X轴标签位置
            float xStep = graphWidth / (xLabels.size() - 1);
            for (int i = 0; i < xLabels.size(); i++) {
                String label = xLabels.get(i);
                float x = paddingLeft + i * xStep;
                
                float textWidth = textPaint.measureText(label);
                canvas.drawText(label, x - textWidth / 2, height - paddingBottom + 30, textPaint);
            }
        } else {
            // 柱状图的X轴标签位置
            float barWidth = graphWidth / xLabels.size() * 0.7f;
            float spacing = graphWidth / xLabels.size() * 0.3f;
            
            for (int i = 0; i < xLabels.size(); i++) {
                String label = xLabels.get(i);
                float x = paddingLeft + i * (barWidth + spacing) + barWidth/2 + spacing/2;
                
                float textWidth = textPaint.measureText(label);
                canvas.drawText(label, x - textWidth / 2, height - paddingBottom + 30, textPaint);
            }
        }
    }
    
    private void drawVerticalText(Canvas canvas, String text, float x, float y, Paint paint) {
        canvas.save();
        canvas.rotate(-90, x, y);
        canvas.drawText(text, x - paint.measureText(text) / 2, y, paint);
        canvas.restore();
    }
} 