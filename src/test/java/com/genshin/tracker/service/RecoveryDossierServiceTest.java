package com.genshin.tracker.service;

import com.genshin.tracker.model.GenshinAccount;
import com.genshin.tracker.model.ItemCategory;
import com.genshin.tracker.model.PlatformStore;
import com.genshin.tracker.model.PurchaseBill;
import com.genshin.tracker.model.ServerRegion;
import com.genshin.tracker.repository.GenshinAccountRepository;
import com.genshin.tracker.repository.PurchaseBillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RecoveryDossierServiceTest {

    private GenshinAccountRepository accountRepository;
    private PurchaseBillRepository billRepository;
    private StorageService storageService;
    private RecoveryDossierService dossierService;

    @BeforeEach
    void setUp() {
        accountRepository = Mockito.mock(GenshinAccountRepository.class);
        billRepository = Mockito.mock(PurchaseBillRepository.class);
        storageService = Mockito.mock(StorageService.class);
        dossierService = new RecoveryDossierService(accountRepository, billRepository, storageService);
    }

    @Test
    void testGenerateRecoveryTextContainsCriticalDetails() {
        GenshinAccount account = new GenshinAccount();
        account.setId(1L);
        account.setUid("812345678");
        account.setNickname("AetherTraveller");
        account.setServer(ServerRegion.ASIA);
        account.setRegistrationDate(LocalDate.of(2020, 9, 28));
        account.setRegistrationDevice("PC Windows 10");
        account.setRegistrationMethod("HoYoverse Account");

        PurchaseBill firstBill = new PurchaseBill();
        firstBill.setId(10L);
        firstBill.setAccount(account);
        firstBill.setOrderId("GPA.3341-9999-8888-77777");
        firstBill.setPurchaseDate(LocalDateTime.of(2020, 10, 5, 12, 0));
        firstBill.setPlatform(PlatformStore.GOOGLE_PLAY);
        firstBill.setPaymentMethod("Visa");
        firstBill.setItemCategory(ItemCategory.WELKIN_MOON);
        firstBill.setAmount(new BigDecimal("4.99"));
        firstBill.setCurrency("USD");
        firstBill.setFirstPurchase(true);

        when(billRepository.findFirstByAccountIdAndIsFirstPurchaseTrue(1L)).thenReturn(Optional.of(firstBill));
        when(billRepository.findByAccountIdOrderByPurchaseDateDesc(1L)).thenReturn(List.of(firstBill));

        String text = dossierService.generateRecoveryText(account);

        assertNotNull(text);
        assertTrue(text.contains("812345678"));
        assertTrue(text.contains("AetherTraveller"));
        assertTrue(text.contains("Asia"));
        assertTrue(text.contains("GPA.3341-9999-8888-77777"));
        assertTrue(text.contains("2020-09-28"));
        assertTrue(text.contains("Blessing of the Welkin Moon"));
    }
}
