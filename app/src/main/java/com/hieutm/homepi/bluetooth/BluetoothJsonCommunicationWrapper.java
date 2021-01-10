package com.hieutm.homepi.bluetooth;

import android.annotation.SuppressLint;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;

public class BluetoothJsonCommunicationWrapper {
    private final BluetoothHelper.BluetoothCommunicationHandler handler;

    public BluetoothJsonCommunicationWrapper(BluetoothHelper.BluetoothCommunicationHandler handler) {
        this.handler = handler;
    }

    public Single<JsonObject> sendMessage(JsonObject message) {
        return sendMessage(message, BluetoothHelper.BluetoothCommunicationHandler.DEFAULT_TIMEOUT, BluetoothHelper.BluetoothCommunicationHandler.DEFAULT_BUFFER_SIZE);
    }

    public Single<JsonObject> sendMessage(JsonObject message, long timeout) {
        return sendMessage(message, timeout, BluetoothHelper.BluetoothCommunicationHandler.DEFAULT_BUFFER_SIZE);
    }

    public Single<JsonObject> sendMessage(JsonObject message, long timeout, int bufferSize) {
        return new Single<JsonObject>() {
            @SuppressLint("CheckResult")
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super JsonObject> observer) {
                handler.sendMessage(message.toString(), timeout, bufferSize).subscribe(response -> {
                    try {
                        response = response.trim();
                        JsonObject responseJson = new Gson().fromJson(response, JsonObject.class);
                        observer.onSuccess(responseJson);
                    } catch (JsonSyntaxException e) {
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
