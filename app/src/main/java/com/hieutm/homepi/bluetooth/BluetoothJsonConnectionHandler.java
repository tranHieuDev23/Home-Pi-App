package com.hieutm.homepi.bluetooth;

import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.hieutm.homepi.data.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class BluetoothJsonConnectionHandler implements BluetoothConnectionHandler {
    private final JSONObject requestJson;
    private final int retryLimit;
    private final Result.ResultHandler<JSONObject> resultHandler;

    private BluetoothSerialDevice connectedDevice;

    public BluetoothJsonConnectionHandler(JSONObject requestJson, int retryLimit, Result.ResultHandler<JSONObject> resultHandler) {
        this.requestJson = requestJson;
        this.retryLimit = retryLimit;
        this.resultHandler = resultHandler;
    }

    @Override
    public void onConnected(BluetoothSerialDevice connectedDevice) {
        this.connectedDevice = connectedDevice;
        makeRequest();
    }

    @Override
    public void onConnectionError(Throwable error) {
        this.resultHandler.onError(new Result.Error(new RuntimeException("Failed to connect to device")));
    }

    @Override
    public void onMessageSent(String message) {}

    @Override
    public void onMessageReceived(String message) {
        JSONObject responseJson;
        try {
            responseJson = new JSONObject(message);
            String reqId = responseJson.getString("reqId");
            if (!this.reqId.equals(reqId)) {
                return;
            }
        } catch (JSONException e) {
            onError(e);
            return;
        }
        resultHandler.onSuccess(new Result.Success<>(responseJson));
    }

    private int retryCount = 0;

    @Override
    public void onError(Throwable error) {
        retryCount++;
        if (retryCount >= retryLimit) {
            resultHandler.onError(new Result.Error(new RuntimeException("Out of retries")));
            return;
        }
        makeRequest();
    }

    private String reqId;

    private void makeRequest() {
        try {
            JSONObject requestJsonWithId = new JSONObject(requestJson.toString());
            this.reqId = UUID.randomUUID().toString();
            requestJsonWithId.put("reqId", reqId);
            String requestStr = requestJsonWithId.toString();
            connectedDevice.send(requestStr);
        } catch (Exception e) {
            onError(new Exception("Failed to generate request json string"));
        }
    }
}
