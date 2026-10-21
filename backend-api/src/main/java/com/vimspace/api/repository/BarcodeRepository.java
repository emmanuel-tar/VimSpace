package com.vimspace.api.repository;

import com.vimspace.api.entity.BarcodeEntity;
import com.vimspace.api.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BarcodeRepository extends JpaRepository<BarcodeEntity, Long> {
    Optional<BarcodeEntity> findByBarcode(String barcode);
    List<BarcodeEntity> findByProduct(ProductEntity product);
}
