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

public class CommanderBottomSheetFragment extends BottomSheetDialogFragment {
    public interface UnregisterListener {
        void onUnregister(Commander commander);
    }

    private final Commander commander;
    private final UnregisterListener unregisterListener;

    public CommanderBottomSheetFragment(Commander commander, UnregisterListener unregisterListener) {
        this.commander = commander;
        this.unregisterListener = unregisterListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.device_list_item_bottom_sheet, container);
        TextView bottomSheetTitle = root.findViewById(R.id.device_list_bottom_sheet_title);
        TextView bottomSheetSubtitle = root.findViewById(R.id.device_list_bottom_sheet_subtitle);
        Button bottomSheetUnregisterButton = root.findViewById(R.id.device_list_unregister_button);
        bottomSheetTitle.setText(commander.getDisplayName());
        bottomSheetSubtitle.setText(commander.getId());
        bottomSheetUnregisterButton.setOnClickListener(v -> {
            unregisterListener.onUnregister(commander);
            dismiss();
        });
        return root;
    }
}
