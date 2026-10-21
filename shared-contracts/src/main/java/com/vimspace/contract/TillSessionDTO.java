package com.vimspace.contract;

/**
 * Till session / cash shift.
 *
 * <p>All timestamps are formatted strings ({@code yyyy-MM-dd HH:mm:ss}) rather than
 * {@code LocalDateTime} so that plain {@code ObjectMapper} clients (the Swing
 * clients) can read them without registering a JavaTime module.</p>
 */
public class TillSessionDTO {
    private Long id;
    private String terminalId;
    private String cashierId;
    private Double openingFloat;
    private Double countedCash;
    private Double expectedCash;
    private Double variance;
    private Double cashSalesTotal;
    private Integer saleCount;
    private String status; // "OPEN", "CLOSED"
    private String openedAt;
    private String closedAt;
    private String notes;

    public TillSessionDTO() {}

    public TillSessionDTO(Long id, String terminalId, String cashierId, Double openingFloat,
                          Double countedCash, Double expectedCash, String status,
                          String openedAt, String closedAt) {
        this.id = id;
        this.terminalId = terminalId;
        this.cashierId = cashierId;
        this.openingFloat = openingFloat;
        this.countedCash = countedCash;
        this.expectedCash = expectedCash;
        this.status = status;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
    }

    /** Full constructor including reconciliation figures. */
    public TillSessionDTO(Long id, String terminalId, String cashierId, Double openingFloat,
                          Double countedCash, Double expectedCash, Double variance,
                          Double cashSalesTotal, Integer saleCount, String status,
                          String openedAt, String closedAt, String notes) {
        this(id, terminalId, cashierId, openingFloat, countedCash, expectedCash, status, openedAt, closedAt);
        this.variance = variance;
        this.cashSalesTotal = cashSalesTotal;
        this.saleCount = saleCount;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
    public String getCashierId() { return cashierId; }
    public void setCashierId(String cashierId) { this.cashierId = cashierId; }
    public Double getOpeningFloat() { return openingFloat; }
    public void setOpeningFloat(Double openingFloat) { this.openingFloat = openingFloat; }
    public Double getCountedCash() { return countedCash; }
    public void setCountedCash(Double countedCash) { this.countedCash = countedCash; }
    public Double getExpectedCash() { return expectedCash; }
    public void setExpectedCash(Double expectedCash) { this.expectedCash = expectedCash; }
    public Double getVariance() { return variance; }
    public void setVariance(Double variance) { this.variance = variance; }
    public Double getCashSalesTotal() { return cashSalesTotal; }
    public void setCashSalesTotal(Double cashSalesTotal) { this.cashSalesTotal = cashSalesTotal; }
    public Integer getSaleCount() { return saleCount; }
    public void setSaleCount(Integer saleCount) { this.saleCount = saleCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOpenedAt() { return openedAt; }
    public void setOpenedAt(String openedAt) { this.openedAt = openedAt; }
    public String getClosedAt() { return closedAt; }
    public void setClosedAt(String closedAt) { this.closedAt = closedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
