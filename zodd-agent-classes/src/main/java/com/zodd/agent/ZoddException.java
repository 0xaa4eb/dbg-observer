package com.zodd.agent;

public class ZoddException extends RuntimeException {

    public ZoddException(String message) {
        super(message);
    }

    public ZoddException(Throwable cause) {
        super(cause);
    }

    public ZoddException(String message, Throwable cause) {
        super(message, cause);
    }
}
