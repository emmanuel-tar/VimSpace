package com.vimspace.api.repository;

import com.vimspace.api.entity.PurchaseOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, Long> {
    List<PurchaseOrderEntity> findByStatusOrderByOrderedDateDesc(String status);
    List<PurchaseOrderEntity> findBySupplierIdOrderByOrderedDateDesc(Long supplierId);
    List<PurchaseOrderEntity> findByStatusNotOrderByOrderedDateDesc(String status);
    Optional<PurchaseOrderEntity> findByPoNumber(String poNumber);
}