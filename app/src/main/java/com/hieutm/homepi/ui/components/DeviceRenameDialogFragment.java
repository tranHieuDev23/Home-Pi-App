package com.hieutm.homepi.ui.components;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.hieutm.homepi.R;

public class DeviceRenameDialogFragment extends DialogFragment {
    public interface RenameListener {
        boolean onNameInput(String newName);
    }

    private final String oldName;
    private final RenameListener listener;

    public DeviceRenameDialogFragment(String oldName, RenameListener listener) {
        this.oldName = oldName;
        this.listener = listener;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.rename_device_dialog, null);
        EditText editText = dialogView.findViewById(R.id.rename_device_dialog_edit_text);
        editText.setText(oldName);
        builder
                .setView(dialogView)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = editText.getText().toString();
                    if (listener.onNameInput(newName)) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}
