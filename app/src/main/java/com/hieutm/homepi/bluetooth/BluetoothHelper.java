package com.hieutm.homepi.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import com.harrysoft.androidbluetoothserial.BluetoothManager;
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;
import com.hieutm.homepi.data.Result;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BluetoothHelper {
    private BluetoothHelper() {
    }

    private static final BluetoothHelper INSTANCE = new BluetoothHelper();

    public static synchronized BluetoothHelper getInstance() {
        return INSTANCE;
    }

    private static final BluetoothManager BLUETOOTH_MANAGER = BluetoothManager.getInstance();

    public boolean isBluetoothEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return false;
        }
        return adapter.isEnabled();
    }

    public boolean ensureBluetoothEnabled(Activity context) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return false;
        }
        if (!adapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(intent, 0);
        }
        return adapter.isEnabled();
    }

    public List<BluetoothDevice> getPairedDevices() {
        return new ArrayList<>(BLUETOOTH_MANAGER.getPairedDevicesList());
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
                }, handler::onConnectionError);
    }

    public void disconnectDevice(String mac) {
        BLUETOOTH_MANAGER.closeDevice(mac);
    }

    public void sendJsonMessage(String mac, JSONObject message, int retry, Result.ResultHandler<JSONObject> responseHandler) {
        BluetoothJsonConnectionHandler handler = new BluetoothJsonConnectionHandler(
                message, retry, new Result.ResultHandler<JSONObject>() {
            @Override
            public void onSuccess(Result.Success<JSONObject> result) {
                responseHandler.onSuccess(result);
                disconnectDevice(mac);
            }

            @Override
            public void onError(Result.Error error) {
                responseHandler.onError(error);
                disconnectDevice(mac);
            }
        });
        connectDevice(mac, handler);
    }
}
