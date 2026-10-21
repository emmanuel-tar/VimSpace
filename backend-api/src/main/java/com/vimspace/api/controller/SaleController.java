package com.vimspace.api.controller;

import com.vimspace.api.service.SaleService;
import com.vimspace.contract.DailySalesSummaryDTO;
import com.vimspace.contract.SaleDetailDTO;
import com.vimspace.contract.SaleRequest;
import com.vimspace.contract.SaleResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    public ResponseEntity<SaleResponse> completeSale(@RequestBody SaleRequest request) {
        SaleResponse response = saleService.processSale(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SaleDetailDTO>> getAllSales() {
        return ResponseEntity.ok(saleService.getAllSales());
    }

    @GetMapping("/receipt/{receiptNumber}")
    public ResponseEntity<SaleDetailDTO> getByReceipt(@PathVariable("receiptNumber") String receiptNumber) {
        return saleService.getSaleByReceipt(receiptNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<DailySalesSummaryDTO> getDailySummary() {
        return ResponseEntity.ok(saleService.getDailySummary());
    }
}
