package com.vimspace.contract;

public class SaleResponse {
    private String receiptNumber;
    private String status;
    private Double total;
    public SaleResponse() {}
    public SaleResponse(String receiptNumber, String status, Double total) {
        this.receiptNumber = receiptNumber; this.status = status; this.total = total;
    }
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}
