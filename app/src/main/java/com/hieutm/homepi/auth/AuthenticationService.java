package com.hieutm.homepi.auth;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.hieutm.homepi.models.LoggedInUser;
import com.hieutm.homepi.utils.ApiUrls;
import com.hieutm.homepi.utils.PersistentCookieStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;

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

    public Maybe<LoggedInUser> getCurrentUser() {
        return new Maybe<LoggedInUser>() {
            @Override
            protected void subscribeActual(MaybeObserver<? super LoggedInUser> observer) {
                if (currentUser != null) {
                    observer.onSuccess(currentUser);
                    return;
                }
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ApiUrls.AUTH_VALIDATE_URL, null, response -> {
                    if (!response.has("username") || !response.has("displayName")) {
                        observer.onComplete();
                        return;
                    }
                    try {
                        String username = response.getString("username");
                        String displayName = response.getString("displayName");
                        currentUser = new LoggedInUser(username, displayName);
                        observer.onSuccess(currentUser);
                    } catch (JSONException e) {
                        // Ignored because exceptions cannot happen
                    }
                }, observer::onError);
                requestQueue.add(request);
            }
        };
    }

    public Maybe<LoggedInUser> logIn(String username, String password) {
        return new Maybe<LoggedInUser>() {
            @Override
            protected void subscribeActual(MaybeObserver<? super LoggedInUser> observer) {
                if (currentUser != null) {
                    observer.onError(new RuntimeException("Already logged in"));
                    return;
                }
                JSONObject requestBody = new JSONObject();
                try {
                    requestBody.put("username", username);
                    requestBody.put("password", password);
                } catch (JSONException e) {
                    observer.onError(e);
                    return;
                }
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ApiUrls.AUTH_LOGIN_URL, requestBody, response -> {
                    try {
                        String displayName = response.getString("displayName");
                        currentUser = new LoggedInUser(username, displayName);
                        observer.onSuccess(currentUser);
                    } catch (JSONException e) {
                        observer.onError(e);
                    }
                }, observer::onError);
                requestQueue.add(request);
            }
        };
    }

    public Completable logOut() {
        return new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {
                if (currentUser == null) {
                    s.onError(new RuntimeException("Not logged in"));
                    return;
                }
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ApiUrls.AUTH_LOGOUT_URL, null, response -> {
                    currentUser = null;
                    s.onComplete();
                }, s::onError);
                requestQueue.add(request);
            }
        };
    }
}
