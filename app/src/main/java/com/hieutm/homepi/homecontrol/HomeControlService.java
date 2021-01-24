package com.hieutm.homepi.homecontrol;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.hieutm.homepi.models.Commander;
import com.hieutm.homepi.models.Device;
import com.hieutm.homepi.models.DeviceType;
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

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
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

    public Single<Boolean> checkCommanderOwnership(String commanderId) {
        return new Single<Boolean>() {
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super Boolean> observer) {
                JSONObject requestBody = new JSONObject();
                try {
                    requestBody.put("commanderId", commanderId);
                } catch (Exception e) {
                    observer.onError(e);
                    return;
                }
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        ApiUrls.HOME_CONTROL_CHECK_COMMANDER_OWNERSHIP,
                        requestBody,
                        response -> {
                            try {
                                boolean isRegistered = response.getBoolean("isRegistered");
                                observer.onSuccess(isRegistered);
                            } catch (JSONException e) {
                                observer.onError(e);
                            }
                        }, observer::onError);
                requestQueue.add(request);

            }
        };
    }

    public static class CommanderRegistrationResponse {
        private final Commander commander;
        private final String token;

        public CommanderRegistrationResponse(Commander commander, String token) {
            this.commander = commander;
            this.token = token;
        }

        public Commander getCommander() {
            return commander;
        }

        public String getToken() {
            return token;
        }
    }

    public Single<CommanderRegistrationResponse> registerCommander(@NotNull String commanderId) {
        return new Single<CommanderRegistrationResponse>() {
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super CommanderRegistrationResponse> observer) {
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
                                String token = response.getString("token");
                                observer.onSuccess(new CommanderRegistrationResponse(new Commander(id, displayName), token));
                            } catch (Exception e) {
                                observer.onError(e);
                            }
                        }, observer::onError);
                requestQueue.add(request);
            }
        };
    }

    public Completable unregisterCommander(@NotNull String commanderId) {
        return new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {
                JSONObject requestBody = new JSONObject();
                try {
                    requestBody.put("commanderId", commanderId);
                } catch (Exception e) {
                    s.onError(e);
                    return;
                }
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        ApiUrls.HOME_CONTROL_UNREGISTER_COMMANDER,
                        requestBody,
                        response -> s.onComplete(),
                        s::onError
                );
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

    public Single<Boolean> checkDeviceOwnership(String deviceId) {
        return new Single<Boolean>() {
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super Boolean> observer) {
                JSONObject requestBody = new JSONObject();
                try {
                    requestBody.put("deviceId", deviceId);
                } catch (Exception e) {
                    observer.onError(e);
                    return;
                }
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        ApiUrls.HOME_CONTROL_CHECK_DEVICE_OWNERSHIP,
                        requestBody,
                        response -> {
                            try {
                                boolean isRegistered = response.getBoolean("isRegistered");
                                observer.onSuccess(isRegistered);
                            } catch (JSONException e) {
                                observer.onError(e);
                            }
                        }, observer::onError);
                requestQueue.add(request);

            }
        };
    }

    public static class DeviceRegistrationResponse {
        private final Device device;
        private final String token;

        public DeviceRegistrationResponse(Device device, String token) {
            this.device = device;
            this.token = token;
        }

        public Device getDevice() {
            return device;
        }

        public String getToken() {
            return token;
        }
    }

    public Single<DeviceRegistrationResponse> registerDevice(@NotNull String deviceId) {
        return new Single<DeviceRegistrationResponse>() {
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super DeviceRegistrationResponse> observer) {
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
                                String token = response.getString("token");
                                observer.onSuccess(new DeviceRegistrationResponse(new Device(id, displayName, type), token));
                            } catch (Exception e) {
                                observer.onError(e);
                            }
                        }, observer::onError);
                requestQueue.add(request);
            }
        };
    }

    public Completable unregisterDevice(@NotNull String deviceId) {
        return new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {
                JSONObject requestBody = new JSONObject();
                try {
                    requestBody.put("deviceId", deviceId);
                } catch (Exception e) {
                    s.onError(e);
                    return;
                }
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        ApiUrls.HOME_CONTROL_UNREGISTER_DEVICE,
                        requestBody,
                        response -> s.onComplete(),
                        s::onError
                );
                requestQueue.add(request);
            }
        };
    }
}
