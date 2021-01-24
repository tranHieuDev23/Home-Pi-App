package com.hieutm.homepi.ui.device;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.hieutm.homepi.R;
import com.hieutm.homepi.models.Device;
import com.hieutm.homepi.ui.components.DeviceRenameDialogFragment;

public class DeviceBottomSheetFragment extends BottomSheetDialogFragment {
    public interface UnregisterListener {
        void onUnregister(Device device);
    }

    public interface RenameListener {
        void onRename(String newName);
    }

    private final Device device;
    private final UnregisterListener unregisterListener;
    private final RenameListener renameListener;

    public DeviceBottomSheetFragment(Device device, UnregisterListener unregisterListener, RenameListener renameListener) {
        this.device = device;
        this.unregisterListener = unregisterListener;
        this.renameListener = renameListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.device_list_item_bottom_sheet, container);
        TextView bottomSheetTitle = root.findViewById(R.id.device_list_bottom_sheet_title);
        TextView bottomSheetSubtitle = root.findViewById(R.id.device_list_bottom_sheet_subtitle);
        Button bottomSheetUnregisterButton = root.findViewById(R.id.device_list_unregister_button);
        Button bottomSheetRenameButton = root.findViewById(R.id.device_list_rename_button);

        bottomSheetTitle.setText(device.getDisplayName());
        bottomSheetSubtitle.setText(device.getId());
        bottomSheetUnregisterButton.setOnClickListener(v -> {
            unregisterListener.onUnregister(device);
            dismiss();
        });
        bottomSheetRenameButton.setOnClickListener(v -> {
            DeviceRenameDialogFragment dialogFragment = new DeviceRenameDialogFragment(device.getDisplayName(), newName -> {
                if (device.getDisplayName().equals(newName)) {
                    return false;
                }
                renameListener.onRename(newName);
                return true;
            });
            dialogFragment.show(getParentFragmentManager(), "RenameDeviceDialog");
            dismiss();
        });
        return root;
    }
}
