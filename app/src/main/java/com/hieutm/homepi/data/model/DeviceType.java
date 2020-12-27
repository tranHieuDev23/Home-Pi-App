package com.hieutm.homepi.data.model;

import java.util.HashMap;
import java.util.Map;

public enum DeviceType {
    LIGHT,
    THERMOSTAT;

    private static final Map<DeviceType, String> HUMAN_READABLE_STRING;

    static {
        HUMAN_READABLE_STRING = new HashMap<>();
        HUMAN_READABLE_STRING.put(LIGHT, "Light");
        HUMAN_READABLE_STRING.put(THERMOSTAT, "Thermostat");
    }

    public String getHumanReadableString() {
        return HUMAN_READABLE_STRING.get(this);
    }
}
