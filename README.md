## SimpleNiceVideoPlayer
简化版的NiceVideoPlayer，主要用于学习自定义视频播放器，原地址https://github.com/xiaoyanger0825/NiceVieoPlayer

## 使用方式
添加权限,添加configChanges，转换为全屏显示时不重新绘制
``` javascript
    <uses-permission android:name="android.permission.INTERNET" />

    <activity
            android:name=".NormalActivity"
            android:configChanges="orientation|keyboardHidden|screenSize"
            android:screenOrientation="portrait" />
```

#### 正常显示方式
``` javascript
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
```
布局文件

``` javascript
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.example.com.simplenicevideoplayer.MainActivity">

    <com.example.com.videoplayer.NiceVideoPlayer
        android:id="@+id/nice_video_player"
        android:layout_width="match_parent"
        android:layout_height="200dp"/>

</LinearLayout>

```

#### 列表使用方式

注意:添加回收

``` javascript
  recyclerView.setRecyclerListener(new RecyclerView.RecyclerListener() {
            @Override
            public void onViewRecycled(RecyclerView.ViewHolder holder) {
                if (holder instanceof TestAdapter.ViewHolder) {
                    NiceVideoPlayer niceVideoPlayer = ((TestAdapter.ViewHolder) holder).niceVideoPlayer;
                    if (niceVideoPlayer == NiceVideoPlayerManager.instance().getCurrentNiceVideoPlayer()) {
                        NiceVideoPlayerManager.instance().releaseNiceVideoPlayer();
                    }
                }
            }
        });
```
## 详细构建
[简化版SimpleNiceVideoPlayer详细解析CSDN][1]。

[简化版SimpleNiceVideoPlayer详细解析个人网站][2]。

  [1]: http://blog.csdn.net/fessible_max/article/details/79164438
  [2]: http://www.fessible.club/2018/01/22/%E8%87%AA%E5%AE%9A%E4%B9%89VideoView/#more


