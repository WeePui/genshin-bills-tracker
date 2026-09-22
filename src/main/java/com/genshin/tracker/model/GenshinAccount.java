package com.genshin.tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "genshin_accounts")
public class GenshinAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "UID is required")
    @Column(nullable = false, unique = true, length = 32)
    private String uid;

    @NotBlank(message = "Nickname is required")
    @Column(nullable = false)
    private String nickname;

    @NotNull(message = "Server is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ServerRegion server;

    private LocalDate registrationDate;

    private String registrationDevice;

    private String registrationMethod;

    @Column(length = 2000)
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("purchaseDate DESC")
    private List<PurchaseBill> bills = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public GenshinAccount() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public ServerRegion getServer() { return server; }
    public void setServer(ServerRegion server) { this.server = server; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public String getRegistrationDevice() { return registrationDevice; }
    public void setRegistrationDevice(String registrationDevice) { this.registrationDevice = registrationDevice; }

    public String getRegistrationMethod() { return registrationMethod; }
    public void setRegistrationMethod(String registrationMethod) { this.registrationMethod = registrationMethod; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public List<PurchaseBill> getBills() { return bills; }
    public void setBills(List<PurchaseBill> bills) { this.bills = bills; }
}
