package com.genshin.tracker.controller;

import com.genshin.tracker.dto.DashboardSummaryDTO;
import com.genshin.tracker.model.GenshinAccount;
import com.genshin.tracker.model.PurchaseBill;
import com.genshin.tracker.service.AccountService;
import com.genshin.tracker.service.AnalyticsService;
import com.genshin.tracker.service.BillService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DashboardController {

    private final AnalyticsService analyticsService;
    private final AccountService accountService;
    private final BillService billService;

    public DashboardController(AnalyticsService analyticsService, AccountService accountService, BillService billService) {
        this.analyticsService = analyticsService;
        this.accountService = accountService;
        this.billService = billService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "accountId", required = false) Long accountId, Model model) {
        List<GenshinAccount> accounts = accountService.getAllAccounts();
        DashboardSummaryDTO summary = analyticsService.getDashboardSummary(accountId);
        List<PurchaseBill> recentBills;

        if (accountId != null) {
            recentBills = billService.getBillsByAccount(accountId);
        } else {
            recentBills = billService.getAllBills();
        }

        if (recentBills.size() > 6) {
            recentBills = recentBills.subList(0, 6);
        }

        model.addAttribute("accounts", accounts);
        model.addAttribute("selectedAccountId", accountId);
        model.addAttribute("summary", summary);
        model.addAttribute("recentBills", recentBills);

        return "dashboard/index";
    }
}
