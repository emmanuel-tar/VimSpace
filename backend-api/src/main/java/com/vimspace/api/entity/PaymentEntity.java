package com.vimspace.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "payments")
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private SaleEntity sale;

    @Column(nullable = false, length = 30)
    private String method;

    @Column(precision = 18, scale = 4, nullable = false)
    private BigDecimal amount;

    public PaymentEntity() {}

    public PaymentEntity(SaleEntity sale, String method, BigDecimal amount) {
        this.sale = sale;
        this.method = method;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SaleEntity getSale() { return sale; }
    public void setSale(SaleEntity sale) { this.sale = sale; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

