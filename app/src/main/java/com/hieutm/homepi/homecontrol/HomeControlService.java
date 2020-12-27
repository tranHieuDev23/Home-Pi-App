package com.hieutm.homepi.homecontrol;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.hieutm.homepi.data.Result;
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

    public void getCommandersOfUser(Result.ResultHandler<List<Commander>> handler) {
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
                            commanders.add(new Commander(id, displayName));
                        }
                        handler.onSuccess(new Result.Success<>(commanders));
                    } catch (JSONException e) {
                        handler.onError(new Result.Error(e));
                    }
                },
                error -> handler.onError(new Result.Error(error)));
        requestQueue.add(request);
    }

    public void registerCommander(@NotNull String commanderId, Result.ResultHandler<Commander> handler) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("commanderId", commanderId);
        } catch (Exception e) {
            handler.onError(new Result.Error(e));
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
                        handler.onSuccess(new Result.Success<>(new Commander(id, displayName)));
                    } catch (Exception e) {
                        handler.onError(new Result.Error(e));
                    }
                },
                error -> handler.onError(new Result.Error(error))
        );
        requestQueue.add(request);
    }

    public void getDevicesOfUser(Result.ResultHandler<List<Device>> handler) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiUrls.HOME_CONTROL_GET_DEVICES,
                null,
                response -> {
                    try {
                        JSONArray commandersJson = response.getJSONArray("devices");
                        List<Device> devices = new ArrayList<>();
                        for (int i = 0; i < commandersJson.length(); i++) {
                            JSONObject item = commandersJson.getJSONObject(i);
                            String id = item.getString("id");
                            String displayName = item.getString("displayName");
                            DeviceType type = DeviceType.valueOf(item.getString("type"));
                            devices.add(new Device(id, displayName, type));
                        }
                        handler.onSuccess(new Result.Success<>(devices));
                    } catch (JSONException e) {
                        handler.onError(new Result.Error(e));
                    }
                },
                error -> handler.onError(new Result.Error(error)));
        requestQueue.add(request);
    }

    public void registerDevice(@NotNull String deviceId, Result.ResultHandler<Device> handler) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("deviceId", deviceId);
        } catch (Exception e) {
            handler.onError(new Result.Error(e));
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ApiUrls.HOME_CONTROL_REGISTER_DEVICE,
                requestBody,
                response -> {
                    try {
                        JSONObject deviceJson = response.getJSONObject("commander");
                        String id = deviceJson.getString("id");
                        String displayName = deviceJson.getString("displayName");
                        DeviceType type = DeviceType.valueOf(deviceJson.getString("type"));
                        handler.onSuccess(new Result.Success<>(new Device(id, displayName, type)));
                    } catch (Exception e) {
                        handler.onError(new Result.Error(e));
                    }
                },
                error -> handler.onError(new Result.Error(error))
        );
        requestQueue.add(request);
    }
}
