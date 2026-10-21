package com.vimspace.api.service;

import com.vimspace.api.entity.PurchaseOrderEntity;
import com.vimspace.api.entity.PurchaseOrderItemEntity;
import com.vimspace.api.entity.SupplierEntity;
import com.vimspace.api.repository.PurchaseOrderRepository;
import com.vimspace.api.repository.SupplierRepository;
import com.vimspace.contract.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderService {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final PurchaseOrderRepository poRepository;
    private final SupplierRepository supplierRepository;
    private final ProductService productService;

    public PurchaseOrderService(PurchaseOrderRepository poRepository,
                                SupplierRepository supplierRepository,
                                ProductService productService) {
        this.poRepository = poRepository;
        this.supplierRepository = supplierRepository;
        this.productService = productService;
    }

    public List<PurchaseOrderDTO> getAll() {
        return poRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<PurchaseOrderDTO> getAllByStatus(String status) {
        return poRepository.findByStatusOrderByOrderedDateDesc(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PurchaseOrderDTO> getBySupplierId(Long supplierId) {
        return poRepository.findBySupplierIdOrderByOrderedDateDesc(supplierId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PurchaseOrderDTO> getOpen() {
        return poRepository.findByStatusNotOrderByOrderedDateDesc("RECEIVED").stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<PurchaseOrderDTO> findById(Long id) {
        return poRepository.findById(id).map(this::toDTO);
    }

    public Optional<PurchaseOrderDTO> findByPoNumber(String poNumber) {
        return poRepository.findByPoNumber(poNumber).map(this::toDTO);
    }

    @Transactional
    public PurchaseOrderDTO create(CreatePurchaseOrderRequest req) {
        Optional<SupplierEntity> supplierOpt = supplierRepository.findById(req.getSupplierId());
        if (supplierOpt.isEmpty()) {
            throw new RuntimeException("Supplier not found: " + req.getSupplierId());
        }
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new RuntimeException("Purchase order must contain at least one item");
        }

        PurchaseOrderEntity po = new PurchaseOrderEntity();
        po.setPoNumber(generatePoNumber());
        po.setSupplier(supplierOpt.get());
        po.setStatus("DRAFT");
        po.setOrderedDate(LocalDateTime.now());
        if (req.getExpectedDate() != null && !req.getExpectedDate().isBlank()) {
            po.setExpectedDate(LocalDateTime.parse(req.getExpectedDate(), FMT));
        }

        for (CreatePurchaseOrderRequest.PurchaseOrderItemRequest itemReq : req.getItems()) {
            String productName = productService.findBySku(itemReq.getSku())
                    .map(ProductDTO::getName)
                    .orElse(itemReq.getSku());
            PurchaseOrderItemEntity item = new PurchaseOrderItemEntity(
                    itemReq.getSku(),
                    productName,
                    itemReq.getQuantityOrdered(),
                    0,
                    BigDecimal.valueOf(itemReq.getUnitCost() != null ? itemReq.getUnitCost() : 0.0));
            po.addItem(item);
        }

        po.setTotalAmount(calculateTotal(po));
        PurchaseOrderEntity saved = poRepository.save(po);
        return toDTO(saved);
    }

    @Transactional
    public PurchaseOrderDTO receive(RequestPurchaseOrderRequest req) {
        PurchaseOrderEntity po = poRepository.findById(req.getPoId())
                .orElseThrow(() -> new RuntimeException("Purchase order not found: " + req.getPoId()));
        if (!"DRAFT".equals(po.getStatus()) && !"ORDERED".equals(po.getStatus())) {
            throw new RuntimeException("Cannot receive goods for PO with status: " + po.getStatus());
        }
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new RuntimeException("No items supplied for receipt");
        }

        for (RequestPurchaseOrderRequest.ReceiveItem itemReq : req.getItems()) {
            Optional<PurchaseOrderItemEntity> itemOpt = po.getItems().stream()
                    .filter(i -> i.getSku().equals(itemReq.getSku()))
                    .findFirst();
            if (itemOpt.isEmpty()) {
                throw new RuntimeException("Item SKU not found in PO: " + itemReq.getSku());
            }
            PurchaseOrderItemEntity item = itemOpt.get();
            int existing = item.getQuantityReceived() != null ? item.getQuantityReceived() : 0;
            int requested = itemReq.getQuantityReceived() != null ? itemReq.getQuantityReceived() : 0;
            if (requested <= 0) {
                throw new RuntimeException("Received quantity must be positive for SKU: " + item.getSku());
            }
            int newReceived = existing + requested;
            if (newReceived > item.getQuantityOrdered()) {
                throw new RuntimeException("Quantity received exceeds ordered amount for SKU: " + item.getSku());
            }

            item.setQuantityReceived(newReceived);
            productService.adjustStock(new StockAdjustmentRequest(
                    item.getSku(),
                    (double) requested,
                    "purchase_receipt",
                    "P.O. " + po.getPoNumber()));
        }

        if (isFullyReceived(po)) {
            po.setStatus("RECEIVED");
            po.setReceivedDate(LocalDateTime.now());
        } else {
            po.setStatus("ORDERED");
        }
        PurchaseOrderEntity saved = poRepository.save(po);
        return toDTO(saved);
    }

    @Transactional
    public PurchaseOrderDTO cancel(Long id) {
        PurchaseOrderEntity po = poRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found: " + id));
        po.setStatus("CANCELLED");
        po.setCancelledDate(LocalDateTime.now());
        return toDTO(poRepository.save(po));
    }

    private String generatePoNumber() {
        long max = poRepository.findAll().stream()
                .map(PurchaseOrderEntity::getPoNumber)
                .filter(Objects::nonNull)
                .mapToLong(num -> {
                    try {
                        return Long.parseLong(num.replaceAll("[^0-9]", ""));
                    } catch (NumberFormatException ex) {
                        return 0L;
                    }
                })
                .max()
                .orElse(0L);
        return "PO" + String.format("%04d", max + 1);
    }

    private boolean isFullyReceived(PurchaseOrderEntity po) {
        return !po.getItems().isEmpty()
                && po.getItems().stream().allMatch(i ->
                (i.getQuantityReceived() != null ? i.getQuantityReceived() : 0) >= i.getQuantityOrdered());
    }

    private BigDecimal calculateTotal(PurchaseOrderEntity po) {
        return po.getItems().stream()
                .map(PurchaseOrderItemEntity::getOrderedLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PurchaseOrderDTO toDTO(PurchaseOrderEntity e) {
        List<PurchaseOrderItemDTO> items = e.getItems().stream()
                .map(i -> new PurchaseOrderItemDTO(
                        i.getSku(),
                        i.getProductName(),
                        i.getQuantityOrdered(),
                        i.getQuantityReceived(),
                        i.getUnitCost().doubleValue(),
                        i.getOrderedLineTotal().doubleValue()))
                .collect(Collectors.toList());
        BigDecimal total = e.getTotalAmount() != null ? e.getTotalAmount() : BigDecimal.ZERO;
        return new PurchaseOrderDTO(
                e.getId(),
                e.getPoNumber(),
                e.getSupplier().getId(),
                e.getSupplier().getName(),
                e.getStatus(),
                e.getOrderedDate() != null ? e.getOrderedDate().format(FMT) : null,
                e.getExpectedDate() != null ? e.getExpectedDate().format(FMT) : null,
                e.getReceivedDate() != null ? e.getReceivedDate().format(FMT) : null,
                total.doubleValue(),
                items);
    }
}