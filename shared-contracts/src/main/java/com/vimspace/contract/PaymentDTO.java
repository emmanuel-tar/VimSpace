package com.vimspace.contract;

public class PaymentDTO {
    private String method;
    private Double amount;
    public PaymentDTO() {}
    public PaymentDTO(String method, Double amount) {
        this.method = method; this.amount = amount;
    }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
