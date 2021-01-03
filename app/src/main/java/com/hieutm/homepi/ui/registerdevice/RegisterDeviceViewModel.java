package com.hieutm.homepi.ui.registerdevice;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hieutm.homepi.bluetooth.BluetoothHelper;
import com.hieutm.homepi.data.Result;
import com.hieutm.homepi.data.model.Device;
import com.hieutm.homepi.data.model.DeviceType;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RegisterDeviceViewModel extends ViewModel {
    private static final BluetoothHelper BLUETOOTH_HELPER = BluetoothHelper.getInstance();

    private final MutableLiveData<Boolean> isBluetoothEnabled;
    private final MutableLiveData<List<BluetoothDeviceListItem>> devices;
    private final MutableLiveData<Boolean> isRegistering;

    public RegisterDeviceViewModel() {
        isBluetoothEnabled = new MutableLiveData<>(BLUETOOTH_HELPER.isBluetoothEnabled());
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
        List<BluetoothDevice> pairedDevices = BLUETOOTH_HELPER.getPairedDevices();
        this.devices.setValue(
                pairedDevices.stream()
                        .map(item -> new BluetoothDeviceListItem(item, false))
                        .collect(Collectors.toList())
        );
    }

    public void requestBluetooth(@NotNull Activity context) {
        isBluetoothEnabled.setValue(BLUETOOTH_HELPER.ensureBluetoothEnabled(context));
    }

    public void registerDevice(int position, Result.ResultHandler<Device> registerHandler) {
        BluetoothDeviceListItem item = devices.getValue().get(position);
        String mac = item.getDevice().getAddress();
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("action", "getId");
        } catch (JSONException e) {
            return;
        }
        setDeviceListItemRegistering(position, true);
        BLUETOOTH_HELPER.sendJsonMessage(mac, requestJson, 3, new Result.ResultHandler<JSONObject>() {
            @Override
            public void onSuccess(Result.Success<JSONObject> result) {
                setDeviceListItemRegistering(position, false);
                JSONObject response = result.getData();
                try {
                    String deviceId = response.getString("id");
                    Device device = new Device(deviceId, "New Light", DeviceType.LIGHT);
                    registerHandler.onSuccess(new Result.Success<>(device));
                } catch (JSONException e) {
                    registerHandler.onError(new Result.Error(new RuntimeException("No device id was found in response")));
                }
            }

            @Override
            public void onError(Result.Error error) {
                setDeviceListItemRegistering(position, false);
                registerHandler.onError(error);
            }
        });
    }

    private void setDeviceListItemRegistering(int position, boolean isLoading) {
        this.isRegistering.postValue(isLoading);
        List<BluetoothDeviceListItem> newList = devices.getValue();
        newList.get(position).setLoading(isLoading);
        devices.postValue(newList);
    }
}
