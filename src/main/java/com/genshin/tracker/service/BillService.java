package com.genshin.tracker.service;

import com.genshin.tracker.model.*;
import com.genshin.tracker.repository.AppSettingsRepository;
import com.genshin.tracker.repository.BillAttachmentRepository;
import com.genshin.tracker.repository.PurchaseBillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BillService {

    private final PurchaseBillRepository billRepository;
    private final BillAttachmentRepository attachmentRepository;
    private final AppSettingsRepository settingsRepository;
    private final StorageService storageService;

    public BillService(PurchaseBillRepository billRepository,
                       BillAttachmentRepository attachmentRepository,
                       AppSettingsRepository settingsRepository,
                       StorageService storageService) {
        this.billRepository = billRepository;
        this.attachmentRepository = attachmentRepository;
        this.settingsRepository = settingsRepository;
        this.storageService = storageService;
    }

    public List<PurchaseBill> getAllBills() {
        return billRepository.findAllByOrderByPurchaseDateDesc();
    }

    public List<PurchaseBill> getBillsByAccount(Long accountId) {
        return billRepository.findByAccountIdOrderByPurchaseDateDesc(accountId);
    }

    public Optional<PurchaseBill> getBillById(Long id) {
        return billRepository.findById(id);
    }

    public Optional<PurchaseBill> getFirstPurchase(Long accountId) {
        Optional<PurchaseBill> flagged = billRepository.findFirstByAccountIdAndIsFirstPurchaseTrue(accountId);
        if (flagged.isPresent()) {
            return flagged;
        }
        return billRepository.findFirstByAccountIdOrderByPurchaseDateAsc(accountId);
    }

    public List<PurchaseBill> filterBills(Long accountId, ItemCategory category, PlatformStore platform, String search) {
        return billRepository.filterBills(accountId, category, platform, search);
    }

    public PurchaseBill saveBill(PurchaseBill bill, List<MultipartFile> files) throws IOException {
        normalizeBaseAmount(bill);

        // Maintain single first-purchase flag per account
        if (bill.isFirstPurchase() && bill.getAccount() != null) {
            List<PurchaseBill> existing = billRepository.findByAccountIdOrderByPurchaseDateDesc(bill.getAccount().getId());
            for (PurchaseBill b : existing) {
                if (!b.getId().equals(bill.getId()) && b.isFirstPurchase()) {
                    b.setFirstPurchase(false);
                    billRepository.save(b);
                }
            }
        }

        PurchaseBill savedBill = billRepository.save(bill);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String storedName = storageService.store(file);
                    BillAttachment attachment = new BillAttachment();
                    attachment.setBill(savedBill);
                    attachment.setOriginalFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "receipt");
                    attachment.setStoredFileName(storedName);
                    attachment.setContentType(file.getContentType());
                    attachment.setFileSize(file.getSize());
                    attachment.setPrimaryProof(savedBill.getAttachments().isEmpty());
                    attachmentRepository.save(attachment);
                    savedBill.getAttachments().add(attachment);
                }
            }
        }

        return savedBill;
    }

    public void deleteBill(Long id) {
        Optional<PurchaseBill> billOpt = billRepository.findById(id);
        if (billOpt.isPresent()) {
            PurchaseBill bill = billOpt.get();
            for (BillAttachment attachment : bill.getAttachments()) {
                storageService.delete(attachment.getStoredFileName());
            }
            billRepository.delete(bill);
        }
    }

    public void deleteAttachment(Long attachmentId) {
        Optional<BillAttachment> attOpt = attachmentRepository.findById(attachmentId);
        if (attOpt.isPresent()) {
            BillAttachment att = attOpt.get();
            storageService.delete(att.getStoredFileName());
            attachmentRepository.delete(att);
        }
    }

    public void setPrimaryAttachment(Long attachmentId) {
        Optional<BillAttachment> attOpt = attachmentRepository.findById(attachmentId);
        if (attOpt.isPresent()) {
            BillAttachment target = attOpt.get();
            PurchaseBill bill = target.getBill();
            for (BillAttachment a : bill.getAttachments()) {
                a.setPrimaryProof(a.getId().equals(target.getId()));
                attachmentRepository.save(a);
            }
        }
    }

    private void normalizeBaseAmount(PurchaseBill bill) {
        if (bill.getAmount() == null) {
            bill.setBaseAmount(BigDecimal.ZERO);
            return;
        }

        String currency = bill.getCurrency() != null ? bill.getCurrency().toUpperCase().trim() : "USD";
        bill.setCurrency(currency);

        AppSettings settings = settingsRepository.findById(1L).orElseGet(() -> {
            AppSettings s = new AppSettings();
            return settingsRepository.save(s);
        });

        if ("USD".equalsIgnoreCase(currency)) {
            bill.setBaseAmount(bill.getAmount());
        } else if ("VND".equalsIgnoreCase(currency)) {
            BigDecimal rate = settings.getUsdToVndRate() != null && settings.getUsdToVndRate().compareTo(BigDecimal.ZERO) > 0
                    ? settings.getUsdToVndRate()
                    : new BigDecimal("25400");
            bill.setBaseAmount(bill.getAmount().divide(rate, 2, RoundingMode.HALF_UP));
        } else if ("EUR".equalsIgnoreCase(currency)) {
            BigDecimal rate = settings.getUsdToEurRate() != null && settings.getUsdToEurRate().compareTo(BigDecimal.ZERO) > 0
                    ? settings.getUsdToEurRate()
                    : new BigDecimal("0.92");
            bill.setBaseAmount(bill.getAmount().divide(rate, 2, RoundingMode.HALF_UP));
        } else if ("JPY".equalsIgnoreCase(currency)) {
            BigDecimal rate = settings.getUsdToJpyRate() != null && settings.getUsdToJpyRate().compareTo(BigDecimal.ZERO) > 0
                    ? settings.getUsdToJpyRate()
                    : new BigDecimal("155");
            bill.setBaseAmount(bill.getAmount().divide(rate, 2, RoundingMode.HALF_UP));
        } else {
            // Default 1:1 if currency rate not explicitly mapped
            bill.setBaseAmount(bill.getAmount());
        }
    }
}
