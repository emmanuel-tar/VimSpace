package com.vimspace.boh.service;

/**
 * Thrown when the backend API returns an error status or a network error occurs.
 */
public class ApiException extends RuntimeException {

    private final Integer statusCode;

    public ApiException(String message) {
        super(message);
        this.statusCode = null;
    }

    public ApiException(String message, Integer statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ApiException(String message, Throwable cause, Integer statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public boolean isClientError() {
        return statusCode != null && statusCode >= 400 && statusCode < 500;
    }

    public boolean isServerError() {
        return statusCode != null && statusCode >= 500;
    }
}
