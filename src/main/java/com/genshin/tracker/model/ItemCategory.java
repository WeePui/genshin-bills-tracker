package com.genshin.tracker.model;

import java.math.BigDecimal;

public enum ItemCategory {
    WELKIN_MOON("Blessing of the Welkin Moon", new BigDecimal("4.99")),
    BATTLE_PASS_HYMN("Gnostic Hymn (Battle Pass)", new BigDecimal("9.99")),
    BATTLE_PASS_CHORUS("Gnostic Chorus (Battle Pass)", new BigDecimal("19.99")),
    BATTLE_PASS_UPGRADE("Gnostic Chorus Upgrade", new BigDecimal("11.99")),
    GENESIS_60("60 Genesis Crystals", new BigDecimal("0.99")),
    GENESIS_300("300+30 Genesis Crystals", new BigDecimal("4.99")),
    GENESIS_980("980+110 Genesis Crystals", new BigDecimal("14.99")),
    GENESIS_1980("1980+260 Genesis Crystals", new BigDecimal("29.99")),
    GENESIS_3280("3280+600 Genesis Crystals", new BigDecimal("49.99")),
    GENESIS_6480("6480+1600 Genesis Crystals", new BigDecimal("99.99")),
    CHARACTER_OUTFIT("Character Outfit / Skin", new BigDecimal("29.99")),
    BUNDLE("In-game Bundle / Pack", null),
    OTHER("Other / Custom", null);

    private final String displayName;
    private final BigDecimal defaultUsdPrice;

    ItemCategory(String displayName, BigDecimal defaultUsdPrice) {
        this.displayName = displayName;
        this.defaultUsdPrice = defaultUsdPrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getDefaultUsdPrice() {
        return defaultUsdPrice;
    }
}
