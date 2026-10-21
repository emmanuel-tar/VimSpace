package com.vimspace.api.service;

import com.vimspace.api.entity.*;
import com.vimspace.api.repository.*;
import com.vimspace.contract.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SaleService {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final TillSessionRepository tillSessionRepository;

    public SaleService(SaleRepository saleRepository,
                       ProductRepository productRepository,
                       StockMovementRepository stockMovementRepository,
                       TillSessionRepository tillSessionRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.tillSessionRepository = tillSessionRepository;
    }

    @Transactional
    public SaleResponse processSale(SaleRequest request) {
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<SaleEntity> existing = saleRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                SaleEntity sale = existing.get();
                return new SaleResponse(sale.getReceiptNumber(), sale.getStatus(), sale.getTotal().doubleValue());
            }
        }

        String receiptNumber = "VSP-" + System.currentTimeMillis();
        SaleEntity sale = new SaleEntity();
        sale.setReceiptNumber(receiptNumber);
        sale.setCashierId(request.getCashierId() != null ? request.getCashierId() : "system");
        sale.setTerminalId(request.getTerminalId() != null ? request.getTerminalId() : "POS-01");
        sale.setIdempotencyKey(request.getIdempotencyKey() != null ? request.getIdempotencyKey() : receiptNumber);
        sale.setCreatedAt(LocalDateTime.now());
        sale.setStatus("completed");
        sale.setSessionId(resolveSessionId(request));

        BigDecimal total = BigDecimal.ZERO;

        if (request.getLines() != null) {
            for (SaleLineDTO lineDTO : request.getLines()) {
                ProductEntity product = productRepository.findBySku(lineDTO.getProductSku())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineDTO.getProductSku()));

                BigDecimal qty = BigDecimal.valueOf(lineDTO.getQuantity());
                BigDecimal unitPrice = BigDecimal.valueOf(lineDTO.getUnitPrice());
                BigDecimal lineTotal = unitPrice.multiply(qty);

                SaleLineEntity line = new SaleLineEntity(sale, product, qty, unitPrice, lineTotal);
                sale.getLines().add(line);
                total = total.add(lineTotal);

                // Deduct stock
                product.setStockOnHand(product.getStockOnHand().subtract(qty));
                productRepository.save(product);

                // Record append-only stock movement ledger
                StockMovementEntity movement = new StockMovementEntity(
                        product,
                        "sale",
                        receiptNumber,
                        qty.negate(),
                        "POS sale " + receiptNumber,
                        sale.getCashierId()
                );
                stockMovementRepository.save(movement);
            }
        }

        sale.setTotal(total);

        if (request.getPayments() != null) {
            for (PaymentDTO paymentDTO : request.getPayments()) {
                PaymentEntity payment = new PaymentEntity(
                        sale,
                        paymentDTO.getMethod(),
                        BigDecimal.valueOf(paymentDTO.getAmount())
                );
                sale.getPayments().add(payment);
            }
        }

        SaleEntity saved = saleRepository.save(sale);
        return new SaleResponse(saved.getReceiptNumber(), saved.getStatus(), saved.getTotal().doubleValue());
    }

    /**
     * Resolves the till session a sale belongs to.
     *
     * <p>If the client supplied an explicit {@code sessionId} it is honoured as
     * long as that session exists and is still open. Otherwise the sale is
     * linked to the terminal's open session, if there is one. Sales rung up
     * without any open session keep a null session (they cannot be reconciled
     * against a drawer).</p>
     */
    private Long resolveSessionId(SaleRequest request) {
        if (request.getSessionId() != null) {
            TillSessionEntity session = tillSessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new TillSessionNotFoundException(
                            "Till session not found: " + request.getSessionId()));
            if (!"OPEN".equalsIgnoreCase(session.getStatus())) {
                throw new TillSessionConflictException(
                        "Till session #" + session.getId() + " is " + session.getStatus()
                                + " and cannot accept new sales");
            }
            return session.getId();
        }
        String terminalId = request.getTerminalId() != null ? request.getTerminalId() : "POS-01";
        return tillSessionRepository
                .findTopByTerminalIdAndStatusOrderByIdDesc(terminalId, "OPEN")
                .map(TillSessionEntity::getId)
                .orElse(null);
    }

    public List<SaleDetailDTO> getAllSales() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return saleRepository.findAll().stream()
                .sorted(Comparator.comparing(SaleEntity::getCreatedAt).reversed())
                .map(s -> toDetailDTO(s, fmt))
                .collect(Collectors.toList());
    }

    public Optional<SaleDetailDTO> getSaleByReceipt(String receiptNumber) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return saleRepository.findByReceiptNumber(receiptNumber).map(s -> toDetailDTO(s, fmt));
    }

    public DailySalesSummaryDTO getDailySummary() {
        LocalDate today = LocalDate.now();
        List<SaleEntity> todaySales = saleRepository.findAll().stream()
                .filter(s -> s.getCreatedAt().toLocalDate().equals(today))
                .collect(Collectors.toList());

        double totalSales = 0.0;
        Map<String, Double> paymentMap = new LinkedHashMap<>();
        paymentMap.put("Cash", 0.0);
        paymentMap.put("Card / POS", 0.0);
        paymentMap.put("Bank Transfer", 0.0);

        for (SaleEntity sale : todaySales) {
            totalSales += sale.getTotal().doubleValue();
            for (PaymentEntity p : sale.getPayments()) {
                String m = p.getMethod();
                paymentMap.put(m, paymentMap.getOrDefault(m, 0.0) + p.getAmount().doubleValue());
            }
        }

        return new DailySalesSummaryDTO(totalSales, todaySales.size(), paymentMap);
    }

    private SaleDetailDTO toDetailDTO(SaleEntity s, DateTimeFormatter fmt) {
        List<SaleLineDTO> lines = s.getLines().stream()
                .map(l -> new SaleLineDTO(
                        l.getProduct().getSku(),
                        l.getQuantity().intValue(),
                        l.getUnitPrice().doubleValue(),
                        l.getLineTotal().doubleValue()
                )).collect(Collectors.toList());

        List<PaymentDTO> payments = s.getPayments().stream()
                .map(p -> new PaymentDTO(p.getMethod(), p.getAmount().doubleValue()))
                .collect(Collectors.toList());

        return new SaleDetailDTO(
                s.getReceiptNumber(),
                s.getTerminalId(),
                s.getCashierId(),
                s.getCreatedAt().format(fmt),
                s.getTotal().doubleValue(),
                s.getTax().doubleValue(),
                s.getDiscount().doubleValue(),
                s.getStatus(),
                lines,
                payments
        );
    }
}
