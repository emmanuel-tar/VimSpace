package com.vimspace.contract;

import java.util.List;

public class CreateProductRequest {
    private String sku;
    private String name;
    private String category;
    private String unit;
    private String size;
    private String color;
    private Double costPrice;
    private Double sellingPrice;
    private Double initialStock;
    private List<String> barcodes;

    public CreateProductRequest() {}

    public CreateProductRequest(String sku, String name, String category, String unit,
                                String size, String color,
                                Double costPrice, Double sellingPrice, Double initialStock, List<String> barcodes) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.size = size;
        this.color = color;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.initialStock = initialStock;
        this.barcodes = barcodes;
    }

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
    public Double getCostPrice() { return costPrice; }
    public void setCostPrice(Double costPrice) { this.costPrice = costPrice; }
    public Double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(Double sellingPrice) { this.sellingPrice = sellingPrice; }
    public Double getInitialStock() { return initialStock; }
    public void setInitialStock(Double initialStock) { this.initialStock = initialStock; }
    public List<String> getBarcodes() { return barcodes; }
    public void setBarcodes(List<String> barcodes) { this.barcodes = barcodes; }
}
