package com.example.opd.exception;

/** Thrown when a patient, appointment or consultation does not exist (HTTP 404). */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
