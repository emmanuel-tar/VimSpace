package com.vimspace.contract;

public class InventoryMovementDTO {
    private Long id;
    private String productSku;
    private String productName;
    private String type;
    private String referenceId;
    private Double quantityDelta;
    private String reason;
    private String createdBy;
    private String createdAt;

    public InventoryMovementDTO() {}

    public InventoryMovementDTO(Long id, String productSku, String productName, String type,
                                String referenceId, Double quantityDelta, String reason, String createdBy, String createdAt) {
        this.id = id;
        this.productSku = productSku;
        this.productName = productName;
        this.type = type;
        this.referenceId = referenceId;
        this.quantityDelta = quantityDelta;
        this.reason = reason;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public Double getQuantityDelta() { return quantityDelta; }
    public void setQuantityDelta(Double quantityDelta) { this.quantityDelta = quantityDelta; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

