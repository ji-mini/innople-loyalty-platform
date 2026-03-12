package com.innople.loyalty.service.code;

public final class CommonCodeExceptions {
    private CommonCodeExceptions() {
    }

    public static class CommonCodeAlreadyExistsException extends RuntimeException {
        public CommonCodeAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class CommonCodeNotFoundException extends RuntimeException {
        public CommonCodeNotFoundException(String message) {
            super(message);
        }
    }
}

