package com.hieutm.homepi.ui.registerdevice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hieutm.homepi.data.Result;
import com.hieutm.homepi.data.model.Device;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RegisterDeviceViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isBluetoothEnabled;
    private final MutableLiveData<List<BluetoothDeviceListItem>> devices;
    private final MutableLiveData<Boolean> isRegistering;

    public RegisterDeviceViewModel() {
        isBluetoothEnabled = new MutableLiveData<>(true);
        devices = new MutableLiveData<>(new ArrayList<>());
        isRegistering = new MutableLiveData<>(false);
    }

    public LiveData<Boolean> getIsBluetoothEnabled() {
        return isBluetoothEnabled;
    }

    public LiveData<List<BluetoothDeviceListItem>> getDevices() {
        return devices;
    }

    public void discoverDevices() {
        List<BluetoothDevice> pairedDevices = new ArrayList<>();
        this.devices.setValue(
                pairedDevices.stream()
                        .map(item -> new BluetoothDeviceListItem(item, false))
                        .collect(Collectors.toList())
        );
    }

    public void requestBluetooth(@NotNull Activity context) {
        isBluetoothEnabled.setValue(true);
    }

    @SuppressLint("CheckResult")
    public void registerDevice(int position, Result.ResultHandler<Device> registerHandler) {
    }

    private void setDeviceListItemRegistering(int position, boolean isLoading) {
        this.isRegistering.postValue(isLoading);
        List<BluetoothDeviceListItem> newList = devices.getValue();
        //noinspection ConstantConditions
        newList.get(position).setLoading(isLoading);
        devices.postValue(newList);
    }
}
