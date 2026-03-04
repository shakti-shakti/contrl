package com.family.parentalcontrol.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.models.Media;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
    private Context context;
    private List<Media> mediaList;

    public MediaAdapter(Context context, List<Media> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false);
        return new MediaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Media media = mediaList.get(position);
        
        holder.mediaName.setText(media.getFileName());
        holder.mediaType.setText(media.getType().toUpperCase());
        
        long timestamp = media.getTimestamp();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
        holder.mediaDate.setText(dateStr);
    }

    @Override
    public int getItemCount() {
        return mediaList != null ? mediaList.size() : 0;
    }

    public static class MediaViewHolder extends RecyclerView.ViewHolder {
        TextView mediaName;
        TextView mediaType;
        TextView mediaDate;
        ImageView mediaIcon;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            mediaName = itemView.findViewById(R.id.mediaName);
            mediaType = itemView.findViewById(R.id.mediaType);
            mediaDate = itemView.findViewById(R.id.mediaDate);
            mediaIcon = itemView.findViewById(R.id.mediaIcon);
        }
    }
}
