package com.hieutm.homepi.models;

public class WifiNetwork {
    private final String ssid;
    private final boolean isOpen;

    public WifiNetwork(String ssid, boolean isOpen) {
        this.ssid = ssid;
        this.isOpen = isOpen;
    }

    public String getSsid() {
        return ssid;
    }

    public boolean isOpen() {
        return isOpen;
    }
}
