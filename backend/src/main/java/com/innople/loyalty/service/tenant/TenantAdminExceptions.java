package com.innople.loyalty.service.tenant;

public final class TenantAdminExceptions {
    private TenantAdminExceptions() {
    }

    public static class TenantNotFoundException extends RuntimeException {
        public TenantNotFoundException(String message) {
            super(message);
        }
    }

    public static class TenantDeleteConflictException extends RuntimeException {
        public TenantDeleteConflictException(String message) {
            super(message);
        }
    }
}

