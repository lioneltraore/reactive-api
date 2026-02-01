package com.gexcode.reactive.exception;

public class EmailUniquenessException extends RuntimeException {

    public EmailUniquenessException(String message) {
        super(message);
    }

    public EmailUniquenessException(String message, Throwable cause) {
        super(message, cause);
    }
}
