package com.hieutm.homepi.bluetooth;

import android.annotation.SuppressLint;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;

public class BluetoothJsonCommunicationWrapper {
    private final BluetoothHelper.BluetoothCommunicationHandler handler;

    public BluetoothJsonCommunicationWrapper(BluetoothHelper.BluetoothCommunicationHandler handler) {
        this.handler = handler;
    }

    public Single<JSONObject> sendMessage(JSONObject message) {
        return sendMessage(message, BluetoothHelper.BluetoothCommunicationHandler.DEFAULT_TIMEOUT, BluetoothHelper.BluetoothCommunicationHandler.DEFAULT_BUFFER_SIZE);
    }

    public Single<JSONObject> sendMessage(JSONObject message, long timeout) {
        return sendMessage(message, timeout, BluetoothHelper.BluetoothCommunicationHandler.DEFAULT_BUFFER_SIZE);
    }

    public Single<JSONObject> sendMessage(JSONObject message, long timeout, int bufferSize) {
        return new Single<JSONObject>() {
            @SuppressLint("CheckResult")
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super JSONObject> observer) {
                handler.sendMessage(message.toString(), timeout, bufferSize).subscribe(response -> {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        observer.onSuccess(responseJson);
                    } catch (JSONException e) {
                        observer.onError(new RuntimeException("Response is not a valid JSON string", e));
                    }
                }, observer::onError);
            }
        };
    }

    public void disconnect() {
        this.handler.disconnect();
    }
}
