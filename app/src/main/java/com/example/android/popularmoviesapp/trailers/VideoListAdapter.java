package com.example.android.popularmoviesapp.trailers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmoviesapp.R;
import com.example.android.popularmoviesapp.models.MovieVideo;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoViewHolder> {

    private MovieVideo[] mVideos;
    private final Context parentContext;

    public VideoListAdapter(Context context) {
        parentContext = context;
    }

    @Override
    public VideoListAdapter.VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.video_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);

        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        // Populate view list ui.
        MovieVideo video = mVideos[position];
        String videoKey = video.getKey();

        holder.videoName.setText(video.getName());
        ((VideoListAdapterOnBindImage) parentContext).loadImageIntoView(holder, videoKey);
    }

    @Override
    public int getItemViewType(int position) {
        // IMPT : Prevents inconsistent update of multiple views inside viewholder during scrolling.
        return position;
    }

    @Override
    public int getItemCount() {
        return null == mVideos ? 0 : mVideos.length;
    }

    public void setVideosData(MovieVideo[] videos) {
        mVideos = videos;
        notifyDataSetChanged();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView videoThumbnail;
        private final TextView videoName;
        private Button watchVideoButton;

        public VideoViewHolder (View itemView) {
            super(itemView);
            videoThumbnail = itemView.findViewById(R.id.iv_video_thumbnail);
            videoName = itemView.findViewById(R.id.tv_video_name);
            watchVideoButton = itemView.findViewById(R.id.button_watch_trailer);
            watchVideoButton.setOnClickListener(this);
        }

        public ImageView getVideoThumbnail() {
            return videoThumbnail;
        }

        @Override
        public void onClick(View view) {
            ((VideoListAdapterOnClickHandler) parentContext).onClick(mVideos[getAdapterPosition()]);
        }
    }

    public interface VideoListAdapterOnBindImage {
        void loadImageIntoView(VideoViewHolder holder, String key);
    }

    public interface  VideoListAdapterOnClickHandler {
        void onClick(MovieVideo video);
    }
}
