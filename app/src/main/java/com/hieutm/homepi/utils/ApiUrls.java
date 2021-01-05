package com.hieutm.homepi.utils;

public class ApiUrls {
    private ApiUrls() {}

    private static final String BASE_URL = "https://e997502aa67c.ngrok.io";
    public static final String AUTH_VALIDATE_URL = BASE_URL + "/api/auth/validate";
    public static final String AUTH_LOGIN_URL = BASE_URL + "/api/auth/login";
    public static final String AUTH_LOGOUT_URL = BASE_URL + "/api/auth/logout";

    public static final String HOME_CONTROL_GET_COMMANDERS = BASE_URL + "/api/home-control/get-commanders";
    public static final String HOME_CONTROL_REGISTER_COMMANDER = BASE_URL + "/api/home-control/register-commander";
    public static final String HOME_CONTROL_GET_DEVICES = BASE_URL + "/api/home-control/get-devices";
    public static final String HOME_CONTROL_REGISTER_DEVICE = BASE_URL + "/api/home-control/register-device";
}
