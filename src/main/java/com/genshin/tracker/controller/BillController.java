package com.genshin.tracker.controller;

import com.genshin.tracker.model.*;
import com.genshin.tracker.service.AccountService;
import com.genshin.tracker.service.BillService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/bills")
public class BillController {

    private final BillService billService;
    private final AccountService accountService;

    public BillController(BillService billService, AccountService accountService) {
        this.billService = billService;
        this.accountService = accountService;
    }

    @GetMapping
    public String listBills(
            @RequestParam(value = "accountId", required = false) Long accountId,
            @RequestParam(value = "category", required = false) ItemCategory category,
            @RequestParam(value = "platform", required = false) PlatformStore platform,
            @RequestParam(value = "search", required = false) String search,
            Model model) {

        List<PurchaseBill> bills = billService.filterBills(accountId, category, platform, search);
        model.addAttribute("bills", bills);
        model.addAttribute("accounts", accountService.getAllAccounts());
        model.addAttribute("categories", ItemCategory.values());
        model.addAttribute("platforms", PlatformStore.values());
        model.addAttribute("selectedAccountId", accountId);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedPlatform", platform);
        model.addAttribute("search", search);

        return "bills/list";
    }

    @GetMapping("/new")
    public String newBillForm(@RequestParam(value = "accountId", required = false) Long accountId, Model model) {
        PurchaseBill bill = new PurchaseBill();
        bill.setPurchaseDate(LocalDateTime.now());
        bill.setCurrency("USD");
        bill.setPlatform(PlatformStore.GOOGLE_PLAY);
        bill.setItemCategory(ItemCategory.WELKIN_MOON);

        if (accountId != null) {
            accountService.getAccountById(accountId).ifPresent(bill::setAccount);
        }

        model.addAttribute("bill", bill);
        model.addAttribute("accounts", accountService.getAllAccounts());
        model.addAttribute("categories", ItemCategory.values());
        model.addAttribute("platforms", PlatformStore.values());
        model.addAttribute("isNew", true);

        return "bills/form";
    }

    @GetMapping("/{id}/edit")
    public String editBillForm(@PathVariable Long id, Model model) {
        PurchaseBill bill = billService.getBillById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid bill ID: " + id));

        model.addAttribute("bill", bill);
        model.addAttribute("accounts", accountService.getAllAccounts());
        model.addAttribute("categories", ItemCategory.values());
        model.addAttribute("platforms", PlatformStore.values());
        model.addAttribute("isNew", false);

        return "bills/form";
    }

    @GetMapping("/{id}")
    public String viewBillDetail(@PathVariable Long id, Model model) {
        PurchaseBill bill = billService.getBillById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid bill ID: " + id));

        model.addAttribute("bill", bill);
        return "bills/detail";
    }

    @PostMapping("/save")
    public String saveBill(
            @ModelAttribute PurchaseBill bill,
            @RequestParam("accountId") Long accountId,
            @RequestParam(value = "purchaseDateStr", required = false) String purchaseDateStr,
            @RequestParam(value = "receiptFiles", required = false) List<MultipartFile> files,
            RedirectAttributes redirectAttributes) {

        try {
            GenshinAccount account = accountService.getAccountById(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
            bill.setAccount(account);

            if (purchaseDateStr != null && !purchaseDateStr.isBlank()) {
                if (purchaseDateStr.length() == 10) {
                    bill.setPurchaseDate(LocalDateTime.parse(purchaseDateStr + "T12:00:00"));
                } else if (purchaseDateStr.length() == 16) {
                    bill.setPurchaseDate(LocalDateTime.parse(purchaseDateStr + ":00"));
                } else {
                    bill.setPurchaseDate(LocalDateTime.parse(purchaseDateStr));
                }
            } else if (bill.getPurchaseDate() == null) {
                bill.setPurchaseDate(LocalDateTime.now());
            }

            PurchaseBill saved = billService.saveBill(bill, files);
            redirectAttributes.addFlashAttribute("successMessage", "Bill saved successfully!");
            return "redirect:/bills/" + saved.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving bill: " + e.getMessage());
            return "redirect:/bills/new";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteBill(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        billService.deleteBill(id);
        redirectAttributes.addFlashAttribute("successMessage", "Bill deleted successfully.");
        return "redirect:/bills";
    }

    @PostMapping("/{billId}/attachments/{attachmentId}/delete")
    public String deleteAttachment(
            @PathVariable Long billId,
            @PathVariable Long attachmentId,
            RedirectAttributes redirectAttributes) {
        billService.deleteAttachment(attachmentId);
        redirectAttributes.addFlashAttribute("successMessage", "Receipt attachment removed.");
        return "redirect:/bills/" + billId;
    }

    @PostMapping("/{billId}/attachments/{attachmentId}/primary")
    public String setPrimaryAttachment(
            @PathVariable Long billId,
            @PathVariable Long attachmentId,
            RedirectAttributes redirectAttributes) {
        billService.setPrimaryAttachment(attachmentId);
        redirectAttributes.addFlashAttribute("successMessage", "Marked as primary receipt.");
        return "redirect:/bills/" + billId;
    }
}
