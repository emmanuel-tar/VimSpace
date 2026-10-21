package com.vimspace.api.repository;

import com.vimspace.api.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findBySku(String sku);
    List<ProductEntity> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(String name, String sku);
}

