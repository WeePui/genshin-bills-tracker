package com.genshin.tracker.model;

public enum ServerRegion {
    ASIA("Asia (UID 8xx / 18xx)"),
    AMERICA("America (UID 6xx)"),
    EUROPE("Europe (UID 7xx)"),
    TW_HK_MO("TW / HK / MO (UID 9xx)");

    private final String displayName;

    ServerRegion(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
