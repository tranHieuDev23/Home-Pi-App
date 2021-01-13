package com.hieutm.homepi.ui.device;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hieutm.homepi.R;
import com.hieutm.homepi.homecontrol.HomeControlService;
import com.hieutm.homepi.models.Device;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DeviceViewModel extends ViewModel {
    private final MutableLiveData<List<Device>> devices;
    private final MutableLiveData<Integer> errors;
    private final MutableLiveData<Boolean> isLoading;
    private final HomeControlService homeControlService;

    @SuppressLint("CheckResult")
    public DeviceViewModel(HomeControlService homeControlService) {
        this.devices = new MutableLiveData<>();
        this.errors = new MutableLiveData<>(null);
        this.isLoading = new MutableLiveData<>(false);
        this.homeControlService = homeControlService;
    }

    public LiveData<List<Device>> getDevices() {
        return devices;
    }

    public LiveData<Integer> getErrors() {
        return errors;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void unregisterDevice(@NotNull String deviceId) {
        removeDevice(deviceId);
    }

    @SuppressLint("CheckResult")
    public void refresh() {
        devices.setValue(new ArrayList<>());
        isLoading.setValue(true);
        this.homeControlService.getDevicesOfUser().subscribe(
                this::addDevice,
                error -> errors.setValue(R.string.error_cannot_connect),
                () -> isLoading.setValue(false));
    }

    private void addDevice(@NotNull Device device) {
        devices.getValue().add(device);
        devices.setValue(devices.getValue());
    }

    private void removeDevice(@NotNull String deviceId) {
        devices.getValue().removeIf(item -> item.getId().equals(deviceId));
        devices.setValue(devices.getValue());
    }
}