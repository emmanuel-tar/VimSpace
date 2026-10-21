package com.vimspace.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "till_sessions")
public class TillSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @Column(name = "terminal_id", nullable = false, length = 30)
    private String terminalId;

    @Column(name = "cashier_id", nullable = false, length = 50)
    private String cashierId;

    @Column(name = "opened_at", nullable = false, updatable = false)
    private LocalDateTime openedAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "opening_float", precision = 18, scale = 4, nullable = false)
    private BigDecimal openingFloat = BigDecimal.ZERO;

    @Column(name = "counted_cash", precision = 18, scale = 4)
    private BigDecimal countedCash = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    private String status = "OPEN"; // "OPEN", "CLOSED"

    @Column(name = "expected_cash", precision = 18, scale = 4)
    private BigDecimal expectedCash = BigDecimal.ZERO;

    @Column(name = "notes", length = 500)
    private String notes;

    public TillSessionEntity() {}

    public TillSessionEntity(String terminalId, String cashierId, BigDecimal openingFloat) {
        this.terminalId = terminalId;
        this.cashierId = cashierId;
        this.openingFloat = openingFloat;
        this.status = "OPEN";
        this.openedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
    public String getCashierId() { return cashierId; }
    public void setCashierId(String cashierId) { this.cashierId = cashierId; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public BigDecimal getOpeningFloat() { return openingFloat; }
    public void setOpeningFloat(BigDecimal openingFloat) { this.openingFloat = openingFloat; }
    public BigDecimal getCountedCash() { return countedCash; }
    public void setCountedCash(BigDecimal countedCash) { this.countedCash = countedCash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getExpectedCash() { return expectedCash; }
    public void setExpectedCash(BigDecimal expectedCash) { this.expectedCash = expectedCash; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
