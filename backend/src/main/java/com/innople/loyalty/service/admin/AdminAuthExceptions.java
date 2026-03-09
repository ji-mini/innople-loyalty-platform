package com.innople.loyalty.service.admin;

public final class AdminAuthExceptions {
    private AdminAuthExceptions() {
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

    public static class AdminUserAlreadyExistsException extends RuntimeException {
        public AdminUserAlreadyExistsException(String message) {
            super(message);
        }
    }
}

