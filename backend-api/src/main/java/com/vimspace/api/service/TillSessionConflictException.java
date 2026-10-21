package com.vimspace.api.service;

import com.vimspace.api.entity.TillSessionEntity;
import com.vimspace.contract.TillSessionDTO;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a till-session operation is not allowed in the current state
 * (e.g. opening a second session on a terminal that already has one open).
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class TillSessionConflictException extends RuntimeException {
    public TillSessionConflictException(String message) {
        super(message);
    }
}
