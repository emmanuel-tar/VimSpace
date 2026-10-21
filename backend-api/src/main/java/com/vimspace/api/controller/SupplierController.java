package com.vimspace.api.controller;

import com.vimspace.api.service.SupplierService;
import com.vimspace.contract.CreateSupplierRequest;
import com.vimspace.contract.SupplierDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public ResponseEntity<List<SupplierDTO>> getAll(@RequestParam(value = "q", defaultValue = "") String q) {
        List<SupplierDTO> list = q.isBlank() ? supplierService.getAll() : supplierService.search(q);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDTO> getById(@PathVariable Long id) {
        return supplierService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SupplierDTO> create(@RequestBody CreateSupplierRequest req) {
        return ResponseEntity.ok(supplierService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierDTO> update(@PathVariable Long id, @RequestBody CreateSupplierRequest req) {
        return ResponseEntity.ok(supplierService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<SupplierDTO>> getActive() {
        return ResponseEntity.ok(supplierService.getActive());
    }
}