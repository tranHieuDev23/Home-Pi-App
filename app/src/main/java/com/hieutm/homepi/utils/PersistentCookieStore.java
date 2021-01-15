package com.hieutm.homepi.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

public class PersistentCookieStore implements CookieStore {
    private static final String COOKIE_PREFERENCES_FILE = "COOKIE_PREFERENCES_FILE";
    private static final String SESSION_COOKIE_KEY = "SessionCookie";
    private static final String SESSION_COOKIE_NAME = "HomePiAuth";

    private final SharedPreferences cookiePrefs;
    private final CookieStore baseCookieStore;

    private PersistentCookieStore(Context context) {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFERENCES_FILE, 0);
        this.baseCookieStore = new CookieManager().getCookieStore();
        String sessionCookieValue = cookiePrefs.getString(SESSION_COOKIE_KEY, null);
        if (sessionCookieValue != null) {
            Gson gson = new Gson();
            HttpCookie sessionCookie = gson.fromJson(sessionCookieValue, HttpCookie.class);
            this.baseCookieStore.add(URI.create(sessionCookie.getDomain()), sessionCookie);
        }
    }

    private static volatile PersistentCookieStore INSTANCE = null;

    public static PersistentCookieStore getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new PersistentCookieStore(context);
        }
        return INSTANCE;
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {
        baseCookieStore.add(uri, cookie);
        if (cookie.getName().equals(SESSION_COOKIE_NAME)) {
            saveSessionCookie(cookie);
        }
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        return baseCookieStore.get(uri);
    }

    @Override
    public List<HttpCookie> getCookies() {
        return baseCookieStore.getCookies();
    }

    @Override
    public List<URI> getURIs() {
        return baseCookieStore.getURIs();
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        if (cookie.getName().equals(SESSION_COOKIE_NAME)) {
            deleteSessionCookie();
        }
        return baseCookieStore.remove(uri, cookie);
    }

    @Override
    public boolean removeAll() {
        deleteSessionCookie();
        return baseCookieStore.removeAll();
    }

    private void saveSessionCookie(HttpCookie cookie) {
        Gson gson = new Gson();
        String sessionCookieValue = gson.toJson(cookie);
        cookiePrefs.edit().putString(SESSION_COOKIE_KEY, sessionCookieValue).apply();
    }

    private void deleteSessionCookie() {
        cookiePrefs.edit().remove(SESSION_COOKIE_KEY).apply();
    }
}
