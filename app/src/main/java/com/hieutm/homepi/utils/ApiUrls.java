package com.hieutm.homepi.utils;

public class ApiUrls {
    private ApiUrls() {}

    private static final String BASE_URL = "https://06f15522c7aa.ngrok.io";
    public static final String AUTH_VALIDATE_URL = BASE_URL + "/api/auth/validate";
    public static final String AUTH_LOGIN_URL = BASE_URL + "/api/auth/login";
    public static final String AUTH_LOGOUT_URL = BASE_URL + "/api/auth/logout";
}
