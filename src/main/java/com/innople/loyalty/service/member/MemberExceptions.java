package com.innople.loyalty.service.member;

public final class MemberExceptions {
    private MemberExceptions() {
    }

    public static class MemberAlreadyExistsException extends RuntimeException {
        public MemberAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class MemberNotFoundException extends RuntimeException {
        public MemberNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidMemberStatusException extends RuntimeException {
        public InvalidMemberStatusException(String message) {
            super(message);
        }
    }
}

