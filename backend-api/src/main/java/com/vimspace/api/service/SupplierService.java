package com.vimspace.api.service;

import com.vimspace.api.entity.SupplierEntity;
import com.vimspace.api.repository.SupplierRepository;
import com.vimspace.contract.CreateSupplierRequest;
import com.vimspace.contract.SupplierDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<SupplierDTO> getAll() {
        return supplierRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<SupplierDTO> search(String query) {
        if (query == null || query.isBlank()) {
            return getAll();
        }
        return supplierRepository.findByNameContainingIgnoreCase(query).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<SupplierDTO> findById(Long id) {
        return supplierRepository.findById(id).map(this::toDTO);
    }

    public Optional<SupplierDTO> findByName(String name) {
        return supplierRepository.findByName(name).map(this::toDTO);
    }

    @Transactional
    public SupplierDTO create(CreateSupplierRequest req) {
        Optional<SupplierEntity> existing = supplierRepository.findByName(req.getName().trim());
        if (existing.isPresent()) {
            throw new RuntimeException("Supplier already exists: " + req.getName());
        }

        SupplierEntity entity = new SupplierEntity(
                req.getName().trim(),
                req.getContactPerson() != null ? req.getContactPerson().trim() : null,
                req.getPhone() != null ? req.getPhone().trim() : null,
                req.getEmail() != null ? req.getEmail().trim() : null,
                req.getAddress() != null ? req.getAddress().trim() : null
        );
        SupplierEntity saved = supplierRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public SupplierDTO update(Long id, CreateSupplierRequest req) {
        SupplierEntity entity = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + id));

        if (req.getName() != null && !req.getName().trim().equals(entity.getName())) {
            Optional<SupplierEntity> existing = supplierRepository.findByName(req.getName().trim());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new RuntimeException("Supplier already exists: " + req.getName());
            }
            entity.setName(req.getName().trim());
        }
        if (req.getContactPerson() != null) entity.setContactPerson(req.getContactPerson().trim());
        if (req.getPhone() != null) entity.setPhone(req.getPhone().trim());
        if (req.getEmail() != null) entity.setEmail(req.getEmail().trim());
        if (req.getAddress() != null) entity.setAddress(req.getAddress().trim());

        SupplierEntity saved = supplierRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new RuntimeException("Supplier not found: " + id);
        }
        supplierRepository.deleteById(id);
    }

    public List<SupplierDTO> getActive() {
        return supplierRepository.findByActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private SupplierDTO toDTO(SupplierEntity e) {
        return new SupplierDTO(
                e.getId(),
                e.getName(),
                e.getContactPerson(),
                e.getPhone(),
                e.getEmail(),
                e.getAddress(),
                e.getActive()
        );
    }
}