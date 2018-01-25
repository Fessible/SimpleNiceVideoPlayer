package com.example.com.videoplayer;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 音量，亮度，进度条控制界面抽象类
 * Created by rhm on 2018/1/23.
 */

public abstract class NiceVideoController
        extends FrameLayout implements View.OnTouchListener {
    private final static int THRESHOLD = 80;//定义最小滑动距离
    //每隔一段时间执行进度更新
    private Timer mUpdateProgressTimer;
    private TimerTask mUpdateProgressTimerTask;

    private INiceVideoPlayer mNiceVideoPlayer;
    private Context mContext;
    private float mDownX;
    private float mDownY;
    private boolean mNeedChangePosition = false;
    private boolean mNeedChangeVolume = false;
    private boolean mNeedChangeBrightness = false;

    private long mGestureDownPosition;
    private float mGestureDownBrightness;
    private int mGestureDownVolume;
    private long mNewPosition;


    protected NiceVideoController(@NonNull Context context) {
        super(context);
        mContext = context;
        this.setOnTouchListener(this);//添加触摸监听
    }

    /**
     * @param niceVideoPlayer 得到NiceVideoPlayer
     */
    protected void setNiceVideoPlayer(INiceVideoPlayer niceVideoPlayer) {
        mNiceVideoPlayer = niceVideoPlayer;
    }

    /**
     * 设置视频的标题
     */
    public abstract void setTitle(String title);

    /**
     * 视频底图
     */
    public abstract void setImage(@DrawableRes int resId);

    /**
     * 视频底图ImageView控件，提供给外部用图片加载工具来加载网络图片
     *
     * @return 底图ImageView
     */
    public abstract ImageView getImageView();

    /**
     * 设置总时长
     */
    public abstract void setLength(long length);

    /**
     * 重置控制器，将控制器恢复到初始状态
     */
    protected abstract void reset();

    /**
     * @param playState 播放状态
     */
    protected abstract void onPlayStateChanged(int playState);

    protected abstract void onPlayModeChanged(int playMode);

    /**
     * 开启进度更新计时器
     */
    protected void startUpdateProgressTimer() {
        cancelUpdateProgressTimer();
        if (mUpdateProgressTimer == null) {
            mUpdateProgressTimer = new Timer();
        }
        if (mUpdateProgressTimerTask == null) {
            mUpdateProgressTimerTask = new TimerTask() {
                @Override
                public void run() {
                    //主线程中更新
                    NiceVideoController.this.post(new Runnable() {
                        @Override
                        public void run() {
                            updateProgress();
                        }
                    });
                }
            };
        }
        mUpdateProgressTimer.schedule(mUpdateProgressTimerTask, 0, 1000);
    }

    /**
     * 取消更新进度的计时器
     */
    protected void cancelUpdateProgressTimer() {
        if (mUpdateProgressTimer != null) {
            mUpdateProgressTimer.cancel();
            mUpdateProgressTimer = null;
        }

        if (mUpdateProgressTimerTask != null) {
            mUpdateProgressTimerTask.cancel();
            mUpdateProgressTimerTask = null;
        }
    }

    /**
     * 控制进度条，声音，亮度
     *
     * @param event 触摸事件
     */

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //只在全屏时可拖动位置、亮度和声音
        if (!mNiceVideoPlayer.isFullScreen()) {
            return false;
        }
        // 只有在播放、暂停、缓冲的时候能够拖动改变位置、亮度和声音
        if (mNiceVideoPlayer.isIdle()
                || mNiceVideoPlayer.isError()
                || mNiceVideoPlayer.isPreparing()
                || mNiceVideoPlayer.isPrepared()
                || mNiceVideoPlayer.isCompleted()) {
            hideChangePosition();
            hideChangeBrightness();
            hideChangeVolume();
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://按下时记录位置
                mDownX = x;
                mDownY = y;
                mNeedChangePosition = false;
                mNeedChangeBrightness = false;
                mNeedChangeVolume = false;
                break;
            case MotionEvent.ACTION_MOVE://移动
                float deltaX = x - mDownX;
                float deltaY = y - mDownY;
                float absDeltaX = Math.abs(deltaX);
                float absDeltaY = Math.abs(deltaY);
//                只有在播放、暂停、缓冲的时候能够拖动改变位置、亮度和声音
                if (!mNeedChangePosition && !mNeedChangeVolume && !mNeedChangeBrightness) {
                    // 只有在播放、暂停、缓冲的时候能够拖动改变位置、亮度和声音
                    if (absDeltaX >= THRESHOLD) {
                        cancelUpdateProgressTimer();
                        mNeedChangePosition = true;
                        mGestureDownPosition = mNiceVideoPlayer.getCurrentPosition();
                    } else if (absDeltaY >= THRESHOLD) {
                        if (mDownX < getWidth() * 0.5f) {
                            // 左侧改变亮度
                            mNeedChangeBrightness = true;
                            if (NiceUtil.scanForActivity(mContext) != null) {
                                mGestureDownBrightness = NiceUtil.scanForActivity(mContext)
                                        .getWindow().getAttributes().screenBrightness;
                            }
                        } else {
                            // 右侧改变声音
                            mNeedChangeVolume = true;
                            mGestureDownVolume = mNiceVideoPlayer.getVolume();
                        }
                    }
                }

                if (mNeedChangePosition) {
                    long duration = mNiceVideoPlayer.getDuration();
                    //当前距离+屏幕滑动距离
                    long toPosition = (long) (mGestureDownPosition + duration * deltaX / getWidth());
                    mNewPosition = Math.max(0, Math.min(duration, toPosition));
                    int newPositionProgress = (int) (100f * mNewPosition / duration);
                    showChangePosition(duration, newPositionProgress);
                }
                //亮度 0-1
                if (mNeedChangeBrightness) {
                    deltaY = -deltaY;//由于是向下滑时，最终距离比开始距离大，为正数
                    float deltaBrightness = 3 * deltaY / getHeight();//滑动的距离除以屏幕高度的比例，可以乘以一定的倍率，来加大滑动效果。
                    float newBrightness = mGestureDownBrightness + deltaBrightness;
                    newBrightness = Math.max(0, Math.min(newBrightness, 1));
                    Activity activity = NiceUtil.scanForActivity(mContext);
                    if (activity != null) {
                        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
                        params.screenBrightness = newBrightness;
                        //设置当前窗口亮度值
                        activity.getWindow().setAttributes(params);
                        int newBrightnessProgress = (int) (newBrightness * 100f);
                        showChangeBrightness(newBrightnessProgress);
                    }
                }
                if (mNeedChangeVolume) {
                    deltaY = -deltaY;
                    int maxVolume = mNiceVideoPlayer.getMaxVolume();
                    int deltaVolume = (int) (deltaY * maxVolume * 3 / getHeight());
                    int newVolume = mGestureDownVolume + deltaVolume;
                    newVolume = Math.max(0, Math.min(maxVolume, newVolume));
                    //设置volume
                    mNiceVideoPlayer.setVolume(newVolume);
                    int newVolumeProgress = (int) (100f * newVolume / maxVolume);
                    showChangeVolume(newVolumeProgress);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mNeedChangePosition) {
                    mNiceVideoPlayer.seekTo(mNewPosition);
                    hideChangePosition();
                    startUpdateProgressTimer();
                    return true;
                }
                if (mNeedChangeVolume) {
                    hideChangeVolume();
                    return true;
                }
                if (mNeedChangeBrightness) {
                    hideChangeBrightness();
                    return true;
                }
        }
        return false;
    }

    /**
     * 更新进度，包括进度条，播放位置时长，总时长
     */

    protected abstract void updateProgress();


    /**
     * 隐藏控制条
     */
    protected abstract void hideChangePosition();

    /**
     * 隐藏亮度图标
     */
    protected abstract void hideChangeBrightness();

    /**
     * 隐藏音量图标
     */
    protected abstract void hideChangeVolume();

    /**
     * 手势滑动显示进度
     *
     * @param duration            播放总时长
     * @param newPositionProgress 新的位置
     */
    protected abstract void showChangePosition(long duration, int newPositionProgress);

    /**
     * 手势在左侧上下滑动改变亮度时，显示控制器中间的亮度变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     *
     * @param newBrightnessProgress 新的亮度进度，取值1到100。
     */
    protected abstract void showChangeBrightness(int newBrightnessProgress);

    /**
     * 手势在右侧上下滑动改变音量时，显示控制器中间的音量变化视图，
     * 在手势滑动ACTION_MOVE的过程中，会不断调用此方法。
     *
     * @param newVolumeProgress 新的音量进度，取值1到100。
     */
    protected abstract void showChangeVolume(int newVolumeProgress);
}
