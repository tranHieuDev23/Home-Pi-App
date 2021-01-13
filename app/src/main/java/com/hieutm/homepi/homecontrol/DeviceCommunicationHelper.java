package com.hieutm.homepi.homecontrol;

import android.annotation.SuppressLint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hieutm.homepi.bluetooth.BluetoothHelper;
import com.hieutm.homepi.bluetooth.BluetoothJsonCommunicationWrapper;
import com.hieutm.homepi.models.WifiNetwork;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;

public class DeviceCommunicationHelper {
    private static final BluetoothHelper BLUETOOTH_HELPER = BluetoothHelper.getInstance();
    private final String mac;
    private BluetoothJsonCommunicationWrapper communicationWrapper;

    public DeviceCommunicationHelper(@NotNull String mac) {
        this.mac = mac;
        this.communicationWrapper = null;
    }

    public Completable connect() {
        return new Completable() {
            @SuppressLint("CheckResult")
            @Override
            protected void subscribeActual(CompletableObserver s) {
                BLUETOOTH_HELPER.connect(mac).subscribe(((bluetoothCommunicationHandler, throwable) -> {
                    if (throwable != null) {
                        s.onError(throwable);
                        return;
                    }
                    communicationWrapper = new BluetoothJsonCommunicationWrapper(bluetoothCommunicationHandler);
                    s.onComplete();
                }));
            }
        };
    }

    public void disconnect() {
        if (communicationWrapper == null) {
            return;
        }
        communicationWrapper.disconnect();
    }

    public Single<Boolean> getWifiStatus() {
        return new Single<Boolean>() {
            @SuppressLint("CheckResult")
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super Boolean> observer) {
                JsonObject messageJson = new JsonObject();
                messageJson.addProperty("action", "wifiStatus");
                sendMessage(messageJson).subscribe((responseJson, throwable) -> {
                    if (throwable != null) {
                        observer.onError(throwable);
                        return;
                    }
                    boolean connected = responseJson.get("connected").getAsBoolean();
                    observer.onSuccess(connected);
                });
            }
        };
    }

    public Observable<WifiNetwork> getAvailableWifiNetworks() {
        return new Observable<WifiNetwork>() {
            @SuppressLint("CheckResult")
            @Override
            protected void subscribeActual(Observer<? super WifiNetwork> observer) {
                JsonObject messageJson = new JsonObject();
                messageJson.addProperty("action", "scanWifi");
                communicationWrapper.sendMessage(messageJson).subscribe((responseJson, throwable) -> {
                    if (throwable != null) {
                        observer.onError(throwable);
                        return;
                    }
                    JsonArray networks = responseJson.getAsJsonArray("networks");
                    for (JsonElement itemElement : networks) {
                        JsonObject item = itemElement.getAsJsonObject();
                        String ssid = item.get("ssid").getAsString();
                        boolean open = item.get("open").getAsBoolean();
                        observer.onNext(new WifiNetwork(ssid, open));
                    }
                    observer.onComplete();
                });
            }
        };
    }

    public Single<String> getDeviceId() {
        return new Single<String>() {
            @SuppressLint("CheckResult")
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super String> observer) {
                JsonObject messageJson = new JsonObject();
                messageJson.addProperty("action", "getId");
                communicationWrapper.sendMessage(messageJson).subscribe((responseJson, throwable) -> {
                    if (throwable != null) {
                        observer.onError(throwable);
                        return;
                    }
                    String deviceId = responseJson.get("deviceId").getAsString();
                    observer.onSuccess(deviceId);
                });
            }
        };
    }

    public Completable connectWifi(@NotNull String ssid, String psk) {
        return new Completable() {
            @SuppressLint("CheckResult")
            @Override
            protected void subscribeActual(CompletableObserver s) {
                JsonObject messageJson = new JsonObject();
                messageJson.addProperty("action", "connectWifi");
                messageJson.addProperty("ssid", ssid);
                messageJson.addProperty("psk", psk == null? "" : psk);
                sendMessage(messageJson).subscribe((response, throwable) -> {
                    if (throwable != null) {
                        s.onError(throwable);
                        return;
                    }
                    s.onComplete();
                });
            }
        };
    }

    public Completable registerDevice(@NotNull String token) {
        return new Completable() {
            @SuppressLint("CheckResult")
            @Override
            protected void subscribeActual(CompletableObserver s) {
                JsonObject messageJson = new JsonObject();
                messageJson.addProperty("action", "register");
                messageJson.addProperty("token", token);
                sendMessage(messageJson).subscribe((response, throwable) -> {
                   if (throwable != null) {
                       s.onError(throwable);
                       return;
                   }
                   s.onComplete();
                });
            }
        };
    }

    private Single<JsonObject> sendMessage(JsonObject message) {
        return new Single<JsonObject>() {
            @SuppressLint("CheckResult")
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super JsonObject> observer) {
                if (communicationWrapper == null) {
                    observer.onError(new DeviceCommunicationException("Not yet connected"));
                    return;
                }
                communicationWrapper.sendMessage(message, 30000).subscribe((response, throwable) -> {
                    if (throwable != null) {
                        observer.onError(throwable);
                        return;
                    }
                    boolean success = response.get("success").getAsBoolean();
                    if (!success) {
                        observer.onError(new DeviceCommunicationException("Request failed"));
                        return;
                    }
                    observer.onSuccess(response);
                });
            }
        };
    }
}
