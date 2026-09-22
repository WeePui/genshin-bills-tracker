package com.genshin.tracker.service;

import com.genshin.tracker.model.*;
import com.genshin.tracker.repository.AppSettingsRepository;
import com.genshin.tracker.repository.BillAttachmentRepository;
import com.genshin.tracker.repository.PurchaseBillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BillServiceTest {

    private PurchaseBillRepository billRepository;
    private BillAttachmentRepository attachmentRepository;
    private AppSettingsRepository settingsRepository;
    private StorageService storageService;
    private BillService billService;

    @BeforeEach
    void setUp() {
        billRepository = Mockito.mock(PurchaseBillRepository.class);
        attachmentRepository = Mockito.mock(BillAttachmentRepository.class);
        settingsRepository = Mockito.mock(AppSettingsRepository.class);
        storageService = Mockito.mock(StorageService.class);

        billService = new BillService(billRepository, attachmentRepository, settingsRepository, storageService);

        AppSettings defaultSettings = new AppSettings();
        defaultSettings.setUsdToVndRate(new BigDecimal("25000"));
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(defaultSettings));
    }

    @Test
    void testSaveBillWithCurrencyNormalization() throws IOException {
        GenshinAccount account = new GenshinAccount();
        account.setId(1L);

        PurchaseBill bill = new PurchaseBill();
        bill.setAccount(account);
        bill.setAmount(new BigDecimal("50000"));
        bill.setCurrency("VND");
        bill.setItemCategory(ItemCategory.WELKIN_MOON);
        bill.setPlatform(PlatformStore.GOOGLE_PLAY);
        bill.setPurchaseDate(LocalDateTime.now());

        when(billRepository.save(any(PurchaseBill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseBill saved = billService.saveBill(bill, Collections.emptyList());

        assertNotNull(saved);
        assertEquals(new BigDecimal("2.00"), saved.getBaseAmount());
        verify(billRepository, times(1)).save(bill);
    }

    @Test
    void testSingleFirstPurchasePerAccount() throws IOException {
        GenshinAccount account = new GenshinAccount();
        account.setId(1L);

        PurchaseBill oldFirst = new PurchaseBill();
        oldFirst.setId(10L);
        oldFirst.setAccount(account);
        oldFirst.setFirstPurchase(true);

        when(billRepository.findByAccountIdOrderByPurchaseDateDesc(1L)).thenReturn(List.of(oldFirst));
        when(billRepository.save(any(PurchaseBill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseBill newFirst = new PurchaseBill();
        newFirst.setId(20L);
        newFirst.setAccount(account);
        newFirst.setAmount(new BigDecimal("4.99"));
        newFirst.setCurrency("USD");
        newFirst.setFirstPurchase(true);

        billService.saveBill(newFirst, Collections.emptyList());

        assertFalse(oldFirst.isFirstPurchase());
        verify(billRepository, atLeastOnce()).save(oldFirst);
    }
}
