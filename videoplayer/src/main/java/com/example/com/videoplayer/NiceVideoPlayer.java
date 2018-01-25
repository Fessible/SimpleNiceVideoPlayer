package com.example.com.videoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.Map;

import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;
import static android.media.MediaPlayer.MEDIA_INFO_NOT_SEEKABLE;
import static android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;

/**
 * 自定义播放器
 * Created by rhm on 2018/1/23.
 */

public class NiceVideoPlayer extends FrameLayout implements INiceVideoPlayer, TextureView.SurfaceTextureListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener {
    /****************
     * 播放状态
     ****************/
    /**
     * 播放错误
     */
    public static final int STATE_ERROR = -1;
    /**
     * 播放未开始
     */
    public static final int STATE_IDLE = 0;
    /**
     * 播放准备中
     */
    public static final int STATE_PREPARING = 1;
    /**
     * 播放准备就绪
     */
    public static final int STATE_PREPARED = 2;
    /**
     * 正在播放
     */
    public static final int STATE_PLAYING = 3;
    /**
     * 暂停播放
     */
    public static final int STATE_PAUSED = 4;
    /**
     * 正在缓冲,播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复
     */
    public static final int STATE_BUFFERING_PLAYING = 5;
    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，此时暂停播放器，继续缓冲，缓冲区数据足够后恢复暂停
     **/
    public static final int STATE_BUFFERING_PAUSED = 6;
    /**
     * 播放完成
     */
    public static final int STATE_COMPLETED = 7;
    /*************
     * 播放模式
     *************/
    /**
     * 普通模式
     */
    public static final int MODE_NORMAL = 10;
    /**
     * 全屏模式
     */
    public static final int MODE_FULL_SCREEN = 11;
    /**
     * 小窗口模式
     */
    public static final int MODE_TINY_WINDOW = 12;

    private Context mContext;
    private NiceVideoController mController;
    private FrameLayout mContainer;//设置布局容器
    private SurfaceTexture mSurfaceTexture;
    private MediaPlayer mMediaplayer;
    private Surface mSurface;
    private AudioManager mAudioManager;
    private TextureView mTextureView;
    //当前状态
    private int mCurrentState = STATE_IDLE;
    private int mCurrentMode = MODE_NORMAL;
    //网络链接
    private String mUrl;
    private Map<String, String> mHeaders;
    //指定位置
    private long skipToPosition;
    private int mBufferPercentage;

    public NiceVideoPlayer(@NonNull Context context) {
        super(context);
        mContext = context;
        init();
    }

    public NiceVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        mContainer = new FrameLayout(mContext);//创建容器布局
        mContainer.setBackgroundColor(Color.BLACK);//设置背景色为黑色
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);//设置铺满全屏
        this.addView(mContainer, params);
    }

    @Override
    public void setUp(String url, Map<String, String> headers) {
        mUrl = url;
        mHeaders = headers;
    }

    //设置控制音量，亮度，控制条等界面
    public void setController(NiceVideoController controller) {
        //清除
        mContainer.removeView(mController);
        mController = controller;
        mController.reset();//重置

        mController.setNiceVideoPlayer(this);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //添加控制布局
        mContainer.addView(mController, params);
    }

    @Override
    public void start() {
        NiceVideoPlayerManager.instance().setCurrentNiceVideoPlayer(this);

        //只有IDLE状态下才能开始
        if (mCurrentState == STATE_IDLE) {
            //初始化
            initAudioManager();
            initMediaPlayer();
            initTextureView();
        } else {
            LogUtils.d("NiceVideoPlayer只有在mCurrentState==STATE_IDLE时才能调用");
        }
    }

    @Override
    public void start(long position) {
        skipToPosition = position;
        start();
    }

    /**
     * 初始化音频管理器
     */
    private void initAudioManager() {
        if (mAudioManager == null)
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        //获得音频焦点
        mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    /**
     * 初始化MediaPlayer
     */
    private void initMediaPlayer() {
        if (mMediaplayer == null) {
            mMediaplayer = new MediaPlayer();
            mMediaplayer.reset();
            mMediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    /**
     * 初始化TextureView
     */
    private void initTextureView() {
        if (mTextureView == null) {
            mTextureView = new TextureView(mContext);
            mTextureView.setSurfaceTextureListener(this);
        }
        addTextureView();//将TextureView添加到Container中
    }

    /**
     * 将TextureView添加到Container中
     */
    private void addTextureView() {

        mContainer.removeView(mTextureView);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(mTextureView, 0, params);
    }

    @Override
    public void restart() {
        //当前状态为暂停
        if (mCurrentState == STATE_PAUSED) {
            mMediaplayer.start();
            mCurrentState = STATE_PLAYING;
            mController.onPlayStateChanged(mCurrentState);
            LogUtils.d("STATE_PLAYING");
        } else if (mCurrentState == STATE_BUFFERING_PAUSED) {
            mMediaplayer.start();
            mCurrentState = STATE_BUFFERING_PLAYING;
            mController.onPlayStateChanged(mCurrentState);
            LogUtils.d("STATE_BUFFERING_PLAYING");
//播放完成或者错误
        } else if (mCurrentState == STATE_COMPLETED || mCurrentState == STATE_ERROR) {
            mMediaplayer.reset();
            openMediaPlayer();//重新播放
        }
    }

    /**
     * 添加相关监听，进入准备
     */
    private void openMediaPlayer() {

        try {
            mContainer.setKeepScreenOn(true);//设置屏幕常亮

            //设置数据
            mMediaplayer.setDataSource(mContext.getApplicationContext(), Uri.parse(mUrl), mHeaders);
            if (mSurface == null) {
                mSurface = new Surface(mSurfaceTexture);
            }
            //设置渲染
            mMediaplayer
                    .setSurface(mSurface);
            mMediaplayer.prepareAsync();//准备
            mCurrentState = STATE_PREPARING;
            mController.onPlayStateChanged(mCurrentState);
            LogUtils.d("STATE_PREPARING");
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.d("打开播放器发生错误");
        }

        //设置监听
        mMediaplayer.setOnPreparedListener(this);
        mMediaplayer.setOnVideoSizeChangedListener(this);
        mMediaplayer.setOnCompletionListener(this);
        mMediaplayer.setOnErrorListener(this);
        mMediaplayer.setOnInfoListener(this);
        mMediaplayer.setOnBufferingUpdateListener(this);
    }

    @Override
    public void pause() {
        if (mCurrentState == STATE_PLAYING) {
            mMediaplayer.pause();
            mCurrentState = STATE_PAUSED;
            mController.onPlayStateChanged(mCurrentState);
            LogUtils.d("STATE_PAUSED");
        }
        //缓冲暂停
        if (mCurrentState == STATE_BUFFERING_PLAYING) {
            mMediaplayer.pause();
            mCurrentState = STATE_BUFFERING_PAUSED;
            mController.onPlayStateChanged(mCurrentState);
            LogUtils.d("STATE_BUFFERING_PAUSED");
        }
    }

    @Override
    public void releasePlayer() {
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(null);//释放焦点
            mAudioManager = null;
        }
        if (mMediaplayer != null) {
            mMediaplayer.release();
            mMediaplayer = null;
        }

        mContainer.removeView(mTextureView);
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        mCurrentState = STATE_IDLE;
    }

    //释放
    @Override
    public void release() {
        //保存播放位置

        //退出全屏
        if (isFullScreen()) {
            exitFullScreen();
        }
        //退出小窗口
        if (isTinyWindow()) {
            exitTinyWindow();
        }
        mCurrentMode = MODE_NORMAL;
        //释放播放器
        releasePlayer();
        //释放控制器
        if (mController != null) {
            mController.reset();
        }
        Runtime.getRuntime().gc();//回收
    }

    @Override
    public void seekTo(long pos) {
        if (mMediaplayer != null) {
            mMediaplayer.seekTo((int) pos);
        }
    }

    @Override
    public void continueFromLastPostion(boolean continueFromLastPosition) {

    }

    @Override
    public long getCurrentPosition() {
        return mMediaplayer != null ? mMediaplayer.getCurrentPosition() : 0;
    }

    @Override
    public int getBufferPercentage() {
        return mBufferPercentage;
    }

    @Override
    public void setVolume(int volume) {
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        }
    }

    @Override
    public int getMaxVolume() {
        return mAudioManager != null ? mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) : 0;
    }

    @Override
    public int getVolume() {
        return mAudioManager != null ? mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) : 0;
    }

    @Override
    public long getDuration() {
        return mMediaplayer != null ? mMediaplayer.getDuration() : 0;
    }

    @Override
    public boolean isIdle() {
        return mCurrentState == STATE_IDLE;
    }

    @Override
    public boolean isPreparing() {
        return mCurrentState == STATE_PREPARING;
    }

    @Override
    public boolean isPrepared() {
        return mCurrentState == STATE_PREPARED;
    }

    @Override
    public boolean isBufferingPlaying() {
        return mCurrentState == STATE_BUFFERING_PLAYING;
    }

    @Override
    public boolean isBufferingPaused() {
        return mCurrentState == STATE_BUFFERING_PAUSED;
    }

    @Override
    public boolean isPlaying() {
        return mCurrentState == STATE_PLAYING;
    }

    @Override
    public boolean isPaused() {
        return mCurrentState == STATE_PAUSED;
    }

    @Override
    public boolean isError() {
        return mCurrentState == STATE_ERROR;
    }

    @Override
    public boolean isCompleted() {
        return mCurrentState == STATE_COMPLETED;
    }

    @Override
    public boolean isFullScreen() {
        return mCurrentMode == MODE_FULL_SCREEN;
    }

    @Override
    public boolean isTinyWindow() {
        return mCurrentMode == MODE_TINY_WINDOW;
    }

    @Override
    public boolean isNormal() {
        return mCurrentMode == MODE_NORMAL;
    }

    //全屏模式
    @Override
    public void enterFullScreen() {
        if (mCurrentMode == MODE_FULL_SCREEN) return;
        //隐藏状态栏
        NiceUtil.hideActionBar(mContext);
        //context转换为Activity
        Activity activity = NiceUtil.scanForActivity(mContext);
        if (activity != null) {//转换为横屏
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            ViewGroup contentView = activity.findViewById(android.R.id.content);
            //移除布局
            if (mCurrentMode == MODE_TINY_WINDOW) {
                contentView.removeView(mContainer);
            } else {
                this.removeView(mContainer);
            }
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            //将布局添加到ContentView
            contentView.addView(mContainer, params);
            mCurrentMode = MODE_FULL_SCREEN;
            mController.onPlayModeChanged(mCurrentMode);
            LogUtils.d("MODE_FULL_SCREEN");
        }
    }

    //退出全屏
    @Override
    public boolean exitFullScreen() {
        if (mCurrentMode == MODE_FULL_SCREEN) {
            //显示状态栏
            NiceUtil.showActionBar(mContext);
            Activity activity = NiceUtil.scanForActivity(mContext);
            if (activity != null) {
                //切换竖屏
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                ViewGroup contentView = activity.findViewById(android.R.id.content);
                contentView.removeView(mContainer);
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                this.addView(mContainer, params);
                mCurrentMode = MODE_NORMAL;
                mController.onPlayModeChanged(mCurrentMode);
                LogUtils.d("MODE_NORMAL");
                return true;
            }
        }
        return false;
    }

    //进入小窗口
    @Override
    public void enterTinyWindow() {
        if (mCurrentMode == MODE_TINY_WINDOW) return;
        this.removeView(mContainer);
        Activity activity = NiceUtil.scanForActivity(mContext);
        if (activity != null) {
            ViewGroup contentView = activity.findViewById(android.R.id.content);
            //设置小窗口的布局大小,宽度为屏幕宽度的60%，长宽比默认为16:9，右边距、下边距为8dp。
            //height = 9/16 * 0.6width
            LayoutParams params = new LayoutParams((int) (NiceUtil.getScreenWidth(mContext) * 0.6f), (int) (NiceUtil.getScreenWidth(mContext) * 0.6f * 9f / 16f));
            //设置位置
            params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            //设置边距
            params.rightMargin = NiceUtil.dp2px(mContext, 8f);
            params.bottomMargin = NiceUtil.dp2px(mContext, 8f);
            contentView.addView(mContainer, params);
            mCurrentMode = MODE_TINY_WINDOW;
            mController.onPlayModeChanged(mCurrentMode);
            LogUtils.d("MODE_TINY_WINDOW");
        }
    }

    //退出小窗口
    @Override
    public boolean exitTinyWindow() {
        if (mCurrentMode == MODE_TINY_WINDOW) {
            Activity activity = NiceUtil.scanForActivity(mContext);
            if (activity != null) {
                ViewGroup contentView = activity.findViewById(android.R.id.content);
                contentView.removeView(mContainer);
                LayoutParams params = (LayoutParams) new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                this.addView(mContainer, params);
                mCurrentMode = MODE_NORMAL;
                mController.onPlayModeChanged(mCurrentMode);
                LogUtils.d("MODE_NORMAL");
                return true;
            }
        }
        return false;
    }

    /*********************
     * SurfaceTexture监听
     *********************/

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surface;
            openMediaPlayer();
        } else {
            mTextureView.setSurfaceTexture(mSurfaceTexture);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mCurrentState = STATE_PREPARED;
        mController.onPlayStateChanged(mCurrentState);
        LogUtils.d("STATE_PREPARED");
        mp.start();//开始播放
        //跳转到指定位置
        if (skipToPosition != 0) {
            mp.seekTo((int) skipToPosition);
        }
    }

    //大小变化
    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mBufferPercentage = percent;
    }

    //播放完成
    @Override
    public void onCompletion(MediaPlayer mp) {
        mCurrentState = STATE_COMPLETED;
        mController.onPlayStateChanged(mCurrentState);
        LogUtils.d("onCompletion ——> STATE_COMPLETED");
        //清除屏幕常亮
        mContainer.setKeepScreenOn(false);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // 直播流播放时去调用mediaPlayer.getDuration会导致-38和-2147483648错误，忽略该错误
        if (what != -38 && what != -2147483648 && extra != -38 && extra != -2147483648) {
            mCurrentState = STATE_ERROR;
            mController.onPlayStateChanged(mCurrentState);
            LogUtils.d("onError ——> STATE_ERROR ———— what：" + what + ", extra: " + extra);
        }
        return true;
    }

    //监听信息的变化
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == MEDIA_INFO_VIDEO_RENDERING_START) {
            //播放器开始渲染
            mCurrentState = STATE_PLAYING;
            mController.onPlayStateChanged(mCurrentState);
            LogUtils.d("onInfo ——> MEDIA_INFO_VIDEO_RENDERING_START：STATE_PLAYING");
        } else if (MEDIA_INFO_BUFFERING_START == what) {
            //mediaplayer暂时不播放，来缓冲更多数据
            if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED) {
                mCurrentState = STATE_BUFFERING_PAUSED;
                LogUtils.d("onInfo ——> MEDIA_INFO_BUFFERING_START：STATE_BUFFERING_PAUSED");
            } else {
                mCurrentState = STATE_BUFFERING_PLAYING;
                LogUtils.d("onInfo ——> MEDIA_INFO_BUFFERING_START：STATE_BUFFERING_PLAYING");
            }
            mController.onPlayStateChanged(mCurrentState);
        } else if (what == MEDIA_INFO_BUFFERING_END) {
            //缓冲结束，mediaplayer恢复 播放/暂停
            if (mCurrentState == STATE_BUFFERING_PLAYING) {
                mCurrentState = STATE_PLAYING;
                mController.onPlayStateChanged(mCurrentState);
                LogUtils.d("onInfo ——> MEDIA_INFO_BUFFERING_END： STATE_PLAYING");
            }
            if (mCurrentState == STATE_BUFFERING_PAUSED) {
                mCurrentState = STATE_PAUSED;
                mController.onPlayStateChanged(mCurrentState);
                LogUtils.d("onInfo ——> MEDIA_INFO_BUFFERING_END： STATE_PAUSED");
//            } else if (what == MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
//                // 视频旋转了extra度，需要恢复
//                if (mTextureView != null) {
//                    mTextureView.setRotation(extra);
//                    LogUtils.d("视频旋转角度：" + extra);
//                }
            } else if (what == MEDIA_INFO_NOT_SEEKABLE) {
                LogUtils.d("视频不能seekTo，为直播视频");
            } else {
                LogUtils.d("onInfo ——> what：" + what);
            }

        }
        return true;
    }
}