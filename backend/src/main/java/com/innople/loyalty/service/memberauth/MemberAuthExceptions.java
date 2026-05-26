package com.innople.loyalty.service.memberauth;

public final class MemberAuthExceptions {
    private MemberAuthExceptions() {
    }

    public static class MemberAlreadyExistsException extends RuntimeException {
        public MemberAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

    public static class MemberCredentialNotFoundException extends RuntimeException {
        public MemberCredentialNotFoundException(String message) {
            super(message);
        }
    }

    public static class MemberCredentialAlreadyExistsException extends RuntimeException {
        public MemberCredentialAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class AppLoginDisabledException extends RuntimeException {
        public AppLoginDisabledException(String message) {
            super(message);
        }
    }
}
