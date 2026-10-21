package com.vimspace.api.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "poi_id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id", nullable = false)
    private PurchaseOrderEntity purchaseOrder;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "quantity_ordered", nullable = false)
    private Integer quantityOrdered;

    @Column(name = "quantity_received", nullable = false)
    private Integer quantityReceived = 0;

    @Column(name = "unit_cost", precision = 18, scale = 4, nullable = false)
    private BigDecimal unitCost;

    public PurchaseOrderItemEntity() {}

    public PurchaseOrderItemEntity(String sku, String productName, Integer quantityOrdered,
                                   Integer quantityReceived, BigDecimal unitCost) {
        this.sku = sku;
        this.productName = productName;
        this.quantityOrdered = quantityOrdered;
        this.quantityReceived = quantityReceived != null ? quantityReceived : 0;
        this.unitCost = unitCost;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PurchaseOrderEntity getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrderEntity purchaseOrder) { this.purchaseOrder = purchaseOrder; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; }
    public Integer getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getLineTotal() {
        return unitCost.multiply(new BigDecimal(quantityReceived != null ? quantityReceived : 0));
    }

    /** Value of the ordered line: unit cost x quantity ordered. */
    public BigDecimal getOrderedLineTotal() {
        return unitCost.multiply(new BigDecimal(quantityOrdered != null ? quantityOrdered : 0));
    }
}