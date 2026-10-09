package com.example.opd.exception;

/** Thrown when a business rule fails, e.g. fewer than two vitals on completion (HTTP 400). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
