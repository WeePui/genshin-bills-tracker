package com.genshin.tracker.controller;

import com.genshin.tracker.model.GenshinAccount;
import com.genshin.tracker.model.ServerRegion;
import com.genshin.tracker.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public String listAccounts(Model model) {
        model.addAttribute("accounts", accountService.getAllAccounts());
        return "accounts/list";
    }

    @GetMapping("/new")
    public String newAccountForm(Model model) {
        model.addAttribute("account", new GenshinAccount());
        model.addAttribute("servers", ServerRegion.values());
        model.addAttribute("isNew", true);
        return "accounts/form";
    }

    @GetMapping("/{id}/edit")
    public String editAccountForm(@PathVariable Long id, Model model) {
        GenshinAccount account = accountService.getAccountById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid account ID: " + id));
        model.addAttribute("account", account);
        model.addAttribute("servers", ServerRegion.values());
        model.addAttribute("isNew", false);
        return "accounts/form";
    }

    @PostMapping("/save")
    public String saveAccount(@ModelAttribute GenshinAccount account, RedirectAttributes redirectAttributes) {
        try {
            accountService.saveAccount(account);
            redirectAttributes.addFlashAttribute("successMessage", "Account saved successfully!");
            return "redirect:/accounts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving account: " + e.getMessage());
            return "redirect:/accounts/new";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        accountService.deleteAccount(id);
        redirectAttributes.addFlashAttribute("successMessage", "Account and related bills removed.");
        return "redirect:/accounts";
    }
}
