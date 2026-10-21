package com.vimspace.contract;

public class PurchaseOrderItemDTO {
    private String sku;
    private String productName;
    private Integer quantityOrdered;
    private Integer quantityReceived;
    private Double unitCost;
    private Double lineTotal;

    public PurchaseOrderItemDTO() {}

    public PurchaseOrderItemDTO(String sku, String productName, Integer quantityOrdered,
                                Integer quantityReceived, Double unitCost, Double lineTotal) {
        this.sku = sku;
        this.productName = productName;
        this.quantityOrdered = quantityOrdered;
        this.quantityReceived = quantityReceived;
        this.unitCost = unitCost;
        this.lineTotal = lineTotal;
    }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; }
    public Integer getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }
    public Double getUnitCost() { return unitCost; }
    public void setUnitCost(Double unitCost) { this.unitCost = unitCost; }
    public Double getLineTotal() { return lineTotal; }
    public void setLineTotal(Double lineTotal) { this.lineTotal = lineTotal; }
}