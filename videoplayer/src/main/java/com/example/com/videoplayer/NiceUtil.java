package com.example.com.videoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;

import java.util.Formatter;
import java.util.Locale;

/**
 * 工具类
 * Created by rhm on 2018/1/24.
 */

public class NiceUtil {

    /**
     * 显示状态栏
     */
    public static void showActionBar(Context context) {
        if (getAppCompatActivity(context) != null) {
            ActionBar actionBar = getAppCompatActivity(context).getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
        //清除全屏
        if (scanForActivity(context) != null) {
            scanForActivity(context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * 隐藏状态栏
     */
    public static void hideActionBar(Context context) {
        if (getAppCompatActivity(context) != null) {
            ActionBar actionBar = getAppCompatActivity(context).getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }
        if (scanForActivity(context) != null) {
            //设置全屏
            scanForActivity(context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * 将Context转换为Activity
     */
    public static Activity scanForActivity(Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext()); //递归子类寻找是能转换为Activity
        }
        return null;
    }

    /**
     * 得到AppCompactActivity
     *
     * @param context 上下文
     * @return
     */
    public static AppCompatActivity getAppCompatActivity(Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof AppCompatActivity) {
            return (AppCompatActivity) context;
        } else if (context instanceof ContextThemeWrapper) {
            return getAppCompatActivity(((ContextThemeWrapper) context).getBaseContext());
        }
        return null;
    }

    /**
     * 将毫秒数格式化为"##:##"的时间
     *
     * @param milliseconds 毫秒数
     * @return ##:##
     */
    public static String formatTime(long milliseconds) {
        if (milliseconds <= 0 || milliseconds >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        long totalSeconds = milliseconds / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    //获取屏幕宽度
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    //获取屏幕的高度
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    //将dp转化为px
    //value * metrics.density;
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.getResources().getDisplayMetrics());
    }


}
