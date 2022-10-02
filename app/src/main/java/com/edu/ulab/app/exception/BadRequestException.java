package com.edu.ulab.app.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super("Request is bad: " + message);
    }
}
