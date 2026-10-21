package com.vimspace.api.repository;

import com.vimspace.api.entity.SaleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<SaleEntity, Long> {
    Optional<SaleEntity> findByIdempotencyKey(String idempotencyKey);
    Optional<SaleEntity> findByReceiptNumber(String receiptNumber);

    List<SaleEntity> findBySessionId(Long sessionId);

    long countBySessionId(Long sessionId);
}

