package com.hieutm.homepi.ui.registerdevice;

import android.bluetooth.BluetoothDevice;

public class BluetoothDeviceListItem {
    private final BluetoothDevice device;
    private boolean isLoading;

    public BluetoothDeviceListItem(BluetoothDevice device, boolean isLoading) {
        this.device = device;
        this.isLoading = isLoading;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }
}
