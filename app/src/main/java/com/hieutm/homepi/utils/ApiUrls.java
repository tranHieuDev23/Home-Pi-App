package com.hieutm.homepi.utils;

public class ApiUrls {
    private ApiUrls() {}

    private static final String BASE_URL = "https://f3fb91e883df.ngrok.io";
    public static final String AUTH_VALIDATE_URL = BASE_URL + "/api/auth/validate";
    public static final String AUTH_LOGIN_URL = BASE_URL + "/api/auth/login";
    public static final String AUTH_SIGN_UP_URL = BASE_URL + "/api/auth/register";
    public static final String AUTH_LOGOUT_URL = BASE_URL + "/api/auth/logout";

    public static final String HOME_CONTROL_GET_COMMANDERS = BASE_URL + "/api/home-control/get-commanders";
    public static final String HOME_CONTROL_CHECK_COMMANDER_OWNERSHIP = BASE_URL + "/api/home-control/check-commander-ownership";
    public static final String HOME_CONTROL_REGISTER_COMMANDER = BASE_URL + "/api/home-control/register-commander";
    public static final String HOME_CONTROL_UNREGISTER_COMMANDER = BASE_URL + "/api/home-control/unregister-commander";
    public static final String HOME_CONTROL_GET_DEVICES = BASE_URL + "/api/home-control/get-devices";
    public static final String HOME_CONTROL_CHECK_DEVICE_OWNERSHIP = BASE_URL + "/api/home-control/check-device-ownership";
    public static final String HOME_CONTROL_REGISTER_DEVICE = BASE_URL + "/api/home-control/register-device";
    public static final String HOME_CONTROL_UNREGISTER_DEVICE = BASE_URL + "/api/home-control/unregister-device";
}
