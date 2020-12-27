package com.hieutm.homepi.ui.commander;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hieutm.homepi.R;
import com.hieutm.homepi.data.model.Commander;

import java.util.List;

public class CommanderListAdapter extends ArrayAdapter<Commander> {
    public interface ItemClickListener {
        void onUnregister(Commander commander);
    }

    private final ItemClickListener itemClickListener;

    public CommanderListAdapter(@NonNull Context context, @NonNull List<Commander> objects, ItemClickListener itemClickListener) {
        super(context, 0, objects);
        this.itemClickListener = itemClickListener;
    }

    public void setCommanders(List<Commander> commanders) {
        clear();
        addAll(commanders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Commander commander = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_item, parent, false);
        }
        TextView deviceTitleView = convertView.findViewById(R.id.device_title_view);
        TextView deviceSubtitleView = convertView.findViewById(R.id.device_subtitle_view);
        deviceTitleView.setText(commander.getDisplayName());
        deviceSubtitleView.setText(commander.getId());
        convertView.setOnClickListener(v -> itemClickListener.onUnregister(commander));
        return convertView;
    }
}
