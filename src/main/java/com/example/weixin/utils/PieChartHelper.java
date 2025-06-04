package com.example.weixin.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 简单的饼图绘制助手类
 * 用于替代MPAndroidChart库的PieChart功能
 */
public class PieChartHelper extends View {
    private static final int[] DEFAULT_COLORS = {
            Color.rgb(64, 89, 128), Color.rgb(149, 165, 124), 
            Color.rgb(217, 184, 162), Color.rgb(191, 134, 134), 
            Color.rgb(179, 48, 80), Color.rgb(192, 255, 140), 
            Color.rgb(255, 247, 140), Color.rgb(255, 208, 140),
            Color.rgb(140, 234, 255), Color.rgb(222, 187, 255)
    };
    
    private static final int DEFAULT_TEXT_COLOR = Color.BLACK;
    private static final int DEFAULT_CENTER_TEXT_COLOR = Color.DKGRAY;
    private static final float DEFAULT_TEXT_SIZE = 28f;
    private static final float DEFAULT_CENTER_TEXT_SIZE = 40f;
    
    private List<PieEntry> entries = new ArrayList<>();
    private Paint piePaint;
    private Paint textPaint;
    private Paint centerTextPaint;
    private Paint linePaint;
    private String centerText = "";
    private String title = "";
    private float holeRadius = 0.5f; // 中心孔洞半径比例
    private boolean drawLabels = true;
    private boolean drawValues = true;
    
    public static class PieEntry {
        private final float value;
        private final String label;
        
        public PieEntry(float value, String label) {
            this.value = value;
            this.label = label;
        }
        
        public float getValue() {
            return value;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    public PieChartHelper(Context context) {
        super(context);
        init();
    }

    public PieChartHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartHelper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        piePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(DEFAULT_TEXT_COLOR);
        textPaint.setTextSize(DEFAULT_TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        centerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerTextPaint.setColor(DEFAULT_CENTER_TEXT_COLOR);
        centerTextPaint.setTextSize(DEFAULT_CENTER_TEXT_SIZE);
        centerTextPaint.setTextAlign(Paint.Align.CENTER);
        
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.GRAY);
        linePaint.setStrokeWidth(2f);
        linePaint.setStyle(Paint.Style.STROKE);
    }
    
    public void setEntries(List<PieEntry> entries) {
        this.entries = new ArrayList<>(entries);
        invalidate();
    }
    
    public void setCenterText(String text) {
        this.centerText = text;
        invalidate();
    }
    
    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }
    
    public void setHoleRadius(float radius) {
        this.holeRadius = Math.max(0f, Math.min(0.9f, radius));
        invalidate();
    }
    
    public void setDrawLabels(boolean drawLabels) {
        this.drawLabels = drawLabels;
        invalidate();
    }
    
    public void setDrawValues(boolean drawValues) {
        this.drawValues = drawValues;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (entries.isEmpty()) {
            return;
        }
        
        // 计算总值
        float total = 0;
        for (PieEntry entry : entries) {
            total += entry.getValue();
        }
        
        int width = getWidth();
        int height = getHeight();
        
        // 绘制标题
        if (title != null && !title.isEmpty()) {
            canvas.drawText(title, width / 2, 50, centerTextPaint);
        }
        
        // 计算饼图绘制区域
        float titleSpace = title != null && !title.isEmpty() ? 80 : 0;
        float radius = Math.min(width, height - titleSpace) / 2f * 0.8f;
        float centerX = width / 2f;
        float centerY = (height + titleSpace) / 2f;
        
        RectF pieRect = new RectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius);
        
        // 绘制饼图
        float startAngle = 0;
        for (int i = 0; i < entries.size(); i++) {
            PieEntry entry = entries.get(i);
            float sweepAngle = 360f * entry.getValue() / total;
            
            piePaint.setColor(DEFAULT_COLORS[i % DEFAULT_COLORS.length]);
            canvas.drawArc(pieRect, startAngle, sweepAngle, true, piePaint);
            
            if (drawLabels || drawValues) {
                drawLabel(canvas, entry, startAngle, sweepAngle, centerX, centerY, radius);
            }
            
            startAngle += sweepAngle;
        }
        
        // 绘制中心孔
        if (holeRadius > 0) {
            piePaint.setColor(Color.WHITE);
            canvas.drawCircle(centerX, centerY, radius * holeRadius, piePaint);
            
            // 绘制中心文本
            if (centerText != null && !centerText.isEmpty()) {
                canvas.drawText(centerText, centerX, centerY + centerTextPaint.getTextSize() / 3, centerTextPaint);
            }
        }
    }
    
    private void drawLabel(Canvas canvas, PieEntry entry, float startAngle, float sweepAngle, float centerX, float centerY, float radius) {
        float angle = startAngle + sweepAngle / 2;
        float radians = (float) Math.toRadians(angle);
        
        // 在中心点和饼图边缘之间的点
        float labelRadius = radius * 0.7f;
        float x = (float) (centerX + Math.cos(radians) * labelRadius);
        float y = (float) (centerY + Math.sin(radians) * labelRadius);
        
        // 绘制标签
        StringBuilder labelText = new StringBuilder();
        if (drawLabels && entry.getLabel() != null) {
            labelText.append(entry.getLabel());
        }
        
        if (drawValues) {
            if (drawLabels && labelText.length() > 0) {
                labelText.append(": ");
            }
            labelText.append(String.format("%.1f%%", 100 * entry.getValue() / getTotal()));
        }
        
        if (labelText.length() > 0) {
            canvas.drawText(labelText.toString(), x, y, textPaint);
        }
    }
    
    private float getTotal() {
        float total = 0;
        for (PieEntry entry : entries) {
            total += entry.getValue();
        }
        return total;
    }
} 