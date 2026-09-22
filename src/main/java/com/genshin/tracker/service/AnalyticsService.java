package com.genshin.tracker.service;

import com.genshin.tracker.dto.DashboardSummaryDTO;
import com.genshin.tracker.model.AppSettings;
import com.genshin.tracker.model.ItemCategory;
import com.genshin.tracker.model.PlatformStore;
import com.genshin.tracker.repository.AppSettingsRepository;
import com.genshin.tracker.repository.PurchaseBillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final PurchaseBillRepository billRepository;
    private final AppSettingsRepository settingsRepository;

    public AnalyticsService(PurchaseBillRepository billRepository, AppSettingsRepository settingsRepository) {
        this.billRepository = billRepository;
        this.settingsRepository = settingsRepository;
    }

    public DashboardSummaryDTO getDashboardSummary(Long accountId) {
        DashboardSummaryDTO summary = new DashboardSummaryDTO();

        AppSettings settings = settingsRepository.findById(1L).orElseGet(() -> {
            AppSettings s = new AppSettings();
            return settingsRepository.save(s);
        });
        summary.setBaseCurrency(settings.getBaseCurrency());

        // Total spending
        if (accountId != null) {
            summary.setTotalSpend(billRepository.sumTotalBaseAmountByAccount(accountId));
        } else {
            summary.setTotalSpend(billRepository.sumTotalBaseAmount());
        }

        // Current Month spending
        LocalDateTime firstDayOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        summary.setThisMonthSpend(billRepository.sumBaseAmountSince(firstDayOfMonth));

        // Counts
        summary.setTotalBillsCount(billRepository.count());
        summary.setWelkinCount(billRepository.countByCategory(ItemCategory.WELKIN_MOON));
        summary.setBattlePassCount(
                billRepository.countByCategory(ItemCategory.BATTLE_PASS_HYMN) +
                billRepository.countByCategory(ItemCategory.BATTLE_PASS_CHORUS) +
                billRepository.countByCategory(ItemCategory.BATTLE_PASS_UPGRADE)
        );

        long crystalsCount =
                billRepository.countByCategory(ItemCategory.GENESIS_60) +
                billRepository.countByCategory(ItemCategory.GENESIS_300) +
                billRepository.countByCategory(ItemCategory.GENESIS_980) +
                billRepository.countByCategory(ItemCategory.GENESIS_1980) +
                billRepository.countByCategory(ItemCategory.GENESIS_3280) +
                billRepository.countByCategory(ItemCategory.GENESIS_6480);
        summary.setCrystalsCount(crystalsCount);

        // Category breakdown
        List<Object[]> categoryData = billRepository.getCategoryBreakdown();
        for (Object[] row : categoryData) {
            ItemCategory cat = (ItemCategory) row[0];
            BigDecimal sum = (BigDecimal) row[2];
            summary.getCategoryLabels().add(cat != null ? cat.getDisplayName() : "Unknown");
            summary.getCategoryValues().add(sum);
        }

        // Platform breakdown
        List<Object[]> platformData = billRepository.getPlatformBreakdown();
        for (Object[] row : platformData) {
            PlatformStore plat = (PlatformStore) row[0];
            BigDecimal sum = (BigDecimal) row[2];
            summary.getPlatformLabels().add(plat != null ? plat.getDisplayName() : "Unknown");
            summary.getPlatformValues().add(sum);
        }

        // Monthly breakdown
        List<Object[]> monthlyData = billRepository.getMonthlyBreakdown();
        for (Object[] row : monthlyData) {
            Integer yr = (Integer) row[0];
            Integer mo = (Integer) row[1];
            BigDecimal sum = (BigDecimal) row[2];
            String label = String.format("%02d/%d", mo, yr);
            summary.getMonthlyLabels().add(label);
            summary.getMonthlyValues().add(sum);
        }

        return summary;
    }
}
