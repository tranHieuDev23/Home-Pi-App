package com.hieutm.homepi.ui.selectwifi;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hieutm.homepi.bluetooth.BluetoothHelper;
import com.hieutm.homepi.homecontrol.DeviceCommunicationHelper;
import com.hieutm.homepi.models.WifiNetwork;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;

public class ConnectWifiViewModel extends ViewModel {
    private static final BluetoothHelper BLUETOOTH_HELPER = BluetoothHelper.getInstance();

    private String mac;
    private final MutableLiveData<Boolean> isBluetoothEnabled;
    private final MutableLiveData<List<WifiNetwork>> wifiNetworks;
    private final MutableLiveData<WifiNetwork> selectedWifiNetwork;
    private final MutableLiveData<Boolean> isLoading;

    public ConnectWifiViewModel() {
        mac = null;
        isBluetoothEnabled = new MutableLiveData<>(BLUETOOTH_HELPER.isBluetoothEnabled());
        wifiNetworks = new MutableLiveData<>(new ArrayList<>());
        selectedWifiNetwork = new MutableLiveData<>(null);
        isLoading = new MutableLiveData<>(false);
    }

    public LiveData<Boolean> getIsBluetoothEnabled() {
        return isBluetoothEnabled;
    }

    public LiveData<List<WifiNetwork>> getWifiNetworks() {
        return wifiNetworks;
    }

    public LiveData<WifiNetwork> getSelectedWifiNetwork() {
        return selectedWifiNetwork;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @SuppressLint("CheckResult")
    public Completable scanWifi() {
        if (mac == null) {
            throw new RuntimeException("Target device's MAC address is not set");
        }
        wifiNetworks.setValue(new ArrayList<>());
        DeviceCommunicationHelper helper = new DeviceCommunicationHelper(mac);
        return new Completable() {
            @SuppressLint("CheckResult")
            @Override
            protected void subscribeActual(CompletableObserver s) {
                isLoading.postValue(true);
                helper
                        .connect()
                        .andThen(helper.getAvailableWifiNetworks())
                        .subscribe(ConnectWifiViewModel.this::addWifiNetwork, s::onError, s::onComplete);
            }
        }.doOnTerminate(() -> {
            isLoading.postValue(false);
            helper.disconnect();
        });
    }

    public void selectWifiNetWork(WifiNetwork wifiNetwork) {
        selectedWifiNetwork.setValue(wifiNetwork);
    }

    @SuppressLint("CheckResult")
    public Completable connectWifi(WifiNetwork wifiNetwork, String psk) {
        if (mac == null) {
            throw new RuntimeException("Target device's MAC address is not set");
        }
        DeviceCommunicationHelper helper = new DeviceCommunicationHelper(mac);
        return new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {
                isLoading.postValue(true);
                if (!wifiNetwork.isOpen() && psk == null) {
                    s.onError(new RuntimeException("Password is required for secured WiFi network"));
                    return;
                }
                String requestPsk = wifiNetwork.isOpen() ? null : psk;
                helper
                        .connect()
                        .andThen(helper.connectWifi(wifiNetwork.getSsid(), requestPsk))
                        .subscribe(s);
            }
        }.doOnTerminate(() -> {
            isLoading.postValue(false);
            helper.disconnect();
        });
    }

    private void addWifiNetwork(WifiNetwork wifiNetwork) {
        wifiNetworks.getValue().add(wifiNetwork);
        wifiNetworks.postValue(wifiNetworks.getValue());
    }
}
