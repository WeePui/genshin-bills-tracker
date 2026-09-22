package com.genshin.tracker.repository;

import com.genshin.tracker.model.ItemCategory;
import com.genshin.tracker.model.PlatformStore;
import com.genshin.tracker.model.PurchaseBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseBillRepository extends JpaRepository<PurchaseBill, Long> {

    List<PurchaseBill> findAllByOrderByPurchaseDateDesc();

    List<PurchaseBill> findByAccountIdOrderByPurchaseDateDesc(Long accountId);

    Optional<PurchaseBill> findFirstByAccountIdAndIsFirstPurchaseTrue(Long accountId);

    Optional<PurchaseBill> findFirstByAccountIdOrderByPurchaseDateAsc(Long accountId);

    @Query("SELECT COALESCE(SUM(b.baseAmount), 0) FROM PurchaseBill b")
    BigDecimal sumTotalBaseAmount();

    @Query("SELECT COALESCE(SUM(b.baseAmount), 0) FROM PurchaseBill b WHERE b.account.id = :accountId")
    BigDecimal sumTotalBaseAmountByAccount(@Param("accountId") Long accountId);

    @Query("SELECT COALESCE(SUM(b.baseAmount), 0) FROM PurchaseBill b WHERE b.purchaseDate >= :startDate")
    BigDecimal sumBaseAmountSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(b) FROM PurchaseBill b WHERE b.itemCategory = :category")
    long countByCategory(@Param("category") ItemCategory category);

    @Query("SELECT b.itemCategory, COUNT(b), COALESCE(SUM(b.baseAmount), 0) FROM PurchaseBill b GROUP BY b.itemCategory")
    List<Object[]> getCategoryBreakdown();

    @Query("SELECT b.platform, COUNT(b), COALESCE(SUM(b.baseAmount), 0) FROM PurchaseBill b GROUP BY b.platform")
    List<Object[]> getPlatformBreakdown();

    @Query("SELECT YEAR(b.purchaseDate) as yr, MONTH(b.purchaseDate) as mo, COALESCE(SUM(b.baseAmount), 0) " +
           "FROM PurchaseBill b GROUP BY YEAR(b.purchaseDate), MONTH(b.purchaseDate) " +
           "ORDER BY YEAR(b.purchaseDate) ASC, MONTH(b.purchaseDate) ASC")
    List<Object[]> getMonthlyBreakdown();

    @Query("SELECT b FROM PurchaseBill b WHERE " +
           "(:accountId IS NULL OR b.account.id = :accountId) AND " +
           "(:category IS NULL OR b.itemCategory = :category) AND " +
           "(:platform IS NULL OR b.platform = :platform) AND " +
           "(:search IS NULL OR LOWER(b.orderId) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(b.notes) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(b.customItemName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY b.purchaseDate DESC")
    List<PurchaseBill> filterBills(
            @Param("accountId") Long accountId,
            @Param("category") ItemCategory category,
            @Param("platform") PlatformStore platform,
            @Param("search") String search);
}
