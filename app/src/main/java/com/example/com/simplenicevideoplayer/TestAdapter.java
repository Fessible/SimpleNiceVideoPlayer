package com.example.com.simplenicevideoplayer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.com.videoplayer.NiceVideoPlayer;
import com.example.com.videoplayer.TxVideoPlayerController;

import java.util.List;

/**
 * 适配器
 * Created by rhm on 2018/1/25.
 */

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {
    private Context context;
    private List<String> urlList;

    public TestAdapter(Context context, List<String> urlList) {
        this.context = context;
        this.urlList = urlList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recycler_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(urlList.get(position));
    }

    @Override
    public int getItemCount() {
        return urlList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public NiceVideoPlayer niceVideoPlayer;
        private TxVideoPlayerController controller;

        public ViewHolder(View itemView) {
            super(itemView);
            niceVideoPlayer = itemView.findViewById(R.id.nice_video_player);
            controller = new TxVideoPlayerController(context);
            niceVideoPlayer.setController(controller);
        }

        public void bindData(String url) {
            controller.setUrl(url);
        }
    }


}
