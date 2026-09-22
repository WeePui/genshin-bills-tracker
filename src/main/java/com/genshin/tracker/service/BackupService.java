package com.genshin.tracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.genshin.tracker.model.AppSettings;
import com.genshin.tracker.model.BillAttachment;
import com.genshin.tracker.model.GenshinAccount;
import com.genshin.tracker.model.PurchaseBill;
import com.genshin.tracker.repository.AppSettingsRepository;
import com.genshin.tracker.repository.BillAttachmentRepository;
import com.genshin.tracker.repository.GenshinAccountRepository;
import com.genshin.tracker.repository.PurchaseBillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class BackupService {

    private final GenshinAccountRepository accountRepository;
    private final PurchaseBillRepository billRepository;
    private final BillAttachmentRepository attachmentRepository;
    private final AppSettingsRepository settingsRepository;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    public BackupService(GenshinAccountRepository accountRepository,
                         PurchaseBillRepository billRepository,
                         BillAttachmentRepository attachmentRepository,
                         AppSettingsRepository settingsRepository,
                         StorageService storageService) {
        this.accountRepository = accountRepository;
        this.billRepository = billRepository;
        this.attachmentRepository = attachmentRepository;
        this.settingsRepository = settingsRepository;
        this.storageService = storageService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional(readOnly = true)
    public byte[] createFullBackupZip() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 1. Export Accounts
            List<GenshinAccount> accounts = accountRepository.findAll();
            String accountsJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(accounts);
            zos.putNextEntry(new ZipEntry("accounts.json"));
            zos.write(accountsJson.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 2. Export Bills
            List<PurchaseBill> bills = billRepository.findAll();
            String billsJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bills);
            zos.putNextEntry(new ZipEntry("bills.json"));
            zos.write(billsJson.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 3. Export Settings
            AppSettings settings = settingsRepository.findById(1L).orElse(new AppSettings());
            String settingsJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(settings);
            zos.putNextEntry(new ZipEntry("settings.json"));
            zos.write(settingsJson.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 4. Export Physical Receipts
            List<BillAttachment> attachments = attachmentRepository.findAll();
            for (BillAttachment att : attachments) {
                Path filePath = storageService.getFilePath(att.getStoredFileName());
                if (Files.exists(filePath)) {
                    ZipEntry fileEntry = new ZipEntry("receipts/" + att.getStoredFileName());
                    zos.putNextEntry(fileEntry);
                    Files.copy(filePath, zos);
                    zos.closeEntry();
                }
            }
        }

        return baos.toByteArray();
    }

    @Transactional
    public void restoreFromBackupZip(MultipartFile zipFile) throws IOException {
        try (InputStream is = zipFile.getInputStream();
             ZipInputStream zis = new ZipInputStream(is)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().startsWith("receipts/") && !entry.isDirectory()) {
                    String filename = entry.getName().substring("receipts/".length());
                    Path target = storageService.getFilePath(filename);
                    Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }
}
