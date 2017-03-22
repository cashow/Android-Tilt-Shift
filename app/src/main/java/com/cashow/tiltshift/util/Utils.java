package com.cashow.tiltshift.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

/**
 * 这个文件是用来存放常用的工具
 */
public class Utils {
    /**
     * 将dp转换成px值
     */
    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 如果a在[min, max]区间内，返回a，否则返回边界值
     */
    public static float clamp(float a, float min, float max) {
        if (a < min + 1e-8) {
            a = min;
        }
        if (a > max - 1e-8) {
            a = max;
        }
        return a;
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 计算2个触摸点之间的距离
     */
    public static double distanceBetweenFingers(MotionEvent event) {
        float disX = Math.abs(event.getX(0) - event.getX(1));
        float disY = Math.abs(event.getY(0) - event.getY(1));
        return Math.sqrt(disX * disX + disY * disY);
    }
}
