package com.example.com.videoplayer;

import android.util.Log;

/**
 * 日志打印工具类
 * Created by rhm on 2018/1/23.
 */

public class LogUtils {

    public static boolean isLog = true;//是否需要打印日志
    public static String TAG = "NiceVideoPlayer";

    public static void setTAG(String tag) {
        TAG = tag;
    }

    public static void i(String msg) {
        if (isLog) {
            Log.i(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (isLog) {
            Log.d(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (isLog) {
            Log.e(TAG, msg);
        }
    }

    public static void v(String msg) {
        if (isLog) {
            Log.v(TAG, msg);
        }
    }
}
