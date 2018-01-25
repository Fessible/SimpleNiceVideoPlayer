package com.example.com.videoplayer;

import java.util.Map;

/**
 * Created by rhm on 2018/1/23.
 */

public interface INiceVideoPlayer {

    /**
     * 设置视频的url，以及headers
     *
     * @param url     视频地址，可以是本地，也可以是网络
     * @param headers 请求header
     */
    void setUp(String url, Map<String, String> headers);

    /***************
     * 播放控制
     ****************/

    /**
     * 开始播放
     */
    void start();

    /**
     * 从指定位置开始播放
     *
     * @param position 播放位置
     */
    void start(long position);

    /**
     * 重新播放，播放器被暂停，播放错误，播放完成后需要调用这个方法重新播放
     */
    void restart();

    /**
     * 暂停播放
     */
    void pause();

    /**
     * 释放videoPlayer
     */
    void releasePlayer();

    /**
     * 释放
     */
    void release();


    /**
     * seek到指定的位置继续播放
     *
     * @param pos
     */
    void seekTo(long pos);

    /**
     * 开始播放时，是否重上一次的位置继续播放
     *
     * @param continueFromLastPosition 上次播放位置
     */
    void continueFromLastPostion(boolean continueFromLastPosition);

    /**
     * 获取当前的播放位置
     */
    long getCurrentPosition();

    /**
     * 获取视频缓冲百分比
     */
    int getBufferPercentage();

    /**
     * 设置音量
     *
     * @param volume 音量值
     */
    void setVolume(int volume);

    /**
     * 获取最大音量
     *
     * @return
     */
    int getMaxVolume();

    /**
     * 获取当前的音量
     *
     * @return
     */
    int getVolume();

    /**
     * 获取总时长
     *
     * @return
     */
    long getDuration();


    /******************************
     * 播放器当前的播放状态
     ******************************/
    boolean isIdle();//是否空闲

    boolean isPreparing();

    boolean isPrepared();

    boolean isBufferingPlaying();

    boolean isBufferingPaused();

    boolean isPlaying();

    boolean isPaused();

    boolean isError();

    boolean isCompleted();


    /*************************
     * 播放器模式
     *************************/
    boolean isFullScreen();

    boolean isTinyWindow();

    boolean isNormal();

    /**
     * 进入全屏模式
     */
    void enterFullScreen();

    /**
     * 退出全屏模式
     *
     * @return
     */
    boolean exitFullScreen();

    /**
     * 进入小窗口模式
     */
    void enterTinyWindow();

    /**
     * 退出小窗口模式
     *
     * @return
     */
    boolean exitTinyWindow();
}
