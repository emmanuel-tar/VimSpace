package com.vimspace.contract;

/**
 * Cash reconciliation result for a till session: how much cash the system
 * expected in the drawer versus what was physically counted.
 */
public class CashReconciliationDTO {
    private Long sessionId;
    private String terminalId;
    private String cashierId;
    private Double openingFloat;
    private Double cashSalesTotal;
    private Double expectedCash;
    private Double countedCash;
    /** countedCash - expectedCash. Negative means a shortfall. */
    private Double variance;
    private Integer saleCount;
    private String status;
    private String openedAt;
    private String closedAt;
    private String reconciledAt;
    private boolean balanced;
    private String notes;

    public CashReconciliationDTO() {}

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
    public String getCashierId() { return cashierId; }
    public void setCashierId(String cashierId) { this.cashierId = cashierId; }
    public Double getOpeningFloat() { return openingFloat; }
    public void setOpeningFloat(Double openingFloat) { this.openingFloat = openingFloat; }
    public Double getCashSalesTotal() { return cashSalesTotal; }
    public void setCashSalesTotal(Double cashSalesTotal) { this.cashSalesTotal = cashSalesTotal; }
    public Double getExpectedCash() { return expectedCash; }
    public void setExpectedCash(Double expectedCash) { this.expectedCash = expectedCash; }
    public Double getCountedCash() { return countedCash; }
    public void setCountedCash(Double countedCash) { this.countedCash = countedCash; }
    public Double getVariance() { return variance; }
    public void setVariance(Double variance) { this.variance = variance; }
    public Integer getSaleCount() { return saleCount; }
    public void setSaleCount(Integer saleCount) { this.saleCount = saleCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOpenedAt() { return openedAt; }
    public void setOpenedAt(String openedAt) { this.openedAt = openedAt; }
    public String getClosedAt() { return closedAt; }
    public void setClosedAt(String closedAt) { this.closedAt = closedAt; }
    public String getReconciledAt() { return reconciledAt; }
    public void setReconciledAt(String reconciledAt) { this.reconciledAt = reconciledAt; }
    public boolean isBalanced() { return balanced; }
    public void setBalanced(boolean balanced) { this.balanced = balanced; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
