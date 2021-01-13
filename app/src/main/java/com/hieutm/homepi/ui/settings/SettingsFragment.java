package com.hieutm.homepi.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.hieutm.homepi.R;
import com.hieutm.homepi.ui.AppViewModelFactory;
import com.hieutm.homepi.ui.MainActivity;
import com.hieutm.homepi.ui.login.LoginActivity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SettingsFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        Activity activity = getActivity();
        if (activity == null) {
            return root;
        }

        final ViewModelProvider.Factory viewModelFactory = AppViewModelFactory.getInstance(activity.getApplicationContext());
        final ViewModelProvider modelProvider = new ViewModelProvider((ViewModelStoreOwner) activity, viewModelFactory);
        SettingsViewModel settingsViewModel = modelProvider.get(SettingsViewModel.class);

        View userInfoView = root.findViewById(R.id.settings_user_info_layout);
        TextView titleView = root.findViewById(R.id.settings_user_title);
        TextView subtitleView = root.findViewById(R.id.settings_user_subtitle);
        settingsViewModel.getCurrentUser().observe(getViewLifecycleOwner(), loggedInUser -> {
            if (loggedInUser == null) {
                userInfoView.setVisibility(View.GONE);
            } else {
                userInfoView.setVisibility(View.VISIBLE);
                titleView.setText(loggedInUser.getDisplayName());
                subtitleView.setText(loggedInUser.getUsername());
            }
        });

        Button logoutButton = root.findViewById(R.id.settings_logout_button);
        logoutButton.setOnClickListener(v -> settingsViewModel
                .logOut()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::openLoginActivity, throwable -> Toast.makeText(activity, R.string.settings_cannot_logout, Toast.LENGTH_SHORT).show()));
        return root;
    }

    private void openLoginActivity() {
        Activity activity = getActivity();
        Intent intent = new Intent(activity, MainActivity.class);
        startActivity(intent);
        activity.finish();
    }
}