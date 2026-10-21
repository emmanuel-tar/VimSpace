package com.vimspace.api.controller;

import com.vimspace.api.service.TillSessionConflictException;
import com.vimspace.api.service.TillSessionNotFoundException;
import com.vimspace.contract.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates service-level failures into structured JSON error payloads so the
 * desktop clients can show a meaningful message instead of a raw stack trace.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(IllegalArgumentException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /** Malformed or unreadable request bodies are a client error, not a server error. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                                 HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request body", request);
    }

    @ExceptionHandler(TillSessionNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(TillSessionNotFoundException ex,
                                                           HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(TillSessionConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflict(TillSessionConflictException ex,
                                                           HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpected(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String message,
                                                   HttpServletRequest request) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                status.value(),
                status.getReasonPhrase(),
                message == null ? status.getReasonPhrase() : message,
                request == null ? null : request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
