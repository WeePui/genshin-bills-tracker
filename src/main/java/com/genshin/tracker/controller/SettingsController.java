package com.genshin.tracker.controller;

import com.genshin.tracker.model.AppSettings;
import com.genshin.tracker.repository.AppSettingsRepository;
import com.genshin.tracker.service.BackupService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final AppSettingsRepository settingsRepository;
    private final BackupService backupService;

    public SettingsController(AppSettingsRepository settingsRepository, BackupService backupService) {
        this.settingsRepository = settingsRepository;
        this.backupService = backupService;
    }

    @GetMapping
    public String viewSettings(Model model) {
        AppSettings settings = settingsRepository.findById(1L).orElseGet(() -> {
            AppSettings s = new AppSettings();
            return settingsRepository.save(s);
        });

        model.addAttribute("settings", settings);
        return "settings/index";
    }

    @PostMapping("/save")
    public String saveSettings(@ModelAttribute AppSettings settings, RedirectAttributes redirectAttributes) {
        settings.setId(1L);
        settingsRepository.save(settings);
        redirectAttributes.addFlashAttribute("successMessage", "Settings updated successfully!");
        return "redirect:/settings";
    }

    @GetMapping("/backup/download")
    public ResponseEntity<byte[]> downloadBackup() throws IOException {
        byte[] backupZip = backupService.createFullBackupZip();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "Genshin_Bills_Backup_" + timestamp + ".zip";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(backupZip);
    }

    @PostMapping("/backup/restore")
    public String restoreBackup(@RequestParam("backupFile") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            backupService.restoreFromBackupZip(file);
            redirectAttributes.addFlashAttribute("successMessage", "Receipt files restored from backup successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to restore backup: " + e.getMessage());
        }
        return "redirect:/settings";
    }
}
