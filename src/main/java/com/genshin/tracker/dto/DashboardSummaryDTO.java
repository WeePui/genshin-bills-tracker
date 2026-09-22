package com.genshin.tracker.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardSummaryDTO {

    private BigDecimal totalSpend = BigDecimal.ZERO;
    private BigDecimal thisMonthSpend = BigDecimal.ZERO;
    private long totalBillsCount = 0;
    private long welkinCount = 0;
    private long battlePassCount = 0;
    private long crystalsCount = 0;
    private String baseCurrency = "USD";

    // Chart.js data
    private List<String> monthlyLabels = new ArrayList<>();
    private List<BigDecimal> monthlyValues = new ArrayList<>();

    private List<String> categoryLabels = new ArrayList<>();
    private List<BigDecimal> categoryValues = new ArrayList<>();

    private List<String> platformLabels = new ArrayList<>();
    private List<BigDecimal> platformValues = new ArrayList<>();

    public DashboardSummaryDTO() {}

    public BigDecimal getTotalSpend() { return totalSpend; }
    public void setTotalSpend(BigDecimal totalSpend) { this.totalSpend = totalSpend; }

    public BigDecimal getThisMonthSpend() { return thisMonthSpend; }
    public void setThisMonthSpend(BigDecimal thisMonthSpend) { this.thisMonthSpend = thisMonthSpend; }

    public long getTotalBillsCount() { return totalBillsCount; }
    public void setTotalBillsCount(long totalBillsCount) { this.totalBillsCount = totalBillsCount; }

    public long getWelkinCount() { return welkinCount; }
    public void setWelkinCount(long welkinCount) { this.welkinCount = welkinCount; }

    public long getBattlePassCount() { return battlePassCount; }
    public void setBattlePassCount(long battlePassCount) { this.battlePassCount = battlePassCount; }

    public long getCrystalsCount() { return crystalsCount; }
    public void setCrystalsCount(long crystalsCount) { this.crystalsCount = crystalsCount; }

    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }

    public List<String> getMonthlyLabels() { return monthlyLabels; }
    public void setMonthlyLabels(List<String> monthlyLabels) { this.monthlyLabels = monthlyLabels; }

    public List<BigDecimal> getMonthlyValues() { return monthlyValues; }
    public void setMonthlyValues(List<BigDecimal> monthlyValues) { this.monthlyValues = monthlyValues; }

    public List<String> getCategoryLabels() { return categoryLabels; }
    public void setCategoryLabels(List<String> categoryLabels) { this.categoryLabels = categoryLabels; }

    public List<BigDecimal> getCategoryValues() { return categoryValues; }
    public void setCategoryValues(List<BigDecimal> categoryValues) { this.categoryValues = categoryValues; }

    public List<String> getPlatformLabels() { return platformLabels; }
    public void setPlatformLabels(List<String> platformLabels) { this.platformLabels = platformLabels; }

    public List<BigDecimal> getPlatformValues() { return platformValues; }
    public void setPlatformValues(List<BigDecimal> platformValues) { this.platformValues = platformValues; }
}
