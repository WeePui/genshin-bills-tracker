package com.genshin.tracker.service;

import com.genshin.tracker.model.BillAttachment;
import com.genshin.tracker.model.GenshinAccount;
import com.genshin.tracker.model.PurchaseBill;
import com.genshin.tracker.repository.GenshinAccountRepository;
import com.genshin.tracker.repository.PurchaseBillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional(readOnly = true)
public class RecoveryDossierService {

    private final GenshinAccountRepository accountRepository;
    private final PurchaseBillRepository billRepository;
    private final StorageService storageService;

    public RecoveryDossierService(GenshinAccountRepository accountRepository,
                                  PurchaseBillRepository billRepository,
                                  StorageService storageService) {
        this.accountRepository = accountRepository;
        this.billRepository = billRepository;
        this.storageService = storageService;
    }

    public String generateRecoveryText(GenshinAccount account) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        sb.append("=================================================================\n");
        sb.append("     HOYOVERSE ACCOUNT RETRIEVAL APPLICATION FORM DOSSIER        \n");
        sb.append("=================================================================\n\n");

        sb.append("1. ACCOUNT IDENTIFIERS:\n");
        sb.append("   - UID: ").append(account.getUid()).append("\n");
        sb.append("   - Server: ").append(account.getServer() != null ? account.getServer().getDisplayName() : "Unknown").append("\n");
        sb.append("   - In-game Nickname: ").append(account.getNickname()).append("\n");
        sb.append("   - Registration Date: ").append(account.getRegistrationDate() != null ? account.getRegistrationDate().format(df) : "Not recorded").append("\n");
        sb.append("   - Registration Device: ").append(account.getRegistrationDevice() != null ? account.getRegistrationDevice() : "Not recorded").append("\n");
        sb.append("   - Registration Method: ").append(account.getRegistrationMethod() != null ? account.getRegistrationMethod() : "Not recorded").append("\n\n");

        // First purchase
        Optional<PurchaseBill> firstBillOpt = billRepository.findFirstByAccountIdAndIsFirstPurchaseTrue(account.getId());
        if (firstBillOpt.isEmpty()) {
            firstBillOpt = billRepository.findFirstByAccountIdOrderByPurchaseDateAsc(account.getId());
        }

        sb.append("2. FIRST PURCHASE INFORMATION (CRITICAL FOR ACCOUNT RETRIEVAL):\n");
        if (firstBillOpt.isPresent()) {
            PurchaseBill first = firstBillOpt.get();
            sb.append("   - First Purchase Date: ").append(first.getPurchaseDate().format(dtf)).append("\n");
            sb.append("   - Payment Method: ").append(first.getPaymentMethod() != null ? first.getPaymentMethod() : "N/A").append("\n");
            sb.append("   - Payment Channel / Platform: ").append(first.getPlatform() != null ? first.getPlatform().getDisplayName() : "N/A").append("\n");
            sb.append("   - Order / Transaction ID: ").append(first.getOrderId() != null ? first.getOrderId() : "N/A").append("\n");
            sb.append("   - Item Purchased: ").append(first.getDisplayItemName()).append("\n");
            sb.append("   - Amount: ").append(first.getAmount()).append(" ").append(first.getCurrency()).append("\n");
            sb.append("   - Receipt Attached: ").append(!first.getAttachments().isEmpty() ? "YES (" + first.getAttachments().size() + " files in dossier)" : "NO").append("\n");
        } else {
            sb.append("   - No purchase bills recorded yet.\n");
        }
        sb.append("\n");

        // Recent purchases
        List<PurchaseBill> allBills = billRepository.findByAccountIdOrderByPurchaseDateDesc(account.getId());
        sb.append("3. RECENT PURCHASE HISTORY (LAST 5 TRANSACTIONS):\n");
        int count = 0;
        for (PurchaseBill bill : allBills) {
            if (count++ >= 5) break;
            sb.append(String.format("   [%d] %s | %s | %s %s | ID: %s | Platform: %s\n",
                    count,
                    bill.getPurchaseDate().format(df),
                    bill.getDisplayItemName(),
                    bill.getAmount(),
                    bill.getCurrency(),
                    bill.getOrderId() != null ? bill.getOrderId() : "N/A",
                    bill.getPlatform() != null ? bill.getPlatform().getDisplayName() : "N/A"));
        }
        sb.append("\n");

        if (account.getNotes() != null && !account.getNotes().isBlank()) {
            sb.append("4. ADDITIONAL ACCOUNT NOTES:\n");
            sb.append("   ").append(account.getNotes().replace("\n", "\n   ")).append("\n\n");
        }

        sb.append("Generated by Genshin Bills Tracker.\n");
        return sb.toString();
    }

    public byte[] generateRecoveryZip(Long accountId) throws IOException {
        GenshinAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 1. Add Text Dossier
            String textInfo = generateRecoveryText(account);
            ZipEntry textEntry = new ZipEntry("hoyoverse_recovery_info.txt");
            zos.putNextEntry(textEntry);
            zos.write(textInfo.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 2. Add First Purchase Receipts
            Optional<PurchaseBill> firstBillOpt = billRepository.findFirstByAccountIdAndIsFirstPurchaseTrue(account.getId());
            if (firstBillOpt.isEmpty()) {
                firstBillOpt = billRepository.findFirstByAccountIdOrderByPurchaseDateAsc(account.getId());
            }

            int index = 1;
            if (firstBillOpt.isPresent()) {
                PurchaseBill first = firstBillOpt.get();
                for (BillAttachment att : first.getAttachments()) {
                    Path filePath = storageService.getFilePath(att.getStoredFileName());
                    if (Files.exists(filePath)) {
                        String ext = getFileExtension(att.getOriginalFileName());
                        String cleanOrderId = first.getOrderId() != null ? first.getOrderId().replaceAll("[^a-zA-Z0-9.-]", "_") : "FIRST";
                        String zipFileName = String.format("receipts/01_FIRST_PURCHASE_%s%s", cleanOrderId, ext);
                        addFileToZip(zos, filePath, zipFileName);
                    }
                }
            }

            // 3. Add Recent 5 Receipts
            List<PurchaseBill> allBills = billRepository.findByAccountIdOrderByPurchaseDateDesc(account.getId());
            for (PurchaseBill bill : allBills) {
                if (firstBillOpt.isPresent() && bill.getId().equals(firstBillOpt.get().getId())) {
                    continue; // Already added as first purchase
                }
                if (index > 5) break;

                for (BillAttachment att : bill.getAttachments()) {
                    Path filePath = storageService.getFilePath(att.getStoredFileName());
                    if (Files.exists(filePath)) {
                        index++;
                        String ext = getFileExtension(att.getOriginalFileName());
                        String dateStr = bill.getPurchaseDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        String zipFileName = String.format("receipts/%02d_RECENT_%s_%s%s", index, dateStr, bill.getDisplayItemName().replaceAll("[^a-zA-Z0-9]", "_"), ext);
                        addFileToZip(zos, filePath, zipFileName);
                    }
                }
            }
        }

        return baos.toByteArray();
    }

    private void addFileToZip(ZipOutputStream zos, Path filePath, String entryName) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        Files.copy(filePath, zos);
        zos.closeEntry();
    }

    private String getFileExtension(String filename) {
        if (filename == null) return ".png";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".png";
    }
}
