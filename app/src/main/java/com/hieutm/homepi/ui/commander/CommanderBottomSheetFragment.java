package com.hieutm.homepi.ui.commander;

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
import com.hieutm.homepi.models.Commander;
import com.hieutm.homepi.ui.components.DeviceRenameDialogFragment;

public class CommanderBottomSheetFragment extends BottomSheetDialogFragment {
    public interface UnregisterListener {
        void onUnregister(Commander commander);
    }

    public interface RenameListener {
        void onRename(String newName);
    }

    private final Commander commander;
    private final UnregisterListener unregisterListener;
    private final RenameListener renameListener;

    public CommanderBottomSheetFragment(Commander commander, UnregisterListener unregisterListener, RenameListener renameListener) {
        this.commander = commander;
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

        bottomSheetTitle.setText(commander.getDisplayName());
        bottomSheetSubtitle.setText(commander.getId());
        bottomSheetUnregisterButton.setOnClickListener(v -> {
            unregisterListener.onUnregister(commander);
            dismiss();
        });
        bottomSheetRenameButton.setOnClickListener(v -> {
            DeviceRenameDialogFragment dialogFragment = new DeviceRenameDialogFragment(commander.getDisplayName(), newName -> {
                if (commander.getDisplayName().equals(newName)) {
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
