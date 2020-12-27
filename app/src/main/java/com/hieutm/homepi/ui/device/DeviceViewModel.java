package com.hieutm.homepi.ui.device;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hieutm.homepi.R;
import com.hieutm.homepi.data.Result;
import com.hieutm.homepi.data.model.Device;
import com.hieutm.homepi.homecontrol.HomeControlService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DeviceViewModel extends ViewModel {
    private final MutableLiveData<List<Device>> devices;
    private final MutableLiveData<Integer> errors;
    private final HomeControlService homeControlService;

    public DeviceViewModel(HomeControlService homeControlService) {
        this.devices = new MutableLiveData<>(new ArrayList<>());
        this.errors = new MutableLiveData<>(null);

        this.homeControlService = homeControlService;
        this.homeControlService.getDevicesOfUser(new Result.ResultHandler<List<Device>>() {
            @Override
            public void onSuccess(Result.Success<List<Device>> result) {
                devices.setValue(result.getData());
            }

            @Override
            public void onError(Result.Error error) {
                Log.e(DeviceViewModel.class.getName(), error.getError().getMessage());
                errors.setValue(R.string.error_cannot_connect);
            }
        });
    }

    public LiveData<List<Device>> getDevices() {
        return devices;
    }

    public LiveData<Integer> getErrors() {
        return errors;
    }

    public void registerDevice(@NotNull String deviceId) {
        homeControlService.registerDevice(deviceId, new Result.ResultHandler<Device>() {
            @Override
            public void onSuccess(Result.Success<Device> result) {
                @SuppressWarnings("ConstantConditions") List<Device> newList = new ArrayList<>(devices.getValue());
                newList.add(result.getData());
                devices.setValue(newList);
            }

            @Override
            public void onError(Result.Error error) {
                Log.e(DeviceViewModel.class.getName(), error.getError().getMessage());
                errors.setValue(R.string.error_cannot_connect);
            }
        });
    }

    public void unregisterDevice(@NotNull String deviceId) {
        @SuppressWarnings("ConstantConditions") List<Device> newList = new ArrayList<>(devices.getValue());
        newList.removeIf(item -> item.getId().equals(deviceId));
        devices.setValue(newList);
    }
}