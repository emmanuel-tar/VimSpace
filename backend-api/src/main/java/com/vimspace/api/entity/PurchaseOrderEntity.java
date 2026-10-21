package com.vimspace.api.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "po_id")
    private Long id;

    @Column(name = "po_number", nullable = false, unique = true, length = 50)
    private String poNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private SupplierEntity supplier;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "DRAFT"; // DRAFT, ORDERED, RECEIVED, CANCELLED

    @Column(name = "ordered_date", nullable = false, updatable = false)
    private LocalDateTime orderedDate;

    @Column(name = "expected_date")
    private LocalDateTime expectedDate;

    @Column(name = "received_date")
    private LocalDateTime receivedDate;

    @Column(name = "total_amount", precision = 18, scale = 4)
    private java.math.BigDecimal totalAmount;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItemEntity> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (orderedDate == null) {
            orderedDate = LocalDateTime.now();
        }
        if (totalAmount == null) {
            totalAmount = java.math.BigDecimal.ZERO;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }
    public SupplierEntity getSupplier() { return supplier; }
    public void setSupplier(SupplierEntity supplier) { this.supplier = supplier; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getOrderedDate() { return orderedDate; }
    public void setOrderedDate(LocalDateTime orderedDate) { this.orderedDate = orderedDate; }
    public LocalDateTime getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDateTime expectedDate) { this.expectedDate = expectedDate; }
    public LocalDateTime getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDateTime receivedDate) { this.receivedDate = receivedDate; }
    public java.math.BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(java.math.BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public List<PurchaseOrderItemEntity> getItems() { return items; }
    @Column(name = "cancelled_date")
    private LocalDateTime cancelledDate;

    public LocalDateTime getCancelledDate() { return cancelledDate; }

    public void setCancelledDate(LocalDateTime cancelledDate) { this.cancelledDate = cancelledDate; }

    public void addItem(PurchaseOrderItemEntity item) {
        items.add(item);
        item.setPurchaseOrder(this);
    }

    public void removeItem(PurchaseOrderItemEntity item) {
        items.remove(item);
        item.setPurchaseOrder(null);
    }
}