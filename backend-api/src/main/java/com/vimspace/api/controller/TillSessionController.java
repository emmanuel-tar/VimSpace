package com.vimspace.api.controller;

import com.vimspace.api.service.TillSessionService;
import com.vimspace.contract.CashReconciliationDTO;
import com.vimspace.contract.CloseTillSessionRequest;
import com.vimspace.contract.OpenTillSessionRequest;
import com.vimspace.contract.TillSessionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Till session / cash-shift endpoints.
 *
 * <pre>
 *   POST /api/till-sessions                     open a session
 *   GET  /api/till-sessions/active              all open sessions
 *   GET  /api/till-sessions/active/{terminalId} open session for one terminal
 *   GET  /api/till-sessions/{id}                one session
 *   GET  /api/till-sessions                     history (optional ?status=)
 *   POST /api/till-sessions/close               close a session and reconcile
 *   GET  /api/till-sessions/{id}/reconciliation cash reconciliation for a session
 * </pre>
 */
@RestController
@RequestMapping("/api/till-sessions")
public class TillSessionController {

    private final TillSessionService tillSessionService;

    public TillSessionController(TillSessionService tillSessionService) {
        this.tillSessionService = tillSessionService;
    }

    @PostMapping
    public ResponseEntity<TillSessionDTO> openSession(@RequestBody OpenTillSessionRequest request) {
        TillSessionDTO session = tillSessionService.openSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @GetMapping("/active")
    public ResponseEntity<List<TillSessionDTO>> getActiveSessions() {
        return ResponseEntity.ok(tillSessionService.getActiveSessions());
    }

    @GetMapping("/active/{terminalId}")
    public ResponseEntity<TillSessionDTO> getActiveSessionByTerminal(@PathVariable("terminalId") String terminalId) {
        return tillSessionService.getActiveSessionByTerminal(terminalId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/close")
    public ResponseEntity<CashReconciliationDTO> closeSession(@RequestBody CloseTillSessionRequest request) {
        return ResponseEntity.ok(tillSessionService.closeSession(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TillSessionDTO> getSession(@PathVariable("id") Long id) {
        return tillSessionService.getSession(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<TillSessionDTO>> getHistory(
            @RequestParam(value = "status", required = false) String status) {
        return ResponseEntity.ok(tillSessionService.getHistory(status));
    }

    @GetMapping("/{id}/reconciliation")
    public ResponseEntity<CashReconciliationDTO> getReconciliation(@PathVariable("id") Long id) {
        return ResponseEntity.ok(tillSessionService.reconcile(id));
    }
}
