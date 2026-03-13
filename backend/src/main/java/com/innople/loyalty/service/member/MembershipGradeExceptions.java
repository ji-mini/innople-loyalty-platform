package com.innople.loyalty.service.member;

public final class MembershipGradeExceptions {

    private MembershipGradeExceptions() {
    }

    public static class MembershipGradeNotFoundException extends RuntimeException {
        public MembershipGradeNotFoundException(String message) {
            super(message);
        }
    }

    public static class LevelAlreadyExistsException extends RuntimeException {
        public LevelAlreadyExistsException(String message) {
            super(message);
        }
    }
}
