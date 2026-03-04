package com.family.parentalcontrol.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.models.Child;

import java.util.List;

public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ViewHolder> {
    private Context context;
    private List<Child> children;
    private OnChildClickListener listener;

    public interface OnChildClickListener {
        void onChildClicked(Child child);
    }

    public ChildrenAdapter(Context context, List<Child> children, OnChildClickListener listener) {
        this.context = context;
        this.children = children;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_child, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Child child = children.get(position);
        holder.tvName.setText(child.getChildName());
        holder.tvStatus.setText("Status: " + child.getStatus());
        holder.tvLocation.setText("Last: " + child.getCurrentLocation());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChildClicked(child);
            }
        });
    }

    @Override
    public int getItemCount() {
        return children == null ? 0 : children.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvStatus;
        TextView tvLocation;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_child_name);
            tvStatus = itemView.findViewById(R.id.tv_child_status);
            tvLocation = itemView.findViewById(R.id.tv_child_location);
        }
    }
}
