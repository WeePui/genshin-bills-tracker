package com.genshin.tracker.controller;

import com.genshin.tracker.model.GenshinAccount;
import com.genshin.tracker.model.PurchaseBill;
import com.genshin.tracker.service.AccountService;
import com.genshin.tracker.service.BillService;
import com.genshin.tracker.service.RecoveryDossierService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/recovery")
public class RecoveryController {

    private final AccountService accountService;
    private final BillService billService;
    private final RecoveryDossierService recoveryDossierService;

    public RecoveryController(AccountService accountService,
                              BillService billService,
                              RecoveryDossierService recoveryDossierService) {
        this.accountService = accountService;
        this.billService = billService;
        this.recoveryDossierService = recoveryDossierService;
    }

    @GetMapping
    public String recoveryDashboard(@RequestParam(value = "accountId", required = false) Long accountId, Model model) {
        List<GenshinAccount> accounts = accountService.getAllAccounts();
        model.addAttribute("accounts", accounts);

        if (accountId == null && !accounts.isEmpty()) {
            accountId = accounts.get(0).getId();
        }

        model.addAttribute("selectedAccountId", accountId);

        if (accountId != null) {
            Optional<GenshinAccount> accountOpt = accountService.getAccountById(accountId);
            if (accountOpt.isPresent()) {
                GenshinAccount account = accountOpt.get();
                model.addAttribute("account", account);
                model.addAttribute("recoveryText", recoveryDossierService.generateRecoveryText(account));

                Optional<PurchaseBill> firstPurchase = billService.getFirstPurchase(accountId);
                model.addAttribute("firstPurchase", firstPurchase.orElse(null));

                List<PurchaseBill> allBills = billService.getBillsByAccount(accountId);
                model.addAttribute("recentBills", allBills.size() > 5 ? allBills.subList(0, 5) : allBills);
                model.addAttribute("totalBillsCount", allBills.size());
            }
        }

        return "recovery/index";
    }

    @GetMapping("/{accountId}/download-zip")
    public ResponseEntity<byte[]> downloadRecoveryZip(@PathVariable Long accountId) throws IOException {
        GenshinAccount account = accountService.getAccountById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        byte[] zipContent = recoveryDossierService.generateRecoveryZip(accountId);
        String filename = String.format("HoYoverse_Recovery_Dossier_UID_%s.zip", account.getUid());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zipContent);
    }
}
