package com.example.com.videoplayer;

/**
 * 视屏播放器管理类
 * Created by rhm on 2018/1/25.
 */

public class NiceVideoPlayerManager {
    private static NiceVideoPlayerManager sInstance;
    private NiceVideoPlayer mVideoPlayer;

    private NiceVideoPlayerManager() {
    }

    //单例模式
    public static NiceVideoPlayerManager instance() {
        if (sInstance == null) {
            synchronized (NiceVideoPlayerManager.class) {
                if (sInstance == null) {
                    sInstance = new NiceVideoPlayerManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取当前的NiceVideoPlayer
     *
     * @return NiceVideoPlayer
     */
    public NiceVideoPlayer getCurrentNiceVideoPlayer() {
        return mVideoPlayer;
    }

    /**
     * 设置当前的NiceVideoPlayer
     */
    public void setCurrentNiceVideoPlayer(NiceVideoPlayer niceVideoPlayer) {
        if (mVideoPlayer != niceVideoPlayer) {
            releaseNiceVideoPlayer();
            mVideoPlayer = niceVideoPlayer;
        }
    }

    /**
     * 暂停
     */
    public void suspendNiceVideoPlayer() {
        if (mVideoPlayer != null && (mVideoPlayer.isPlaying() || mVideoPlayer.isBufferingPlaying())) {
            mVideoPlayer.pause();
        }
    }

    /**
     * 恢复
     */
    public void resumeNiceVideoPlayer(){
        if (mVideoPlayer != null && (mVideoPlayer.isPaused() || mVideoPlayer.isBufferingPaused())) {
            mVideoPlayer.restart();
        }
    }


    /**
     * 释放NiceVideoPlayer
     */
    public void releaseNiceVideoPlayer() {
        if (mVideoPlayer != null) {
            mVideoPlayer.release();
            mVideoPlayer = null;
        }
    }

    /**
     * 按返回键时
     */
    public boolean onBackPressd() {
        if (mVideoPlayer != null) {
            if (mVideoPlayer.isFullScreen()) {
                return mVideoPlayer.exitFullScreen();
            } else if (mVideoPlayer.isTinyWindow()) {
                return mVideoPlayer.exitTinyWindow();
            }
        }
        return false;
    }
}
