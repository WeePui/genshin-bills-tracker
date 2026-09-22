package com.genshin.tracker.dto;

import com.genshin.tracker.model.ItemCategory;
import com.genshin.tracker.model.PlatformStore;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScanResultDTO {

    private boolean success;
    private String orderId;
    private LocalDateTime purchaseDate;
    private ItemCategory itemCategory;
    private String customItemName;
    private PlatformStore platform;
    private String paymentMethod;
    private BigDecimal amount;
    private String currency;
    private String rawText;
    private String message;
    private List<String> detectedFields = new ArrayList<>();

    public ScanResultDTO() {}

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    public ItemCategory getItemCategory() { return itemCategory; }
    public void setItemCategory(ItemCategory itemCategory) { this.itemCategory = itemCategory; }

    public String getCustomItemName() { return customItemName; }
    public void setCustomItemName(String customItemName) { this.customItemName = customItemName; }

    public PlatformStore getPlatform() { return platform; }
    public void setPlatform(PlatformStore platform) { this.platform = platform; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getDetectedFields() { return detectedFields; }
    public void setDetectedFields(List<String> detectedFields) { this.detectedFields = detectedFields; }

    public void addDetectedField(String fieldName) {
        if (!detectedFields.contains(fieldName)) {
            detectedFields.add(fieldName);
        }
    }
}
