package com.hieutm.homepi.ui.device;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hieutm.homepi.R;
import com.hieutm.homepi.data.model.Device;
import com.hieutm.homepi.data.model.DeviceType;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private  final View view;
        private final ImageView deviceImageView;
        private final TextView deviceTitleView;
        private final TextView deviceSubtitleView;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.view = view;
            deviceImageView = view.findViewById(R.id.device_image_view);
            deviceTitleView = view.findViewById(R.id.device_title_view);
            deviceSubtitleView = view.findViewById(R.id.device_subtitle_view);
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
        void onClick(Device device);
    }

    private List<Device> objects;
    private final ItemClickListener itemClickListener;

    public DeviceListAdapter(@NonNull List<Device> objects, @NotNull ItemClickListener itemClickListener) {
        this.objects = objects;
        this.itemClickListener = itemClickListener;
    }

    public void setDevices(List<Device> objects) {
        this.objects = objects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false);
        return new DeviceListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceListAdapter.ViewHolder holder, int position) {
        Device device = objects.get(position);
        holder.getDeviceImageView().setImageResource(getDeviceImageResourceId(device.getType()));
        holder.getDeviceTitleView().setText(device.getDisplayName());
        holder.getDeviceSubtitleView().setText(device.getId());
        holder.getView().setOnClickListener(v -> itemClickListener.onClick(device));
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    private int getDeviceImageResourceId(DeviceType type) {
        switch (type) {
            case LIGHT:
                return R.drawable.ic_smart_light;
            case THERMOSTAT:
                return R.drawable.ic_thermostat;
            default:
                return R.drawable.ic_launcher_foreground;
        }
    }
}
