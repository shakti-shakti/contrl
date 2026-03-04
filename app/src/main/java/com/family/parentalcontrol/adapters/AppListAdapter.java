package com.family.parentalcontrol.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.family.parentalcontrol.R;
import com.family.parentalcontrol.utils.BlockedAppsHelper;

import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    private Context context;
    private List<ApplicationInfo> apps;
    private PackageManager pm;
    private BlockedAppsHelper blockedAppsHelper;

    public AppListAdapter(Context context, List<ApplicationInfo> apps) {
        this.context = context;
        this.apps = apps;
        pm = context.getPackageManager();
        blockedAppsHelper = new BlockedAppsHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApplicationInfo info = apps.get(position);
        String name = pm.getApplicationLabel(info).toString();
        String pkg = info.packageName;
        String category = com.family.parentalcontrol.utils.AppCategoryHelper.categorize(pkg);
        holder.tvAppName.setText(name + " [" + category + "]");
        boolean blocked = blockedAppsHelper.isBlocked(pkg);
        holder.btnBlock.setText(blocked ? "Unblock" : "Block");
        holder.btnBlock.setOnClickListener(v -> {
            if (blockedAppsHelper.isBlocked(pkg)) {
                blockedAppsHelper.unblockApp(pkg);
            } else {
                blockedAppsHelper.blockApp(pkg);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return apps == null ? 0 : apps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAppName;
        Button btnBlock;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAppName = itemView.findViewById(R.id.tv_app_name);
            btnBlock = itemView.findViewById(R.id.btn_block);
        }
    }
}
