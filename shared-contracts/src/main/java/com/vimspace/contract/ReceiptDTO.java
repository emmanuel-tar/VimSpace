package com.vimspace.contract;

import java.time.LocalDateTime;
import java.util.List;

public class ReceiptDTO {
    private String receiptNumber;
    private String storeName;
    private String terminalId;
    private String cashierId;
    private LocalDateTime dateTime;
    private List<SaleLineDTO> lines;
    private List<PaymentDTO> payments;
    private Double subtotal;
    private Double tax;
    private Double total;

    public ReceiptDTO() {}

    public ReceiptDTO(String receiptNumber, String storeName, String terminalId, String cashierId,
                      LocalDateTime dateTime, List<SaleLineDTO> lines, List<PaymentDTO> payments,
                      Double subtotal, Double tax, Double total) {
        this.receiptNumber = receiptNumber;
        this.storeName = storeName;
        this.terminalId = terminalId;
        this.cashierId = cashierId;
        this.dateTime = dateTime;
        this.lines = lines;
        this.payments = payments;
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = total;
    }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
    public String getCashierId() { return cashierId; }
    public void setCashierId(String cashierId) { this.cashierId = cashierId; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public List<SaleLineDTO> getLines() { return lines; }
    public void setLines(List<SaleLineDTO> lines) { this.lines = lines; }
    public List<PaymentDTO> getPayments() { return payments; }
    public void setPayments(List<PaymentDTO> payments) { this.payments = payments; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public Double getTax() { return tax; }
    public void setTax(Double tax) { this.tax = tax; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}
