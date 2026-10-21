package com.vimspace.api.controller;

import com.vimspace.api.service.ProductService;
import com.vimspace.contract.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAll(@RequestParam(value = "activeOnly", defaultValue = "false") boolean activeOnly) {
        if (activeOnly) {
            return ResponseEntity.ok(productService.getAllActive());
        }
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<ProductDTO> getByBarcode(@PathVariable("barcode") String barcode) {
        return productService.findByBarcode(barcode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDTO> getBySku(@PathVariable("sku") String sku) {
        return productService.findBySku(sku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> search(@RequestParam("q") String query) {
        return ResponseEntity.ok(productService.search(query));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createOrUpdateProduct(@RequestBody CreateProductRequest req) {
        return ResponseEntity.ok(productService.createOrUpdateProduct(req));
    }

    @PostMapping("/generate-barcode")
    public ResponseEntity<BarcodeGenerateResponse> generateBarcode(@RequestBody(required = false) BarcodeGenerateRequest req) {
        return ResponseEntity.ok(productService.generateBarcode(req));
    }

    @PostMapping("/adjust-stock")
    public ResponseEntity<ProductDTO> adjustStock(@RequestBody StockAdjustmentRequest req) {
        return ResponseEntity.ok(productService.adjustStock(req));
    }

    @GetMapping("/movements")
    public ResponseEntity<List<InventoryMovementDTO>> getMovements() {
        return ResponseEntity.ok(productService.getStockMovements());
    }
}
