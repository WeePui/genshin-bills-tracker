package com.genshin.tracker.service;

import com.genshin.tracker.dto.ScanResultDTO;
import com.genshin.tracker.model.ItemCategory;
import com.genshin.tracker.model.PlatformStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceParserServiceTest {

    private InvoiceParserService parserService;

    @BeforeEach
    void setUp() {
        parserService = new InvoiceParserService();
    }

    @Test
    void testGooglePlayWelkinMoonReceipt() {
        String receiptText = """
                Google Play
                Thank you for your purchase from COGNOSPHERE PTE. LTD. on Google Play.
                Order number: GPA.3341-9876-5432-10987
                Order date: Sep 28, 2021 14:30:15 UTC
                Item: Blessing of the Welkin Moon (Genshin Impact)
                Price: $4.99
                Tax: $0.00
                Total: $4.99
                Payment method: Visa ...1234
                """;

        ScanResultDTO result = parserService.parseText(receiptText);

        assertTrue(result.isSuccess());
        assertEquals("GPA.3341-9876-5432-10987", result.getOrderId());
        assertEquals(PlatformStore.GOOGLE_PLAY, result.getPlatform());
        assertEquals(ItemCategory.WELKIN_MOON, result.getItemCategory());
        assertEquals(new BigDecimal("4.99"), result.getAmount());
        assertEquals("USD", result.getCurrency());
        assertEquals("Visa", result.getPaymentMethod());
        assertNotNull(result.getPurchaseDate());
        assertEquals(2021, result.getPurchaseDate().getYear());
        assertEquals(9, result.getPurchaseDate().getMonthValue());
        assertEquals(28, result.getPurchaseDate().getDayOfMonth());
    }

    @Test
    void testAppleBattlePassReceipt() {
        String receiptText = """
                Apple Receipt
                Order ID: MN9X87YWQ4
                Document No.: 123456789012
                Date: 15/10/2022
                Genshin Impact - Gnostic Hymn
                Total: $9.99
                Payment: Apple Pay (Mastercard)
                """;

        ScanResultDTO result = parserService.parseText(receiptText);

        assertTrue(result.isSuccess());
        assertEquals("MN9X87YWQ4", result.getOrderId());
        assertEquals(PlatformStore.APPLE_APP_STORE, result.getPlatform());
        assertEquals(ItemCategory.BATTLE_PASS_HYMN, result.getItemCategory());
        assertEquals(new BigDecimal("9.99"), result.getAmount());
        assertEquals("USD", result.getCurrency());
    }

    @Test
    void testPayPalVietnameseWelkinReceipt() {
        String receiptText = """
                COGNOSPHERE PTE. LTD.
                Transaction ID: 8AB12345CD67890EF
                Date: 2023-05-10
                Không Nguyệt Kỳ Chúc (Genshin Impact)
                Tổng cộng: 109.000 ₫
                Payment: PayPal
                """;

        ScanResultDTO result = parserService.parseText(receiptText);

        assertTrue(result.isSuccess());
        assertEquals("8AB12345CD67890EF", result.getOrderId());
        assertEquals(ItemCategory.WELKIN_MOON, result.getItemCategory());
        assertEquals("PayPal", result.getPaymentMethod());
        assertEquals(new BigDecimal("109000"), result.getAmount());
        assertEquals("VND", result.getCurrency());
    }

    @Test
    void testGenesisCrystals6480Pack() {
        String receiptText = """
                Google Play Order GPA.1111-2222-3333-44444
                Item: 6480 Genesis Crystals
                Total: $99.99
                Order date: 2024-01-01
                """;

        ScanResultDTO result = parserService.parseText(receiptText);

        assertTrue(result.isSuccess());
        assertEquals("GPA.1111-2222-3333-44444", result.getOrderId());
        assertEquals(ItemCategory.GENESIS_6480, result.getItemCategory());
        assertEquals(new BigDecimal("99.99"), result.getAmount());
    }
}
