package com.example.opd.exception;

/** Thrown when the request clashes with the current state, e.g. duplicate consultation (HTTP 409). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
