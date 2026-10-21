package com.vimspace.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 100)
    private String category;

    @Column(length = 20)
    private String unit = "pcs";

    @Column(length = 50)
    private String size;

    @Column(length = 50)
    private String color;

    @Column(name = "cost_price", precision = 18, scale = 4, nullable = false)
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(name = "selling_price", precision = 18, scale = 4, nullable = false)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @Column(name = "tax_setting", length = 20)
    private String taxSetting = "inclusive";

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "stock_on_hand", precision = 18, scale = 4, nullable = false)
    private BigDecimal stockOnHand = BigDecimal.ZERO;

    public ProductEntity() {}

    public ProductEntity(String sku, String name, String category, String size, String color,
                         BigDecimal costPrice, BigDecimal sellingPrice, BigDecimal initialStock) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.size = size;
        this.color = color;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.stockOnHand = initialStock;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    public String getTaxSetting() { return taxSetting; }
    public void setTaxSetting(String taxSetting) { this.taxSetting = taxSetting; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public BigDecimal getStockOnHand() { return stockOnHand; }
    public void setStockOnHand(BigDecimal stockOnHand) { this.stockOnHand = stockOnHand; }
}
