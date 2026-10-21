package com.vimspace.contract;

import java.util.List;

public class PurchaseOrderDTO {
    private Long id;
    private String poNumber;
    private Long supplierId;
    private String supplierName;
    private String status; // DRAFT, ORDERED, RECEIVED, CANCELLED
    private String orderedDate;
    private String expectedDate;
    private String receivedDate;
    private Double totalAmount;
    private List<PurchaseOrderItemDTO> items;

    public PurchaseOrderDTO() {}

    public PurchaseOrderDTO(Long id, String poNumber, Long supplierId, String supplierName,
                            String status, String orderedDate, String expectedDate,
                            String receivedDate, Double totalAmount, List<PurchaseOrderItemDTO> items) {
        this.id = id;
        this.poNumber = poNumber;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.status = status;
        this.orderedDate = orderedDate;
        this.expectedDate = expectedDate;
        this.receivedDate = receivedDate;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOrderedDate() { return orderedDate; }
    public void setOrderedDate(String orderedDate) { this.orderedDate = orderedDate; }
    public String getExpectedDate() { return expectedDate; }
    public void setExpectedDate(String expectedDate) { this.expectedDate = expectedDate; }
    public String getReceivedDate() { return receivedDate; }
    public void setReceivedDate(String receivedDate) { this.receivedDate = receivedDate; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public List<PurchaseOrderItemDTO> getItems() { return items; }
    public void setItems(List<PurchaseOrderItemDTO> items) { this.items = items; }
}