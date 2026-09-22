package com.genshin.tracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebMvcTemplateRenderTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testDashboardRenders() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    void testBillsListRenders() throws Exception {
        mockMvc.perform(get("/bills"))
                .andExpect(status().isOk());
    }

    @Test
    void testNewBillFormRenders() throws Exception {
        mockMvc.perform(get("/bills/new"))
                .andExpect(status().isOk());
    }

    @Test
    void testAccountsListRenders() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk());
    }

    @Test
    void testNewAccountFormRenders() throws Exception {
        mockMvc.perform(get("/accounts/new"))
                .andExpect(status().isOk());
    }

    @Test
    void testRecoveryCenterRenders() throws Exception {
        mockMvc.perform(get("/recovery"))
                .andExpect(status().isOk());
    }

    @Test
    void testSettingsRenders() throws Exception {
        mockMvc.perform(get("/settings"))
                .andExpect(status().isOk());
    }
}
