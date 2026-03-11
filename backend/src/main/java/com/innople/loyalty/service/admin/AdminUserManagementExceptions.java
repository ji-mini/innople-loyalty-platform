package com.innople.loyalty.service.admin;

public final class AdminUserManagementExceptions {
    private AdminUserManagementExceptions() {
    }

    public static class AdminUserNotFoundException extends RuntimeException {
        public AdminUserNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidAdminUserPhoneNumberException extends RuntimeException {
        public InvalidAdminUserPhoneNumberException(String message) {
            super(message);
        }
    }
}

