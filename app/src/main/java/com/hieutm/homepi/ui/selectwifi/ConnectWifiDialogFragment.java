package com.hieutm.homepi.ui.selectwifi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.hieutm.homepi.R;

public class ConnectWifiDialogFragment extends DialogFragment {
    public interface PasswordInputListener {
        boolean onPasswordInput(String password);
    }

    private final String ssid;
    private final PasswordInputListener listener;

    public ConnectWifiDialogFragment(String ssid, PasswordInputListener listener) {
        this.ssid = ssid;
        this.listener = listener;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.connect_wifi_auth_dialog, null);
        TextView ssidView = dialogView.findViewById(R.id.select_wifi_auth_dialog_ssid_text);
        ssidView.setText(ssid);
        TextInputEditText pskInput = dialogView.findViewById(R.id.select_wifi_auth_dialog_psk_input);
        builder
                .setView(dialogView)
                .setPositiveButton("Connect", (dialog, which) -> {
                    String psk = pskInput.getText().toString();
                    if (listener.onPasswordInput(psk)) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}
