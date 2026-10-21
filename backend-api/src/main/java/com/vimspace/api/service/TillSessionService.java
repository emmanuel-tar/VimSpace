package com.vimspace.api.service;

import com.vimspace.api.entity.SaleEntity;
import com.vimspace.api.entity.TillSessionEntity;
import com.vimspace.api.repository.PaymentRepository;
import com.vimspace.api.repository.SaleRepository;
import com.vimspace.api.repository.TillSessionRepository;
import com.vimspace.contract.CashReconciliationDTO;
import com.vimspace.contract.CloseTillSessionRequest;
import com.vimspace.contract.OpenTillSessionRequest;
import com.vimspace.contract.TillSessionDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Till session (cash shift) lifecycle: open, query, close and reconcile.
 *
 * <p>Reconciliation rule: expected cash in the drawer equals the opening float
 * plus every cash payment tendered against sales that belong to the session.
 * The variance is the counted closing cash minus that expected figure.</p>
 */
@Service
public class TillSessionService {

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_CLOSED = "CLOSED";

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TillSessionRepository tillSessionRepository;
    private final SaleRepository saleRepository;
    private final PaymentRepository paymentRepository;

    public TillSessionService(TillSessionRepository tillSessionRepository,
                              SaleRepository saleRepository,
                              PaymentRepository paymentRepository) {
        this.tillSessionRepository = tillSessionRepository;
        this.saleRepository = saleRepository;
        this.paymentRepository = paymentRepository;
    }

    // ------------------------------------------------------------------ open

    @Transactional
    public TillSessionDTO openSession(OpenTillSessionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        String terminalId = trimToNull(request.getTerminalId());
        if (terminalId == null) {
            throw new IllegalArgumentException("terminalId is required");
        }
        String cashierId = trimToNull(request.getCashierId());
        if (cashierId == null) {
            throw new IllegalArgumentException("cashierId is required");
        }
        BigDecimal openingFloat = request.getOpeningFloat() == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(request.getOpeningFloat()).setScale(4, RoundingMode.HALF_UP);
        if (openingFloat.signum() < 0) {
            throw new IllegalArgumentException("openingFloat cannot be negative");
        }

        Optional<TillSessionEntity> existing =
                tillSessionRepository.findTopByTerminalIdAndStatusOrderByIdDesc(terminalId, STATUS_OPEN);
        if (existing.isPresent()) {
            throw new TillSessionConflictException(
                    "Terminal " + terminalId + " already has an open till session (#" + existing.get().getId() + ")");
        }

        TillSessionEntity session = new TillSessionEntity(terminalId, cashierId, openingFloat);
        session.setNotes(trimToNull(request.getNotes()));
        session.setExpectedCash(openingFloat);
        TillSessionEntity saved = tillSessionRepository.save(session);
        return toDTO(saved);
    }
    // ------------------------------------------------------------------ read

    public List<TillSessionDTO> getActiveSessions() {
        List<TillSessionDTO> result = new ArrayList<>();
        for (TillSessionEntity s : tillSessionRepository.findByStatusOrderByIdDesc(STATUS_OPEN)) {
            result.add(toDTO(s));
        }
        return result;
    }

    public Optional<TillSessionDTO> getActiveSessionByTerminal(String terminalId) {
        if (terminalId == null || terminalId.isBlank()) {
            return Optional.empty();
        }
        return tillSessionRepository
                .findTopByTerminalIdAndStatusOrderByIdDesc(terminalId.trim(), STATUS_OPEN)
                .map(this::toDTO);
    }

    public Optional<TillSessionDTO> getSession(Long id) {
        return tillSessionRepository.findById(id).map(this::toDTO);
    }

    /**
     * Session history. When {@code status} is supplied only sessions with that
     * status are returned; otherwise every session is returned newest first.
     */
    public List<TillSessionDTO> getHistory(String status) {
        List<TillSessionEntity> sessions;
        if (status == null || status.isBlank()) {
            sessions = tillSessionRepository.findAllByOrderByIdDesc();
        } else {
            String normalized = status.trim().toUpperCase();
            if (!STATUS_OPEN.equals(normalized) && !STATUS_CLOSED.equals(normalized)) {
                throw new IllegalArgumentException("status must be OPEN or CLOSED");
            }
            sessions = tillSessionRepository.findByStatusOrderByIdDesc(normalized);
        }
        List<TillSessionDTO> result = new ArrayList<>();
        for (TillSessionEntity s : sessions) {
            result.add(toDTO(s));
        }
        return result;
    }
    // ----------------------------------------------------------------- close

    @Transactional
    public CashReconciliationDTO closeSession(CloseTillSessionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        Long sessionId = request.getSessionId();
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId is required");
        }
        if (request.getCountedCash() == null) {
            throw new IllegalArgumentException("countedCash is required");
        }
        BigDecimal counted = BigDecimal.valueOf(request.getCountedCash()).setScale(4, RoundingMode.HALF_UP);
        if (counted.signum() < 0) {
            throw new IllegalArgumentException("countedCash cannot be negative");
        }

        TillSessionEntity session = tillSessionRepository.findById(sessionId)
                .orElseThrow(() -> new TillSessionNotFoundException("Till session not found: " + sessionId));

        if (STATUS_CLOSED.equalsIgnoreCase(session.getStatus())) {
            throw new TillSessionConflictException("Till session #" + sessionId + " is already closed");
        }

        BigDecimal cashSales = cashSalesTotal(session.getId());
        BigDecimal expected = nz(session.getOpeningFloat()).add(cashSales).setScale(4, RoundingMode.HALF_UP);

        session.setCountedCash(counted);
        session.setExpectedCash(expected);
        session.setClosedAt(LocalDateTime.now());
        session.setStatus(STATUS_CLOSED);
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            session.setNotes(request.getNotes().trim());
        }
        tillSessionRepository.save(session);

        return toReconciliation(session, cashSales, expected, counted, LocalDateTime.now());
    }
    // -------------------------------------------------------- reconciliation

    /**
     * Cash reconciliation for a session. Works for both open and closed
     * sessions; for an open session the counted cash is the live figure
     * currently in the drawer as last recorded.
     */
    public CashReconciliationDTO reconcile(Long sessionId) {
        TillSessionEntity session = tillSessionRepository.findById(sessionId)
                .orElseThrow(() -> new TillSessionNotFoundException("Till session not found: " + sessionId));

        BigDecimal cashSales = cashSalesTotal(session.getId());
        BigDecimal expected = nz(session.getOpeningFloat()).add(cashSales).setScale(4, RoundingMode.HALF_UP);
        BigDecimal counted = nz(session.getCountedCash());

        // Keep the expected figure current on open sessions so that clients
        // polling the active session always see live totals.
        if (!expected.equals(session.getExpectedCash())) {
            session.setExpectedCash(expected);
            tillSessionRepository.save(session);
        }

        return toReconciliation(session, cashSales, expected, counted,
                session.getClosedAt() != null ? session.getClosedAt() : LocalDateTime.now());
    }
    // ------------------------------------------------------------- internals

    private BigDecimal cashSalesTotal(Long sessionId) {
        BigDecimal cash = paymentRepository.sumCashBySessionId(sessionId);
        return nz(cash).setScale(4, RoundingMode.HALF_UP);
    }

    private CashReconciliationDTO toReconciliation(TillSessionEntity s, BigDecimal cashSales,
                                                   BigDecimal expected, BigDecimal counted,
                                                   LocalDateTime reconciledAt) {
        CashReconciliationDTO dto = new CashReconciliationDTO();
        dto.setSessionId(s.getId());
        dto.setTerminalId(s.getTerminalId());
        dto.setCashierId(s.getCashierId());
        dto.setOpeningFloat(nz(s.getOpeningFloat()).doubleValue());
        dto.setCashSalesTotal(cashSales.doubleValue());
        dto.setExpectedCash(expected.doubleValue());
        dto.setCountedCash(counted.doubleValue());
        double variance = counted.subtract(expected).doubleValue();
        dto.setVariance(round2(variance));
        dto.setSaleCount((int) saleRepository.countBySessionId(s.getId()));
        dto.setStatus(s.getStatus());
        dto.setOpenedAt(fmt(s.getOpenedAt()));
        dto.setClosedAt(fmt(s.getClosedAt()));
        dto.setReconciledAt(fmt(reconciledAt));
        dto.setBalanced(Math.abs(variance) < 0.005);
        dto.setNotes(s.getNotes());
        return dto;
    }
    private TillSessionDTO toDTO(TillSessionEntity s) {
        BigDecimal cashSales = cashSalesTotal(s.getId());
        BigDecimal expected = nz(s.getOpeningFloat()).add(cashSales).setScale(4, RoundingMode.HALF_UP);
        BigDecimal counted = nz(s.getCountedCash());
        double variance = counted.subtract(expected).doubleValue();

        return new TillSessionDTO(
                s.getId(),
                s.getTerminalId(),
                s.getCashierId(),
                nz(s.getOpeningFloat()).doubleValue(),
                counted.doubleValue(),
                expected.doubleValue(),
                round2(variance),
                cashSales.doubleValue(),
                (int) saleRepository.countBySessionId(s.getId()),
                s.getStatus(),
                fmt(s.getOpenedAt()),
                fmt(s.getClosedAt()),
                s.getNotes()
        );
    }

    private static String fmt(LocalDateTime t) {
        return t == null ? null : t.format(TS);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static double round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** Sales rung up against a given till session. */
    public List<SaleEntity> salesForSession(Long sessionId) {
        return saleRepository.findBySessionId(sessionId);
    }
}
