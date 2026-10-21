package com.vimspace.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sale_lines")
public class SaleLineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private SaleEntity sale;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(precision = 18, scale = 4, nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit_price", precision = 18, scale = 4, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "line_total", precision = 18, scale = 4, nullable = false)
    private BigDecimal lineTotal;

    public SaleLineEntity() {}

    public SaleLineEntity(SaleEntity sale, ProductEntity product, BigDecimal quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
        this.sale = sale;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SaleEntity getSale() { return sale; }
    public void setSale(SaleEntity sale) { this.sale = sale; }
    public ProductEntity getProduct() { return product; }
    public void setProduct(ProductEntity product) { this.product = product; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}

