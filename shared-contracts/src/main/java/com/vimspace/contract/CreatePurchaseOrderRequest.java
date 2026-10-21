package com.vimspace.contract;

import java.util.List;

public class CreatePurchaseOrderRequest {
    private Long supplierId;
    private String expectedDate;
    private List<PurchaseOrderItemRequest> items;

    public CreatePurchaseOrderRequest() {}

    public CreatePurchaseOrderRequest(Long supplierId, String expectedDate, List<PurchaseOrderItemRequest> items) {
        this.supplierId = supplierId;
        this.expectedDate = expectedDate;
        this.items = items;
    }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getExpectedDate() { return expectedDate; }
    public void setExpectedDate(String expectedDate) { this.expectedDate = expectedDate; }
    public List<PurchaseOrderItemRequest> getItems() { return items; }
    public void setItems(List<PurchaseOrderItemRequest> items) { this.items = items; }

    public static class PurchaseOrderItemRequest {
        private String sku;
        private Integer quantityOrdered;
        private Double unitCost;

        public PurchaseOrderItemRequest() {}

        public PurchaseOrderItemRequest(String sku, Integer quantityOrdered, Double unitCost) {
            this.sku = sku;
            this.quantityOrdered = quantityOrdered;
            this.unitCost = unitCost;
        }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public Integer getQuantityOrdered() { return quantityOrdered; }
        public void setQuantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; }
        public Double getUnitCost() { return unitCost; }
        public void setUnitCost(Double unitCost) { this.unitCost = unitCost; }
    }
}