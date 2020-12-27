package com.hieutm.homepi.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import com.harrysoft.androidbluetoothserial.BluetoothManager;
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BluetoothHelper {
    private BluetoothHelper() {}

    private static final BluetoothHelper INSTANCE = new BluetoothHelper();

    public static synchronized BluetoothHelper getInstance() {
        return INSTANCE;
    }

    private static final BluetoothManager BLUETOOTH_MANAGER = BluetoothManager.getInstance();

    public boolean ensureBluetoothEnabled(Activity context) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(intent, 0);
        }
        return adapter.isEnabled();
    }

    public List<BluetoothDevice> getPairedDevices() {
        return BLUETOOTH_MANAGER.getPairedDevicesList();
    }

    public interface BluetoothConnectionHandler {
        void onConnected(BluetoothSerialDevice connectedDevice);
        void onMessageSent(String message);
        void onMessageReceived(String message);
        void onError(Throwable error);
    }

    @SuppressLint("CheckResult")
    public void connectDevice(String mac, BluetoothConnectionHandler handler) {
        BLUETOOTH_MANAGER.openSerialDevice(mac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((BluetoothSerialDevice connectedDevice) -> {
                    handler.onConnected(connectedDevice);
                    SimpleBluetoothDeviceInterface deviceInterface = connectedDevice.toSimpleDeviceInterface();
                    deviceInterface.setListeners(handler::onMessageReceived, handler::onMessageSent, handler::onError);
                }, handler::onError);
    }

    public void disconnectDevice(String mac) {
        BLUETOOTH_MANAGER.closeDevice(mac);
    }
}
