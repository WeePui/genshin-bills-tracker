package com.genshin.tracker.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "app_settings")
public class AppSettings {

    @Id
    private Long id = 1L;

    @Column(nullable = false, length = 8)
    private String baseCurrency = "USD";

    @Column(length = 256)
    private String geminiApiKey;

    // Exchange rates relative to USD (1 USD = X Currency)
    private BigDecimal usdToVndRate = new BigDecimal("25400");
    private BigDecimal usdToEurRate = new BigDecimal("0.92");
    private BigDecimal usdToJpyRate = new BigDecimal("155");

    @Column(length = 16)
    private String backgroundMode = "RANDOM"; // "RANDOM" or "STATIC"

    @Column(length = 64)
    private String staticBackground = "background_1.png";

    public AppSettings() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }

    public String getGeminiApiKey() { return geminiApiKey; }
    public void setGeminiApiKey(String geminiApiKey) { this.geminiApiKey = geminiApiKey; }

    public BigDecimal getUsdToVndRate() { return usdToVndRate; }
    public void setUsdToVndRate(BigDecimal usdToVndRate) { this.usdToVndRate = usdToVndRate; }

    public BigDecimal getUsdToEurRate() { return usdToEurRate; }
    public void setUsdToEurRate(BigDecimal usdToEurRate) { this.usdToEurRate = usdToEurRate; }

    public BigDecimal getUsdToJpyRate() { return usdToJpyRate; }
    public void setUsdToJpyRate(BigDecimal usdToJpyRate) { this.usdToJpyRate = usdToJpyRate; }

    public String getBackgroundMode() {
        return (backgroundMode != null && !backgroundMode.isBlank()) ? backgroundMode : "RANDOM";
    }
    public void setBackgroundMode(String backgroundMode) { this.backgroundMode = backgroundMode; }

    public String getStaticBackground() {
        return (staticBackground != null && !staticBackground.isBlank()) ? staticBackground : "background_1.png";
    }
    public void setStaticBackground(String staticBackground) { this.staticBackground = staticBackground; }
}
