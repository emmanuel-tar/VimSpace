package com.vimspace.api.service;

import com.vimspace.api.entity.BarcodeEntity;
import com.vimspace.api.entity.ProductEntity;
import com.vimspace.api.entity.StockMovementEntity;
import com.vimspace.api.repository.BarcodeRepository;
import com.vimspace.api.repository.ProductRepository;
import com.vimspace.api.repository.StockMovementRepository;
import com.vimspace.contract.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final BarcodeRepository barcodeRepository;
    private final StockMovementRepository stockMovementRepository;
    private final Random random = new Random();

    public ProductService(ProductRepository productRepository,
                          BarcodeRepository barcodeRepository,
                          StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.barcodeRepository = barcodeRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public Optional<ProductDTO> findByBarcode(String barcode) {
        return barcodeRepository.findByBarcode(barcode)
                .map(BarcodeEntity::getProduct)
                .map(this::toDTO);
    }

    public Optional<ProductDTO> findBySku(String sku) {
        return productRepository.findBySku(sku).map(this::toDTO);
    }

    public List<ProductDTO> search(String query) {
        return productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(query, query)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getAll() {
        return productRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getAllActive() {
        return productRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        return productRepository.findAll().stream()
                .map(ProductEntity::getCategory)
                .filter(Objects::nonNull)
                .filter(c -> !c.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO createOrUpdateProduct(CreateProductRequest req) {
        ProductEntity entity = productRepository.findBySku(req.getSku())
                .orElse(new ProductEntity());

        entity.setSku(req.getSku());
        entity.setName(req.getName());
        entity.setCategory(req.getCategory());
        entity.setUnit(req.getUnit() != null ? req.getUnit() : "pcs");
        entity.setSize(req.getSize());
        entity.setColor(req.getColor());
        entity.setCostPrice(BigDecimal.valueOf(req.getCostPrice() != null ? req.getCostPrice() : 0.0));
        entity.setSellingPrice(BigDecimal.valueOf(req.getSellingPrice() != null ? req.getSellingPrice() : 0.0));
        if (entity.getId() == null) {
            BigDecimal initStock = BigDecimal.valueOf(req.getInitialStock() != null ? req.getInitialStock() : 0.0);
            entity.setStockOnHand(initStock);
            entity.setActive(true);
        }

        ProductEntity saved = productRepository.save(entity);

        // Save barcodes
        if (req.getBarcodes() != null) {
            for (String code : req.getBarcodes()) {
                String trimmed = code.trim();
                if (!trimmed.isEmpty() && barcodeRepository.findByBarcode(trimmed).isEmpty()) {
                    barcodeRepository.save(new BarcodeEntity(saved, trimmed));
                }
            }
        }

        // Record initial stock entry
        if (req.getInitialStock() != null && req.getInitialStock() > 0 && entity.getStockOnHand().compareTo(BigDecimal.ZERO) > 0) {
            stockMovementRepository.save(new StockMovementEntity(
                    saved, "receipt", "INITIAL-STOCK",
                    BigDecimal.valueOf(req.getInitialStock()), "Initial stock entry", "VimManager"
            ));
        }

        return toDTO(saved);
    }

    @Transactional
    public ProductDTO adjustStock(StockAdjustmentRequest req) {
        ProductEntity product = productRepository.findBySku(req.getSku())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + req.getSku()));

        BigDecimal delta = BigDecimal.valueOf(req.getQuantityDelta());
        product.setStockOnHand(product.getStockOnHand().add(delta));
        productRepository.save(product);

        StockMovementEntity movement = new StockMovementEntity(
                product,
                delta.compareTo(BigDecimal.ZERO) >= 0 ? "adjustment_in" : "adjustment_out",
                "ADJ-" + System.currentTimeMillis(),
                delta,
                req.getReason() != null ? req.getReason() : "Manual adjustment",
                req.getUser() != null ? req.getUser() : "Manager"
        );
        stockMovementRepository.save(movement);

        return toDTO(product);
    }

    public List<InventoryMovementDTO> getStockMovements() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return stockMovementRepository.findAll().stream()
                .map(m -> new InventoryMovementDTO(
                        m.getId(),
                        m.getProduct() != null ? m.getProduct().getSku() : "-",
                        m.getProduct() != null ? m.getProduct().getName() : "-",
                        m.getType(),
                        m.getReferenceId(),
                        m.getQuantityDelta() != null ? m.getQuantityDelta().doubleValue() : 0.0,
                        m.getReason(),
                        m.getCreatedBy(),
                        m.getCreatedAt() != null ? m.getCreatedAt().format(fmt) : "-"
                ))
                .collect(Collectors.toList());
    }

    public BarcodeGenerateResponse generateBarcode(BarcodeGenerateRequest req) {
        String format = (req != null && req.getFormat() != null) ? req.getFormat().toUpperCase() : "EAN13";
        String code;
        int maxAttempts = 10;
        do {
            if ("EAN13".equals(format)) {
                code = generateEan13(req != null && req.getPrefix() != null ? req.getPrefix() : "20");
            } else {
                code = generateCode128(req != null && req.getPrefix() != null ? req.getPrefix() : "VSP");
            }
            maxAttempts--;
        } while (barcodeRepository.findByBarcode(code).isPresent() && maxAttempts > 0);

        return new BarcodeGenerateResponse(code, format);
    }

    private String generateEan13(String prefix) {
        StringBuilder sb = new StringBuilder(prefix);
        while (sb.length() < 12) {
            sb.append(random.nextInt(10));
        }
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int d = Character.getNumericValue(sb.charAt(i));
            sum += (i % 2 == 0) ? d : d * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        sb.append(checkDigit);
        return sb.toString();
    }

    private String generateCode128(String prefix) {
        return prefix + (100000 + random.nextInt(900000));
    }

    private ProductDTO toDTO(ProductEntity p) {
        List<String> codes = barcodeRepository.findByProduct(p).stream()
                .map(BarcodeEntity::getBarcode)
                .collect(Collectors.toList());

        return new ProductDTO(
                p.getId(),
                p.getSku(),
                p.getName(),
                p.getCategory(),
                p.getUnit(),
                p.getSize(),
                p.getColor(),
                p.getSellingPrice() != null ? p.getSellingPrice().doubleValue() : 0.0,
                p.getCostPrice() != null ? p.getCostPrice().doubleValue() : 0.0,
                p.getStockOnHand() != null ? p.getStockOnHand().doubleValue() : 0.0,
                p.getActive(),
                codes
        );
    }
    public String findProductNameBySku(String sku) {
        return productRepository.findBySku(sku)
                .map(ProductEntity::getName)
                .orElse(sku);
    }
}
