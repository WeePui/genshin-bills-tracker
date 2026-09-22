package com.genshin.tracker.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_bills")
public class PurchaseBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Account is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private GenshinAccount account;

    @Column(length = 128)
    private String orderId;

    @NotNull(message = "Purchase date is required")
    @Column(nullable = false)
    private LocalDateTime purchaseDate;

    @NotNull(message = "Item category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private ItemCategory itemCategory;

    private String customItemName;

    @NotNull(message = "Platform is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private PlatformStore platform;

    private String paymentMethod;

    @NotNull(message = "Amount is required")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 8)
    private String currency = "USD";

    @Column(precision = 12, scale = 2)
    private BigDecimal baseAmount;

    private boolean isFirstPurchase = false;

    @Column(length = 2000)
    private String notes;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<BillAttachment> attachments = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.baseAmount == null) {
            this.baseAmount = this.amount;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.baseAmount == null) {
            this.baseAmount = this.amount;
        }
    }

    public PurchaseBill() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GenshinAccount getAccount() { return account; }
    public void setAccount(GenshinAccount account) { this.account = account; }

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

    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }

    public boolean isFirstPurchase() { return isFirstPurchase; }
    public void setFirstPurchase(boolean firstPurchase) { isFirstPurchase = firstPurchase; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<BillAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<BillAttachment> attachments) { this.attachments = attachments; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public String getDisplayItemName() {
        if (itemCategory == ItemCategory.OTHER && customItemName != null && !customItemName.isBlank()) {
            return customItemName;
        }
        return itemCategory != null ? itemCategory.getDisplayName() : "Unknown";
    }

    public void addAttachment(BillAttachment attachment) {
        attachments.add(attachment);
        attachment.setBill(this);
    }
}
