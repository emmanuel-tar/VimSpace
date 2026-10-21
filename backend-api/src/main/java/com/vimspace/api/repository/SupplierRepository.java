package com.vimspace.api.repository;

import com.vimspace.api.entity.SupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<SupplierEntity, Long> {
    Optional<SupplierEntity> findByName(String name);
    List<SupplierEntity> findByNameContainingIgnoreCase(String name);
    List<SupplierEntity> findByActiveTrue();
}