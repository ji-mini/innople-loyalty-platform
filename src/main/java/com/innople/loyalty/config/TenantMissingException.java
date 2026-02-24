package com.innople.loyalty.config;

public class TenantMissingException extends RuntimeException {
    public TenantMissingException(String message) {
        super(message);
    }
}

