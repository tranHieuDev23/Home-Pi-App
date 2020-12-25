package com.hieutm.homepi.auth;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.hieutm.homepi.data.Result;
import com.hieutm.homepi.data.model.LoggedInUser;
import com.hieutm.homepi.utils.ApiUrls;
import com.hieutm.homepi.utils.PersistentCookieStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class AuthenticationService {
    private final RequestQueue requestQueue;

    private AuthenticationService(Context context) {
        CookieManager cookieManager = new CookieManager(PersistentCookieStore.getInstance(context), CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(cookieManager);
        requestQueue = Volley.newRequestQueue(context);
    }

    private static volatile AuthenticationService INSTANCE = null;

    public static AuthenticationService getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AuthenticationService(context);
        }
        return INSTANCE;
    }

    private LoggedInUser currentUser = null;

    public void getCurrentUser(Result.ResultHandler<LoggedInUser> handler) {
        if (currentUser != null) {
            handler.onSuccess(new Result.Success<>(currentUser));
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ApiUrls.AUTH_VALIDATE_URL, null, response -> {
            if (!response.has("username") || !response.has("displayName")) {
                handler.onSuccess(new Result.Success<>(null));
                return;
            }
            try {
                String username = response.getString("username");
                String displayName = response.getString("displayName");
                currentUser = new LoggedInUser(username, displayName);
                handler.onSuccess(new Result.Success<>(currentUser));
            } catch (JSONException e) {
                // Ignored because exceptions cannot happen
            }
        }, error -> {
            Log.e(AuthenticationService.class.getName(), error.getMessage());
            handler.onError(new Result.Error(new Exception("Cannot validate current user")));
        });
        requestQueue.add(request);
    }

    public void logIn(String username, String password, Result.ResultHandler<LoggedInUser> handler) {
        if (currentUser != null) {
            handler.onError(new Result.Error(new RuntimeException("Already logged in")));
            return;
        }
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", username);
            requestBody.put("password", password);
        } catch (JSONException e) {
            handler.onError(new Result.Error(e));
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ApiUrls.AUTH_LOGIN_URL, requestBody, response -> {
            try {
                String displayName = response.getString("displayName");
                currentUser = new LoggedInUser(username, displayName);
                handler.onSuccess(new Result.Success<>(currentUser));
            } catch (JSONException e) {
                handler.onError(new Result.Error(e));
            }
        }, error -> handler.onError(new Result.Error(error)));
        requestQueue.add(request);
    }

    public void logOut(Result.ResultHandler<Void> handler) {
        if (currentUser == null) {
            handler.onError(new Result.Error(new RuntimeException("Not logged in")));
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ApiUrls.AUTH_LOGOUT_URL, null, response -> {
            currentUser = null;
            handler.onSuccess(new Result.Success<>(null));
        }, error -> {
            handler.onError(new Result.Error(error));
        });
        requestQueue.add(request);
    }
}
