package com.example.com.simplenicevideoplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.com.videoplayer.NiceVideoPlayer;
import com.example.com.videoplayer.NiceVideoPlayerManager;
import com.example.com.videoplayer.TxVideoPlayerController;

/**
 * Created by rhm on 2018/1/25.
 */

public class NormalActivity extends AppCompatActivity {
    private NiceVideoPlayer niceVideoPlayer;
    public String url = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4";
    public String imgUrl = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-30-43.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_layout);
        niceVideoPlayer = findViewById(R.id.nice_video_player);

        TxVideoPlayerController controller = new TxVideoPlayerController(this);
        niceVideoPlayer.setController(controller);
        controller.setUrl(url);
        controller.setTitle("呵呵呵");
    }

    @Override
    public void onBackPressed() {
        if (NiceVideoPlayerManager.instance().onBackPressd()) return;
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        NiceVideoPlayerManager.instance().releaseNiceVideoPlayer();
    }
}
