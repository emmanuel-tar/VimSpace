package com.vimspace.contract;

import java.util.List;

public class ProductDTO {
    private Long id;
    private String sku;
    private String name;
    private String category;
    private String unit;
    private String size;
    private String color;
    private Double sellingPrice;
    private Double costPrice;
    private Double stockOnHand;
    private Boolean active;
    private List<String> barcodes;

    public ProductDTO() {}

    public ProductDTO(Long id, String sku, String name, String category, String unit,
                      String size, String color,
                      Double sellingPrice, Double costPrice, Double stockOnHand, Boolean active, List<String> barcodes) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.size = size;
        this.color = color;
        this.sellingPrice = sellingPrice;
        this.costPrice = costPrice;
        this.stockOnHand = stockOnHand;
        this.active = active;
        this.barcodes = barcodes;
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
    public Double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(Double sellingPrice) { this.sellingPrice = sellingPrice; }
    public Double getCostPrice() { return costPrice; }
    public void setCostPrice(Double costPrice) { this.costPrice = costPrice; }
    public Double getStockOnHand() { return stockOnHand; }
    public void setStockOnHand(Double stockOnHand) { this.stockOnHand = stockOnHand; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public List<String> getBarcodes() { return barcodes; }
    public void setBarcodes(List<String> barcodes) { this.barcodes = barcodes; }
}
