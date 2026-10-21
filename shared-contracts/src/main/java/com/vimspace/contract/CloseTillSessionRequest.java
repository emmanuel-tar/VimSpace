package com.vimspace.contract;

/**
 * Request to close a till session (cash shift) with the counted closing cash.
 */
public class CloseTillSessionRequest {
    private Long sessionId;
    private Double countedCash;
    private String notes;

    public CloseTillSessionRequest() {}

    public CloseTillSessionRequest(Long sessionId, Double countedCash) {
        this.sessionId = sessionId;
        this.countedCash = countedCash;
    }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Double getCountedCash() { return countedCash; }
    public void setCountedCash(Double countedCash) { this.countedCash = countedCash; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
