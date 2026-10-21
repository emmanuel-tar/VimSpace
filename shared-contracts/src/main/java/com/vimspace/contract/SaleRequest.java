package com.vimspace.contract;

import java.util.List;

public class SaleRequest {
    private String cashierId;
    private String terminalId;
    private String idempotencyKey;
    private Long sessionId;
    private List<SaleLineDTO> lines;
    private List<PaymentDTO> payments;
    public SaleRequest() {}
    public SaleRequest(String cashierId, String terminalId, String idempotencyKey,
                       List<SaleLineDTO> lines, List<PaymentDTO> payments) {
        this.cashierId = cashierId; this.terminalId = terminalId;
        this.idempotencyKey = idempotencyKey; this.lines = lines; this.payments = payments;
    }
    public String getCashierId() { return cashierId; }
    public void setCashierId(String cashierId) { this.cashierId = cashierId; }
    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    /** Optional till session this sale belongs to. When null the backend links the terminal's open session. */
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public List<SaleLineDTO> getLines() { return lines; }
    public void setLines(List<SaleLineDTO> lines) { this.lines = lines; }
    public List<PaymentDTO> getPayments() { return payments; }
    public void setPayments(List<PaymentDTO> payments) { this.payments = payments; }
}
