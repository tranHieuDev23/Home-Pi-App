package com.hieutm.homepi.ui.selectwifi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hieutm.homepi.R;
import com.hieutm.homepi.models.WifiNetwork;

import java.util.List;

public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final ImageView wifiImageView;
        private final TextView wifiTitleView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
            wifiImageView = itemView.findViewById(R.id.wifi_list_item_image);
            wifiTitleView = itemView.findViewById(R.id.wifi_list_item_title);
        }

        public View getView() {
            return view;
        }

        public ImageView getWifiImageView() {
            return wifiImageView;
        }

        public TextView getWifiTitleView() {
            return wifiTitleView;
        }
    }

    public interface ItemClickListener {
        void onClick(WifiNetwork item);
    }

    private List<WifiNetwork> objects;
    private final ItemClickListener itemClickListener;

    public WifiListAdapter(List<WifiNetwork> objects, ItemClickListener itemClickListener) {
        this.objects = objects;
        this.itemClickListener = itemClickListener;
    }

    public void setObjects(List<WifiNetwork> objects) {
        this.objects = objects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wifi_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WifiNetwork item = objects.get(position);
        holder.getWifiImageView().setImageResource(
                item.isOpen()
                        ? R.drawable.ic_wifi_unsecured
                        : R.drawable.ic_wifi_secured
        );
        holder.getWifiTitleView().setText(item.getSsid());
        holder.getView().setOnClickListener(v -> itemClickListener.onClick(item));
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }
}
