package com.hieutm.homepi.bluetooth;

import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;

public interface BluetoothConnectionHandler {
    void onConnected(BluetoothSerialDevice connectedDevice);
    void onConnectionError(Throwable error);
    void onMessageSent(String message);
    void onMessageReceived(String message);
    void onError(Throwable error);
}
