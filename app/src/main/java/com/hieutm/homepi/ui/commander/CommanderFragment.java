package com.hieutm.homepi.ui.commander;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hieutm.homepi.R;

public class CommanderFragment extends Fragment {

    private CommanderViewModel commanderViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        commanderViewModel =
                new ViewModelProvider(this).get(CommanderViewModel.class);
        View root = inflater.inflate(R.layout.fragment_commander, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        commanderViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}