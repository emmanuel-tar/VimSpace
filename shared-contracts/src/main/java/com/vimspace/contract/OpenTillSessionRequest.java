package com.vimspace.contract;

/**
 * Request to open a new till session (cash shift) for a terminal.
 */
public class OpenTillSessionRequest {
    private String terminalId;
    private String cashierId;
    private Double openingFloat;
    private String notes;

    public OpenTillSessionRequest() {}

    public OpenTillSessionRequest(String terminalId, String cashierId, Double openingFloat) {
        this.terminalId = terminalId;
        this.cashierId = cashierId;
        this.openingFloat = openingFloat;
    }

    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
    public String getCashierId() { return cashierId; }
    public void setCashierId(String cashierId) { this.cashierId = cashierId; }
    public Double getOpeningFloat() { return openingFloat; }
    public void setOpeningFloat(Double openingFloat) { this.openingFloat = openingFloat; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
