package com.hieutm.homepi.ui.commander;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hieutm.homepi.R;
import com.hieutm.homepi.models.Commander;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommanderListAdapter extends RecyclerView.Adapter<CommanderListAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final TextView deviceTitleView;
        private final TextView deviceSubtitleView;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            deviceTitleView = view.findViewById(R.id.device_list_item_title);
            deviceSubtitleView = view.findViewById(R.id.device_list_item_subtitle);
        }

        public View getView() {
            return view;
        }

        public TextView getDeviceTitleView() {
            return deviceTitleView;
        }

        public TextView getDeviceSubtitleView() {
            return deviceSubtitleView;
        }
    }

    public interface ItemClickListener {
        void onClick(Commander commander);
    }

    private List<Commander> objects;
    private final ItemClickListener itemClickListener;

    public CommanderListAdapter(@NotNull List<Commander> objects, ItemClickListener itemClickListener) {
        this.objects = objects;
        this.itemClickListener = itemClickListener;
    }

    public void setCommanders(List<Commander> objects) {
        this.objects = objects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Commander commander = objects.get(position);
        holder.getDeviceTitleView().setText(commander.getDisplayName());
        holder.getDeviceSubtitleView().setText(commander.getId());
        holder.getView().setOnClickListener(v -> itemClickListener.onClick(commander));
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }
}
