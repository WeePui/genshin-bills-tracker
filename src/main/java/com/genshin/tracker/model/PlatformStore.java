package com.genshin.tracker.model;

public enum PlatformStore {
    GOOGLE_PLAY("Google Play Store"),
    APPLE_APP_STORE("Apple App Store"),
    PC_WORLDPAY("PC Client (Card / Worldpay)"),
    CODASHOP("Codashop"),
    RAZER_GOLD("Razer Gold"),
    EPIC_GAMES("Epic Games Store"),
    PLAYSTATION_NETWORK("PlayStation Network"),
    OTHER("Other Platform");

    private final String displayName;

    PlatformStore(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
