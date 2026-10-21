package com.vimspace.contract;

import java.util.Map;

public class DailySalesSummaryDTO {
    private Double totalSales;
    private Integer totalTransactions;
    private Map<String, Double> paymentBreakdown; // "Cash": 500.0, "Card": 350.0

    public DailySalesSummaryDTO() {}

    public DailySalesSummaryDTO(Double totalSales, Integer totalTransactions, Map<String, Double> paymentBreakdown) {
        this.totalSales = totalSales;
        this.totalTransactions = totalTransactions;
        this.paymentBreakdown = paymentBreakdown;
    }

    public Double getTotalSales() { return totalSales; }
    public void setTotalSales(Double totalSales) { this.totalSales = totalSales; }
    public Integer getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }
    public Map<String, Double> getPaymentBreakdown() { return paymentBreakdown; }
    public void setPaymentBreakdown(Map<String, Double> paymentBreakdown) { this.paymentBreakdown = paymentBreakdown; }
}

