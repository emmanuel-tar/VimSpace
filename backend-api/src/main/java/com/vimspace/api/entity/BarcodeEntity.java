package com.vimspace.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "barcodes")
public class BarcodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "barcode_id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false, unique = true, length = 100)
    private String barcode;

    public BarcodeEntity() {}

    public BarcodeEntity(ProductEntity product, String barcode) {
        this.product = product;
        this.barcode = barcode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ProductEntity getProduct() { return product; }
    public void setProduct(ProductEntity product) { this.product = product; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
}

