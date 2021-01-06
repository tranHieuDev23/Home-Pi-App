package com.hieutm.homepi.homecontrol;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.hieutm.homepi.data.model.Commander;
import com.hieutm.homepi.data.model.Device;
import com.hieutm.homepi.data.model.DeviceType;
import com.hieutm.homepi.utils.ApiUrls;
import com.hieutm.homepi.utils.PersistentCookieStore;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;

public class HomeControlService {
    private final RequestQueue requestQueue;

    private HomeControlService(Context context) {
        CookieManager cookieManager = new CookieManager(PersistentCookieStore.getInstance(context), CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(cookieManager);
        requestQueue = Volley.newRequestQueue(context);
    }

    private static volatile HomeControlService INSTANCE = null;

    public static HomeControlService getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new HomeControlService(context);
        }
        return INSTANCE;
    }

    public Observable<Commander> getCommandersOfUser() {
        return new Observable<Commander>() {
            @Override
            protected void subscribeActual(Observer<? super Commander> observer) {
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        ApiUrls.HOME_CONTROL_GET_COMMANDERS,
                        null,
                        response -> {
                            try {
                                JSONArray commandersJson = response.getJSONArray("commanders");
                                List<Commander> commanders = new ArrayList<>();
                                for (int i = 0; i < commandersJson.length(); i++) {
                                    JSONObject item = commandersJson.getJSONObject(i);
                                    String id = item.getString("id");
                                    String displayName = item.getString("displayName");
                                    observer.onNext(new Commander(id, displayName));
                                }
                                observer.onComplete();
                            } catch (JSONException e) {
                                observer.onError(e);
                            }
                        }, observer::onError);
                requestQueue.add(request);
            }
        };
    }

    public Single<Commander> registerCommander(@NotNull String commanderId) {
        return new Single<Commander>() {
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super Commander> observer) {
                JSONObject requestBody = new JSONObject();
                try {
                    requestBody.put("commanderId", commanderId);
                } catch (Exception e) {
                    observer.onError(e);
                    return;
                }
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        ApiUrls.HOME_CONTROL_REGISTER_COMMANDER,
                        requestBody,
                        response -> {
                            try {
                                JSONObject commanderJson = response.getJSONObject("commander");
                                String id = commanderJson.getString("id");
                                String displayName = commanderJson.getString("displayName");
                                observer.onSuccess(new Commander(id, displayName));
                            } catch (Exception e) {
                                observer.onError(e);
                            }
                        }, observer::onError);
                requestQueue.add(request);
            }
        };
    }

    public Observable<Device> getDevicesOfUser() {
        return new Observable<Device>() {
            @Override
            protected void subscribeActual(Observer<? super Device> observer) {
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        ApiUrls.HOME_CONTROL_GET_DEVICES,
                        null,
                        response -> {
                            try {
                                JSONArray devicesJson = response.getJSONArray("devices");
                                for (int i = 0; i < devicesJson.length(); i++) {
                                    JSONObject item = devicesJson.getJSONObject(i);
                                    String id = item.getString("id");
                                    String displayName = item.getString("displayName");
                                    DeviceType type = DeviceType.valueOf(item.getString("type"));
                                    observer.onNext(new Device(id, displayName, type));
                                }
                                observer.onComplete();
                            } catch (JSONException e) {
                                observer.onError(e);
                            }
                        }, observer::onError);
                requestQueue.add(request);
            }
        };
    }


    public Single<Device> registerDevice(@NotNull String deviceId) {
        return new Single<Device>() {
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super Device> observer) {
                JSONObject requestBody = new JSONObject();
                try {
                    requestBody.put("deviceId", deviceId);
                } catch (Exception e) {
                    observer.onError(e);
                    return;
                }
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        ApiUrls.HOME_CONTROL_REGISTER_DEVICE,
                        requestBody,
                        response -> {
                            try {
                                JSONObject deviceJson = response.getJSONObject("device");
                                String id = deviceJson.getString("id");
                                String displayName = deviceJson.getString("displayName");
                                DeviceType type = DeviceType.valueOf(deviceJson.getString("type"));
                                observer.onSuccess(new Device(id, displayName, type));
                            } catch (Exception e) {
                                observer.onError(e);
                            }
                        }, observer::onError);
                requestQueue.add(request);
            }
        };
    }
}
