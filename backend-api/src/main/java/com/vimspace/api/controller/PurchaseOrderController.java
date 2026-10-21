package com.vimspace.api.controller;

import com.vimspace.api.service.PurchaseOrderService;
import com.vimspace.contract.CreatePurchaseOrderRequest;
import com.vimspace.contract.PurchaseOrderDTO;
import com.vimspace.contract.RequestPurchaseOrderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {
    private final PurchaseOrderService poService;

    public PurchaseOrderController(PurchaseOrderService poService) {
        this.poService = poService;
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderDTO>> getAll(@RequestParam(value = "status", defaultValue = "OPEN") String status) {
        if (status == null || status.isBlank()) {
            return ResponseEntity.ok(poService.getAll());
        }
        return ResponseEntity.ok(poService.getAllByStatus(status.toUpperCase()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderDTO> getById(@PathVariable Long id) {
        return poService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/po-number/{poNumber}")
    public ResponseEntity<PurchaseOrderDTO> getByPoNumber(@PathVariable String poNumber) {
        return poService.findByPoNumber(poNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PurchaseOrderDTO>> getBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(poService.getBySupplierId(supplierId));
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderDTO> create(@RequestBody CreatePurchaseOrderRequest req) {
        return ResponseEntity.ok(poService.create(req));
    }

    @PutMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrderDTO> receive(@PathVariable Long id, @RequestBody RequestPurchaseOrderRequest req) {
        return ResponseEntity.ok(poService.receive(req));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<PurchaseOrderDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(poService.cancel(id));
    }

    @GetMapping("/open")
    public ResponseEntity<List<PurchaseOrderDTO>> getOpen() {
        return ResponseEntity.ok(poService.getOpen());
    }
}