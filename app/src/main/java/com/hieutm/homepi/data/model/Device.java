package com.hieutm.homepi.data.model;

public class Device {
    private String id;
    private String displayName;
    private DeviceType type;

    public Device(String id, String displayName, DeviceType type) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }
}
