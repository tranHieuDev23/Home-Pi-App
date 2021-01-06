package com.hieutm.homepi.ui.registerdevice;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hieutm.homepi.bluetooth.BluetoothHelper;
import com.hieutm.homepi.data.model.Device;
import com.hieutm.homepi.data.model.DeviceType;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

public class RegisterDeviceViewModel extends ViewModel {
    private static final BluetoothHelper BLUETOOTH_HELPER = BluetoothHelper.getInstance();

    private final MutableLiveData<Boolean> isBluetoothEnabled;
    private final MutableLiveData<List<BluetoothDeviceListItem>> devices;
    private final MutableLiveData<Boolean> isDiscovering;
    private final MutableLiveData<Boolean> isRegistering;

    public RegisterDeviceViewModel() {
        isBluetoothEnabled = new MutableLiveData<>(BLUETOOTH_HELPER.isBluetoothEnabled());
        devices = new MutableLiveData<>(BLUETOOTH_HELPER.getPairedDevices().stream()
                .map(item -> new BluetoothDeviceListItem(item, false))
                .collect(Collectors.toList()));
        isDiscovering = new MutableLiveData<>(false);
        isRegistering = new MutableLiveData<>(false);
    }

    public LiveData<Boolean> getIsBluetoothEnabled() {
        return isBluetoothEnabled;
    }

    public LiveData<List<BluetoothDeviceListItem>> getDevices() {
        return devices;
    }

    public LiveData<Boolean> getIsDiscovering() {
        return isDiscovering;
    }

    public LiveData<Boolean> getIsRegistering() {
        return isRegistering;
    }

    public void refreshBluetoothStatus() {
        isBluetoothEnabled.setValue(BLUETOOTH_HELPER.isBluetoothEnabled());
        devices.setValue((BLUETOOTH_HELPER.getPairedDevices().stream()
                .map(item -> new BluetoothDeviceListItem(item, false))
                .collect(Collectors.toList())));
    }

    public void startDiscovering() {
        isDiscovering.setValue(true);
        BLUETOOTH_HELPER.startDiscovering();
    }

    public void stopDiscovering() {
        isDiscovering.setValue(false);
        BLUETOOTH_HELPER.stopDiscovering();
    }

    public void addDiscoveredDevice(BluetoothDevice device) {
        List<BluetoothDeviceListItem> newList = devices.getValue();
        //noinspection ConstantConditions
        newList.add(new BluetoothDeviceListItem(device, false));
        devices.setValue(newList);
    }

    @SuppressLint("CheckResult")
    public Single<Device> registerDevice(int position) {
        return new Single<Device>() {
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super Device> observer) {
                setDeviceListItemRegistering(position, true);
                @SuppressWarnings("ConstantConditions") BluetoothDeviceListItem device = devices.getValue().get(position);
                String mac = device.getDevice().getAddress();
                BLUETOOTH_HELPER.connect(mac)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((bluetoothCommunicationHandler, bluetoothConnectionError) -> {
                            if (bluetoothConnectionError != null) {
                                setDeviceListItemRegistering(position, false);
                                observer.onError(bluetoothConnectionError);
                                return;
                            }
                            bluetoothCommunicationHandler.sendMessage("{\"action\": \"getId\"}")
                                    .subscribe((response, bluetoothCommunicationError) -> {
                                        if (bluetoothCommunicationError != null) {
                                            setDeviceListItemRegistering(position, false);
                                            observer.onError(bluetoothCommunicationError);
                                            return;
                                        }
                                        setDeviceListItemRegistering(position, false);
                                        observer.onSuccess(new Device(response, "Home Pi Light", DeviceType.LIGHT));
                                    });

                        });
            }
        };
    }

    private void setDeviceListItemRegistering(int position, boolean isLoading) {
        this.isRegistering.postValue(isLoading);
        List<BluetoothDeviceListItem> newList = devices.getValue();
        //noinspection ConstantConditions
        newList.get(position).setLoading(isLoading);
        devices.setValue(newList);
    }
}
