package com.vimspace.contract;

import java.util.List;

public class SaleDetailDTO {
    private String receiptNumber;
    private String terminalId;
    private String cashierId;
    private String createdAt;
    private Double total;
    private Double tax;
    private Double discount;
    private String status;
    private List<SaleLineDTO> lines;
    private List<PaymentDTO> payments;

    public SaleDetailDTO() {}

    public SaleDetailDTO(String receiptNumber, String terminalId, String cashierId, String createdAt,
                         Double total, Double tax, Double discount, String status,
                         List<SaleLineDTO> lines, List<PaymentDTO> payments) {
        this.receiptNumber = receiptNumber;
        this.terminalId = terminalId;
        this.cashierId = cashierId;
        this.createdAt = createdAt;
        this.total = total;
        this.tax = tax;
        this.discount = discount;
        this.status = status;
        this.lines = lines;
        this.payments = payments;
    }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
    public String getCashierId() { return cashierId; }
    public void setCashierId(String cashierId) { this.cashierId = cashierId; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public Double getTax() { return tax; }
    public void setTax(Double tax) { this.tax = tax; }
    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<SaleLineDTO> getLines() { return lines; }
    public void setLines(List<SaleLineDTO> lines) { this.lines = lines; }
    public List<PaymentDTO> getPayments() { return payments; }
    public void setPayments(List<PaymentDTO> payments) { this.payments = payments; }
}

