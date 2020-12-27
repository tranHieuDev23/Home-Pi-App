package com.hieutm.homepi.ui.device;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hieutm.homepi.R;
import com.hieutm.homepi.data.model.Device;
import com.hieutm.homepi.data.model.DeviceType;

import java.util.List;

public class DeviceListAdapter extends ArrayAdapter<Device> {
    public interface UnregisterListener {
        void onUnregister(Device commander);
    }

    private final UnregisterListener unregisterListener;

    public DeviceListAdapter(@NonNull Context context, @NonNull List<Device> objects, UnregisterListener unregisterListener) {
        super(context, 0, objects);
        this.unregisterListener = unregisterListener;
    }

    public void setDevices(List<Device> devices) {
        clear();
        addAll(devices);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Device device = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_item, parent, false);
        }
        ImageView deviceImageView = convertView.findViewById(R.id.device_image_view);
        TextView deviceTitleView = convertView.findViewById(R.id.device_title_view);
        TextView deviceSubtitleView = convertView.findViewById(R.id.device_subtitle_view);
        deviceImageView.setImageResource(getDeviceImageResourceId(device.getType()));
        deviceTitleView.setText(device.getDisplayName());
        deviceSubtitleView.setText(device.getId());
        return convertView;
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
