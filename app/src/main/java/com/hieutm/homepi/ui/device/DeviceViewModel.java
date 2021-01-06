package com.hieutm.homepi.ui.device;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hieutm.homepi.R;
import com.hieutm.homepi.data.model.Device;
import com.hieutm.homepi.homecontrol.HomeControlService;
import com.hieutm.homepi.ui.commander.CommanderViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DeviceViewModel extends ViewModel {
    private final MutableLiveData<List<Device>> devices;
    private final MutableLiveData<Integer> errors;
    private final HomeControlService homeControlService;

    @SuppressLint("CheckResult")
    public DeviceViewModel(HomeControlService homeControlService) {
        this.devices = new MutableLiveData<>(new ArrayList<>());
        this.errors = new MutableLiveData<>(null);
        this.homeControlService = homeControlService;
        this.homeControlService.getDevicesOfUser().subscribe(device -> {
            //noinspection Convert2MethodRef
            addDevice(device);
        }, error -> {
            errors.setValue(R.string.error_cannot_connect);
        }, () -> {

        });
    }

    public LiveData<List<Device>> getDevices() {
        return devices;
    }

    public LiveData<Integer> getErrors() {
        return errors;
    }

    @SuppressLint("CheckResult")
    public void registerDevice(@NotNull String deviceId) {
        homeControlService.registerDevice(deviceId).subscribe(this::addDevice, error -> {
            Log.e(DeviceViewModel.class.getName(), error.getMessage());
            errors.setValue(R.string.error_cannot_connect);
        });
    }

    public void unregisterDevice(@NotNull String deviceId) {
        removeDevice(deviceId);
    }

    private void addDevice(@NotNull Device device) {
        List<Device> newList = devices.getValue();
        //noinspection ConstantConditions
        newList.add(device);
        devices.setValue(newList);
    }

    private void removeDevice(@NotNull String deviceId) {
        List<Device> newList = devices.getValue();
        //noinspection ConstantConditions
        newList.removeIf(item -> item.getId().equals(deviceId));
        devices.setValue(newList);
    }
}