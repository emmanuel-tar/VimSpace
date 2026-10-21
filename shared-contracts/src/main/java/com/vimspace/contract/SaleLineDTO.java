package com.vimspace.contract;

public class SaleLineDTO {
    private String productSku;
    private Integer quantity;
    private Double unitPrice;
    private Double lineTotal;
    public SaleLineDTO() {}
    public SaleLineDTO(String productSku, Integer quantity, Double unitPrice, Double lineTotal) {
        this.productSku = productSku; this.quantity = quantity;
        this.unitPrice = unitPrice; this.lineTotal = lineTotal;
    }
    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    public Double getLineTotal() { return lineTotal; }
    public void setLineTotal(Double lineTotal) { this.lineTotal = lineTotal; }
}
