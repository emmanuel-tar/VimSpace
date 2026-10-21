package com.vimspace.contract;

public class StockAdjustmentRequest {
    private String sku;
    private Double quantityDelta; // positive or negative
    private String reason; // e.g., "Damage", "Expired", "Shrinkage", "Count Correction"
    private String user;

    public StockAdjustmentRequest() {}

    public StockAdjustmentRequest(String sku, Double quantityDelta, String reason, String user) {
        this.sku = sku;
        this.quantityDelta = quantityDelta;
        this.reason = reason;
        this.user = user;
    }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Double getQuantityDelta() { return quantityDelta; }
    public void setQuantityDelta(Double quantityDelta) { this.quantityDelta = quantityDelta; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
}

