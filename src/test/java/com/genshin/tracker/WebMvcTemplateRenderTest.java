package com.genshin.tracker;

import com.genshin.tracker.model.*;
import com.genshin.tracker.repository.AppSettingsRepository;
import com.genshin.tracker.repository.GenshinAccountRepository;
import com.genshin.tracker.repository.PurchaseBillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebMvcTemplateRenderTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GenshinAccountRepository accountRepository;

    @Autowired
    private PurchaseBillRepository billRepository;

    @Autowired
    private AppSettingsRepository settingsRepository;

    @BeforeEach
    void seedData() {
        billRepository.deleteAll();
        accountRepository.deleteAll();

        GenshinAccount account = new GenshinAccount();
        account.setUid("800123456");
        account.setNickname("Aether");
        account.setServer(ServerRegion.ASIA);
        account.setRegistrationDate(LocalDate.of(2020, 9, 28));
        account.setRegistrationDevice("PC Windows");
        account.setRegistrationMethod("HoYoverse Account");
        GenshinAccount savedAccount = accountRepository.save(account);

        PurchaseBill bill = new PurchaseBill();
        bill.setAccount(savedAccount);
        bill.setOrderId("GPA.3341-1111-2222-33333");
        bill.setItemCategory(ItemCategory.WELKIN_MOON);
        bill.setPlatform(PlatformStore.GOOGLE_PLAY);
        bill.setAmount(new BigDecimal("4.99"));
        bill.setCurrency("USD");
        bill.setPurchaseDate(LocalDateTime.now());
        bill.setFirstPurchase(true);

        BillAttachment att = new BillAttachment();
        att.setBill(bill);
        att.setOriginalFileName("receipt.png");
        att.setStoredFileName("test-uuid.png");
        att.setContentType("image/png");
        att.setFileSize(1024L);
        bill.getAttachments().add(att);

        billRepository.save(bill);
    }

    @Test
    void testDashboardRendersWithData() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    void testBillsListRendersWithData() throws Exception {
        mockMvc.perform(get("/bills"))
                .andExpect(status().isOk());
    }

    @Test
    void testNewBillFormRenders() throws Exception {
        mockMvc.perform(get("/bills/new"))
                .andExpect(status().isOk());
    }

    @Test
    void testAccountsListRendersWithData() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk());
    }

    @Test
    void testNewAccountFormRenders() throws Exception {
        mockMvc.perform(get("/accounts/new"))
                .andExpect(status().isOk());
    }

    @Test
    void testRecoveryCenterRendersWithData() throws Exception {
        mockMvc.perform(get("/recovery"))
                .andExpect(status().isOk());
    }

    @Test
    void testSettingsRenders() throws Exception {
        mockMvc.perform(get("/settings"))
                .andExpect(status().isOk());
    }

    @Test
    void testSaveSettingsWithStaticBackground() throws Exception {
        mockMvc.perform(post("/settings/save")
                        .param("baseCurrency", "USD")
                        .param("backgroundMode", "STATIC")
                        .param("staticBackground", "background_5.png")
                        .param("usdToVndRate", "25400")
                        .param("usdToEurRate", "0.92")
                        .param("usdToJpyRate", "155"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings"));

        AppSettings settings = settingsRepository.findById(1L).orElseThrow();
        assertEquals("STATIC", settings.getBackgroundMode());
        assertEquals("background_5.png", settings.getStaticBackground());

        // Now test that page renders with the static background
        mockMvc.perform(get("/settings"))
                .andExpect(status().isOk());
    }

    @Test
    void testSaveSettingsWithRandomBackground() throws Exception {
        mockMvc.perform(post("/settings/save")
                        .param("baseCurrency", "USD")
                        .param("backgroundMode", "RANDOM")
                        .param("usdToVndRate", "25400")
                        .param("usdToEurRate", "0.92")
                        .param("usdToJpyRate", "155"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings"));

        AppSettings settings = settingsRepository.findById(1L).orElseThrow();
        assertEquals("RANDOM", settings.getBackgroundMode());
    }
}
