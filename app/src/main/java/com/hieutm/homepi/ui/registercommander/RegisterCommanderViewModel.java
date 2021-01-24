package com.hieutm.homepi.ui.registercommander;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hieutm.homepi.auth.AuthenticationService;
import com.hieutm.homepi.bluetooth.BluetoothHelper;
import com.hieutm.homepi.homecontrol.DeviceCommunicationHelper;
import com.hieutm.homepi.homecontrol.HomeControlService;
import com.hieutm.homepi.models.Commander;
import com.hieutm.homepi.ui.registerdevice.DeviceNotConnectedToWifiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;

public class RegisterCommanderViewModel extends ViewModel {
    private static final BluetoothHelper BLUETOOTH_HELPER = BluetoothHelper.getInstance();

    private final Map<String, BluetoothDevice> mac2DeviceMap;
    private final MutableLiveData<Boolean> isBluetoothEnabled;
    private final MutableLiveData<List<BluetoothDevice>> devices;
    private final MutableLiveData<Boolean> isDiscovering;
    private final MutableLiveData<Boolean> isLoading;
    private final AuthenticationService authService;
    private final HomeControlService homeControlService;

    public RegisterCommanderViewModel(AuthenticationService authService, HomeControlService homeControlService) {
        this.authService = authService;
        this.homeControlService = homeControlService;
        isBluetoothEnabled = new MutableLiveData<>(BLUETOOTH_HELPER.isBluetoothEnabled());
        mac2DeviceMap = new HashMap<>();
        devices = new MutableLiveData<>(new ArrayList<>());
        isDiscovering = new MutableLiveData<>(false);
        isLoading = new MutableLiveData<>(false);
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

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void refreshBluetoothStatus() {
        isBluetoothEnabled.setValue(BLUETOOTH_HELPER.isBluetoothEnabled());
    }

    public void startDiscovering() {
        mac2DeviceMap.clear();
        devices.postValue(new ArrayList<>());
        isLoading.postValue(true);
        isDiscovering.postValue(true);
        BLUETOOTH_HELPER.startDiscovering();
    }

    public void stopDiscovering() {
        isLoading.postValue(false);
        isDiscovering.postValue(false);
        BLUETOOTH_HELPER.stopDiscovering();
    }

    public void addDiscoveredDevice(BluetoothDevice device) {
        mac2DeviceMap.put(device.getAddress(), device);
        devices.postValue(new ArrayList<>(mac2DeviceMap.values()));
    }

    @SuppressLint("CheckResult")
    public Single<Commander> registerCommander(String mac) {
        DeviceCommunicationHelper helper = new DeviceCommunicationHelper(mac);
        return new Single<Commander>() {
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super Commander> observer) {
                isLoading.postValue(true);
                authService
                        .getCurrentUser()
                        .subscribe(user -> helper.connect()
                                .andThen(helper.getDeviceId())
                                .subscribe((id, getIdThrowable) -> {
                                    if (getIdThrowable != null) {
                                        observer.onError(getIdThrowable);
                                        return;
                                    }
                                    homeControlService
                                            .checkDeviceOwnership(id)
                                            .subscribe((isRegistered, checkOwnershipThrowable) -> {
                                                if (checkOwnershipThrowable != null) {
                                                    observer.onError(checkOwnershipThrowable);
                                                    return;
                                                }
                                                helper.getWifiStatus()
                                                        .subscribe((connected, connectThrowable) -> {
                                                            if (connectThrowable != null) {
                                                                observer.onError(connectThrowable);
                                                                return;
                                                            }
                                                            if (!connected) {
                                                                observer.onError(new CommanderNotConnectedToWifiException());
                                                                return;
                                                            }
                                                            homeControlService
                                                                    .registerCommander(id)
                                                                    .subscribe((response, registerThrowable) -> {
                                                                        if (registerThrowable != null) {
                                                                            observer.onError(registerThrowable);
                                                                            return;
                                                                        }
                                                                        helper
                                                                                .registerDevice(response.getToken())
                                                                                .subscribe(() -> observer.onSuccess(response.getCommander()), observer::onError);
                                                                    });
                                                        });
                                            });
                                }), observer::onError, () -> observer.onError(new RuntimeException("User has not logged in")));
            }
        }.doFinally(() -> {
            isLoading.postValue(false);
            helper.disconnect();
        });
    }
}
