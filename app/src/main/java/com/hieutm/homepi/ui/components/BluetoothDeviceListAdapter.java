package com.hieutm.homepi.ui.components;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hieutm.homepi.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BluetoothDeviceListAdapter extends RecyclerView.Adapter<BluetoothDeviceListAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final ImageView deviceImageView;
        private final TextView deviceTitleView;
        private final TextView deviceSubtitleView;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.view = view;
            deviceImageView = view.findViewById(R.id.device_list_item_image);
            deviceTitleView = view.findViewById(R.id.device_list_item_title);
            deviceSubtitleView = view.findViewById(R.id.device_list_item_subtitle);
        }

        public View getView() {
            return view;
        }
        public ImageView getDeviceImageView() {
            return deviceImageView;
        }
        public TextView getDeviceTitleView() {
            return deviceTitleView;
        }
        public TextView getDeviceSubtitleView() {
            return deviceSubtitleView;
        }
    }

    public interface ItemClickListener {
        void onClick(int position, BluetoothDevice device);
    }

    private List<BluetoothDevice> objects;
    private final ItemClickListener itemClickListener;

    public BluetoothDeviceListAdapter(@NonNull List<BluetoothDevice> objects, @NotNull ItemClickListener itemClickListener) {
        this.objects = objects;
        this.itemClickListener = itemClickListener;
    }

    public void setDevices(List<BluetoothDevice> objects) {
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
        BluetoothDevice device = objects.get(position);
        holder.getDeviceImageView().setImageResource(R.drawable.ic_bluetooth);
        holder.getDeviceTitleView().setText(device.getName());
        holder.getDeviceSubtitleView().setText(device.getAddress());
        holder.getView().setOnClickListener(v -> itemClickListener.onClick(position, device));
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }
}
