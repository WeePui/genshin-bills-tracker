package com.genshin.tracker.repository;

import com.genshin.tracker.model.BillAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillAttachmentRepository extends JpaRepository<BillAttachment, Long> {
    List<BillAttachment> findByBillId(Long billId);
    Optional<BillAttachment> findByStoredFileName(String storedFileName);
}
