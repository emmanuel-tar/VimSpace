package com.vimspace.api.service;

import com.vimspace.api.entity.PaymentEntity;
import com.vimspace.api.entity.ProductEntity;
import com.vimspace.api.entity.SaleEntity;
import com.vimspace.api.entity.SaleLineEntity;
import com.vimspace.api.entity.TillSessionEntity;
import com.vimspace.api.repository.ProductRepository;
import com.vimspace.api.repository.SaleRepository;
import com.vimspace.api.repository.TillSessionRepository;
import com.vimspace.contract.CashReconciliationDTO;
import com.vimspace.contract.CloseTillSessionRequest;
import com.vimspace.contract.OpenTillSessionRequest;
import com.vimspace.contract.TillSessionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the till-session (cash shift) lifecycle.
 */
@SpringBootTest
@Transactional
class TillSessionServiceTest {

    @Autowired
    private TillSessionService tillSessionService;

    @Autowired
    private TillSessionRepository tillSessionRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    private ProductEntity milk;

    @BeforeEach
    void setUp() {
        milk = productRepository.findBySku("SKU-MILK-01")
                .orElseGet(() -> productRepository.save(new ProductEntity(
                        "SKU-MILK-01", "Full Cream Milk 1L", "Dairy", "1L", "White",
                        new BigDecimal("2.50"), new BigDecimal("3.50"), new BigDecimal("100"))));
    }

    private OpenTillSessionRequest openRequest(String terminal, double openingFloat) {
        return new OpenTillSessionRequest(terminal, "cashier", openingFloat);
    }

    /** Rungs up a sale with the given cash tender and links it to a session. */
    private void ringUpCashSale(Long sessionId, String receipt, double cashAmount) {
        SaleEntity sale = new SaleEntity();
        sale.setReceiptNumber(receipt);
        sale.setCashierId("cashier");
        sale.setTerminalId("POS-01");
        sale.setIdempotencyKey(receipt);
        sale.setSessionId(sessionId);
        sale.setStatus("completed");
        sale.setTotal(BigDecimal.valueOf(cashAmount));

        SaleLineEntity line = new SaleLineEntity(sale, milk, BigDecimal.ONE,
                BigDecimal.valueOf(cashAmount), BigDecimal.valueOf(cashAmount));
        sale.getLines().add(line);
        sale.getPayments().add(new PaymentEntity(sale, "Cash", BigDecimal.valueOf(cashAmount)));

        saleRepository.save(sale);
    }
    @Test
    @DisplayName("Opening a session seeds the expected cash with the opening float")
    void openSessionSeedsExpectedCash() {
        TillSessionDTO session = tillSessionService.openSession(openRequest("POS-A", 50.00));

        assertNotNull(session.getId());
        assertEquals("POS-A", session.getTerminalId());
        assertEquals("OPEN", session.getStatus());
        assertEquals(50.0, session.getOpeningFloat());
        assertEquals(50.0, session.getExpectedCash());
        assertEquals(0, session.getSaleCount());
    }

    @Test
    @DisplayName("Only one open session is allowed per terminal")
    void secondOpenSessionForSameTerminalConflicts() {
        tillSessionService.openSession(openRequest("POS-B", 25.00));

        TillSessionConflictException ex = assertThrows(TillSessionConflictException.class,
                () -> tillSessionService.openSession(openRequest("POS-B", 25.00)));
        assertTrue(ex.getMessage().contains("POS-B"));
    }

    @Test
    @DisplayName("Expected cash grows with the cash sales of the session")
    void expectedCashTracksCashSales() {
        TillSessionDTO session = tillSessionService.openSession(openRequest("POS-C", 40.00));
        ringUpCashSale(session.getId(), "RCP-C-1", 12.50);

        TillSessionDTO updated = tillSessionService.getSession(session.getId()).orElseThrow();

        assertEquals(12.50, updated.getCashSalesTotal());
        assertEquals(52.50, updated.getExpectedCash());
        assertEquals(1, updated.getSaleCount());
    }

    @Test
    @DisplayName("Closing a session reports the variance against the counted cash")
    void closeSessionReportsVariance() {
        TillSessionDTO session = tillSessionService.openSession(openRequest("POS-D", 30.00));
        ringUpCashSale(session.getId(), "RCP-D-1", 10.00);

        CashReconciliationDTO recon = tillSessionService.closeSession(
                new CloseTillSessionRequest(session.getId(), 38.00));

        assertEquals("CLOSED", recon.getStatus());
        assertEquals(30.0, recon.getOpeningFloat());
        assertEquals(10.0, recon.getCashSalesTotal());
        assertEquals(40.0, recon.getExpectedCash());
        assertEquals(38.0, recon.getCountedCash());
        assertEquals(-2.0, recon.getVariance());
        assertFalse(recon.isBalanced());
        assertNotNull(recon.getClosedAt());
    }
    @Test
    @DisplayName("A balanced close is reported as balanced")
    void closeSessionBalanced() {
        TillSessionDTO session = tillSessionService.openSession(openRequest("POS-E", 20.00));
        ringUpCashSale(session.getId(), "RCP-E-1", 5.00);

        CashReconciliationDTO recon = tillSessionService.closeSession(
                new CloseTillSessionRequest(session.getId(), 25.00));

        assertEquals(0.0, recon.getVariance());
        assertTrue(recon.isBalanced());
    }

    @Test
    @DisplayName("Cash sales of one session do not leak into another")
    void cashSalesAreScopedToTheirSession() {
        TillSessionDTO first = tillSessionService.openSession(openRequest("POS-F", 10.00));
        TillSessionDTO second = tillSessionService.openSession(openRequest("POS-G", 10.00));
        ringUpCashSale(first.getId(), "RCP-F-1", 7.00);

        TillSessionDTO other = tillSessionService.getSession(second.getId()).orElseThrow();

        assertEquals(0.0, other.getCashSalesTotal());
        assertEquals(10.0, other.getExpectedCash());
    }

    @Test
    @DisplayName("Reconciliation works for a session that is still open")
    void reconcileOpenSession() {
        TillSessionDTO session = tillSessionService.openSession(openRequest("POS-H", 15.00));
        ringUpCashSale(session.getId(), "RCP-H-1", 5.00);

        CashReconciliationDTO recon = tillSessionService.reconcile(session.getId());

        assertEquals("OPEN", recon.getStatus());
        assertEquals(20.0, recon.getExpectedCash());
        assertNull(recon.getClosedAt());
    }

    @Test
    @DisplayName("Reconciling an unknown session reports a not-found error")
    void reconcileUnknownSessionThrows() {
        assertThrows(TillSessionNotFoundException.class, () -> tillSessionService.reconcile(999999L));
    }

    @Test
    @DisplayName("A negative opening float is rejected")
    void negativeOpeningFloatRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> tillSessionService.openSession(openRequest("POS-K", -1.00)));
    }

    @Test
    @DisplayName("A terminal can open again once the previous session is closed")
    void terminalCanOpenAgainAfterClose() {
        TillSessionDTO first = tillSessionService.openSession(openRequest("POS-L", 5.00));
        tillSessionService.closeSession(new CloseTillSessionRequest(first.getId(), 5.00));

        TillSessionDTO second = tillSessionService.openSession(openRequest("POS-L", 8.00));

        assertNotEquals(first.getId(), second.getId());
        assertEquals("OPEN", second.getStatus());
        assertEquals(8.0, second.getOpeningFloat());
    }
    @Test
    @DisplayName("History can be filtered by status")
    void historyIsFilteredByStatus() {
        TillSessionDTO open = tillSessionService.openSession(openRequest("POS-I", 5.00));
        TillSessionDTO closed = tillSessionService.openSession(openRequest("POS-J", 5.00));
        tillSessionService.closeSession(new CloseTillSessionRequest(closed.getId(), 5.00));

        List<TillSessionDTO> openOnly = tillSessionService.getHistory("OPEN");
        List<TillSessionDTO> closedOnly = tillSessionService.getHistory("CLOSED");
        List<TillSessionDTO> all = tillSessionService.getHistory(null);

        assertTrue(openOnly.stream().allMatch(s -> "OPEN".equals(s.getStatus())));
        assertTrue(closedOnly.stream().allMatch(s -> "CLOSED".equals(s.getStatus())));
        assertTrue(openOnly.size() >= 1);
        assertTrue(closedOnly.size() >= 1);
        assertTrue(all.size() >= openOnly.size() + closedOnly.size());
        assertTrue(openOnly.stream().anyMatch(s -> s.getId().equals(open.getId())));
    }

    @Test
    @DisplayName("Closing a session stores the counted cash and expected cash on the entity")
    void closePersistsReconciliationFigures() {
        TillSessionDTO session = tillSessionService.openSession(openRequest("POS-M", 12.00));
        ringUpCashSale(session.getId(), "RCP-M-1", 3.00);

        tillSessionService.closeSession(new CloseTillSessionRequest(session.getId(), 15.00));

        TillSessionEntity entity = tillSessionRepository.findById(session.getId()).orElseThrow();
        assertEquals("CLOSED", entity.getStatus());
        assertEquals(0, new BigDecimal("15.00").compareTo(entity.getCountedCash()));
        assertEquals(0, new BigDecimal("15.00").compareTo(entity.getExpectedCash()));
        assertNotNull(entity.getClosedAt());
    }
}
