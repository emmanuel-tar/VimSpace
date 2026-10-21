package com.vimspace.api.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a referenced till session does not exist.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TillSessionNotFoundException extends RuntimeException {
    public TillSessionNotFoundException(String message) {
        super(message);
    }
}
