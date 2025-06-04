package com.example.weixin.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;

public class AnimationUtils {

    /**
     * 卡片淡入动画
     * @param view 需要动画的视图
     * @param duration 动画持续时间（毫秒）
     * @param startDelay 开始延时（毫秒）
     */
    public static void fadeIn(final View view, long duration, long startDelay) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setStartDelay(startDelay)
                .setListener(null);
    }

    /**
     * 卡片淡出动画
     * @param view 需要动画的视图
     * @param duration 动画持续时间（毫秒）
     */
    public static void fadeOut(final View view, long duration) {
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * 从底部滑入动画
     * @param view 需要动画的视图
     * @param duration 动画持续时间（毫秒）
     */
    public static void slideInFromBottom(View view, long duration) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,
                0,
                view.getHeight(),
                0);
        animate.setDuration(duration);
        animate.setInterpolator(new AccelerateDecelerateInterpolator());
        view.startAnimation(animate);
    }

    /**
     * 卡片展开动画
     * @param view 需要展开的视图
     * @param targetHeight 展开后的目标高度
     * @param duration 动画持续时间（毫秒）
     */
    public static void expand(final View view, final int targetHeight, long duration) {
        view.setVisibility(View.VISIBLE);
        final int startHeight = view.getMeasuredHeight();
        
        // 设置视图高度从0到目标高度的动画
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startHeight, targetHeight);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = (int) animation.getAnimatedValue();
                view.setLayoutParams(layoutParams);
            }
        });
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    /**
     * 卡片收起动画
     * @param view 需要收起的视图
     * @param duration 动画持续时间（毫秒）
     */
    public static void collapse(final View view, long duration) {
        final int initialHeight = view.getMeasuredHeight();
        
        ValueAnimator valueAnimator = ValueAnimator.ofInt(initialHeight, 0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = (int) animation.getAnimatedValue();
                view.setLayoutParams(layoutParams);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    /**
     * 应用脉动动画效果
     * @param view 需要动画的视图
     */
    public static void applyPulseAnimation(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.1f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.1f, 1.0f);
        
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);
        
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        
        scaleX.setInterpolator(new LinearInterpolator());
        scaleY.setInterpolator(new LinearInterpolator());
        
        scaleX.start();
        scaleY.start();
    }

    /**
     * 天气图标旋转动画
     * @param view 需要旋转的视图
     * @param duration 动画持续时间（毫秒）
     */
    public static void rotateWeatherIcon(View view, long duration) {
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(duration);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        view.startAnimation(rotateAnimation);
    }

    /**
     * 震动动画，适用于错误提示
     * @param view 需要震动的视图
     */
    public static void shakeView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX",
                0, -10, 10, -10, 10, -5, 5, -5, 5, 0);
        animator.setDuration(600);
        animator.start();
    }
} 