package com.hieutm.homepi.ui;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hieutm.homepi.auth.AuthenticationService;
import com.hieutm.homepi.homecontrol.HomeControlService;
import com.hieutm.homepi.ui.commander.CommanderViewModel;
import com.hieutm.homepi.ui.device.DeviceViewModel;
import com.hieutm.homepi.ui.login.LoginViewModel;
import com.hieutm.homepi.ui.registerdevice.RegisterDeviceViewModel;
import com.hieutm.homepi.ui.selectwifi.ConnectWifiViewModel;

public class AppViewModelFactory implements ViewModelProvider.Factory {
    private final AuthenticationService authService;
    private final HomeControlService homeControlService;

    private AppViewModelFactory(Context context) {
        this.authService = AuthenticationService.getInstance(context);
        this.homeControlService = HomeControlService.getInstance(context);
    }

    private static volatile AppViewModelFactory INSTANCE = null;

    public static AppViewModelFactory getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AppViewModelFactory(context);
        }
        return INSTANCE;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(authService);
        }
        if (modelClass.isAssignableFrom(CommanderViewModel.class)) {
            return (T) new CommanderViewModel(homeControlService);
        }
        if (modelClass.isAssignableFrom(DeviceViewModel.class)) {
            return (T) new DeviceViewModel(homeControlService);
        }
        if (modelClass.isAssignableFrom(RegisterDeviceViewModel.class)) {
            return (T) new RegisterDeviceViewModel(authService, homeControlService);
        }
        if (modelClass.isAssignableFrom(ConnectWifiViewModel.class)) {
            return (T) new ConnectWifiViewModel();
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}