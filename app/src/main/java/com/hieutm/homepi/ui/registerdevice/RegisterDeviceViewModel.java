package com.hieutm.homepi.ui.registerdevice;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hieutm.homepi.auth.AuthenticationService;
import com.hieutm.homepi.bluetooth.BluetoothHelper;
import com.hieutm.homepi.homecontrol.DeviceCommunicationHelper;
import com.hieutm.homepi.homecontrol.HomeControlService;
import com.hieutm.homepi.models.Device;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;

public class RegisterDeviceViewModel extends ViewModel {
    private static final BluetoothHelper BLUETOOTH_HELPER = BluetoothHelper.getInstance();

    private final MutableLiveData<Boolean> isBluetoothEnabled;
    private final MutableLiveData<List<BluetoothDevice>> devices;
    private final MutableLiveData<Boolean> isDiscovering;
    private final MutableLiveData<Boolean> isRegistering;
    private final AuthenticationService authService;
    private final HomeControlService homeControlService;

    public RegisterDeviceViewModel(AuthenticationService authService, HomeControlService homeControlService) {
        this.authService = authService;
        this.homeControlService = homeControlService;
        isBluetoothEnabled = new MutableLiveData<>(BLUETOOTH_HELPER.isBluetoothEnabled());
        devices = new MutableLiveData<>(BLUETOOTH_HELPER.getPairedDevices());
        isDiscovering = new MutableLiveData<>(false);
        isRegistering = new MutableLiveData<>(false);
    }

    public LiveData<Boolean> getIsBluetoothEnabled() {
        return isBluetoothEnabled;
    }

    public LiveData<List<BluetoothDevice>> getDevices() {
        return devices;
    }

    public MutableLiveData<Boolean> getIsDiscovering() {
        return isDiscovering;
    }

    public LiveData<Boolean> getIsRegistering() {
        return isRegistering;
    }

    public void refreshBluetoothStatus() {
        isBluetoothEnabled.setValue(BLUETOOTH_HELPER.isBluetoothEnabled());
        devices.setValue(BLUETOOTH_HELPER.getPairedDevices());
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
        devices.getValue().add(device);
        devices.setValue(devices.getValue());
    }

    @SuppressLint("CheckResult")
    public Single<Device> registerDevice(String mac) {
        DeviceCommunicationHelper helper = new DeviceCommunicationHelper(mac);
        return new Single<Device>() {
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super Device> observer) {
                isRegistering.postValue(true);
                authService.getCurrentUser().subscribe(user -> helper
                        .connect()
                        .andThen(helper.getWifiStatus())
                        .subscribe((connected, connectThrowable) -> {
                            if (connectThrowable != null) {
                                observer.onError(connectThrowable);
                                return;
                            }
                            if (!connected) {
                                observer.onError(new DeviceNotConnectedToWifiException());
                                return;
                            }
                            helper
                                    .getDeviceId()
                                    .subscribe((id, getIdThrowable) -> {
                                        if (getIdThrowable != null) {
                                            observer.onError(getIdThrowable);
                                            return;
                                        }
                                        homeControlService
                                                .registerDevice(id)
                                                .subscribe((response, registerThrowable) -> {
                                                    if (registerThrowable != null) {
                                                        observer.onError(registerThrowable);
                                                        return;
                                                    }
                                                    helper
                                                            .registerDevice(user.getCommandTopic(), user.getStatusTopic(), response.getToken())
                                                            .subscribe(() -> observer.onSuccess(response.getDevice()), observer::onError);
                                                });
                                    });
                        }), observer::onError, () -> observer.onError(new RuntimeException("User is not logged in")));
            }
        }.doFinally(() -> {
            isRegistering.postValue(false);
            helper.disconnect();
        });
    }
}
