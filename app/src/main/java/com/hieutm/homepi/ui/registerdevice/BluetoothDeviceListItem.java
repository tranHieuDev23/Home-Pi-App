package com.hieutm.homepi.ui.registerdevice;

import android.bluetooth.BluetoothDevice;

public class BluetoothDeviceListItem {
    private final BluetoothDevice device;

    public BluetoothDeviceListItem(BluetoothDevice device, boolean isLoading) {
        this.device = device;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

}
