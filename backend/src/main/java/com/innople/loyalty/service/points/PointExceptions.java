package com.innople.loyalty.service.points;

public final class PointExceptions {
    private PointExceptions() {
    }

    public static class InvalidPointAmountException extends RuntimeException {
        public InvalidPointAmountException(String message) {
            super(message);
        }
    }

    public static class PointAccountNotFoundException extends RuntimeException {
        public PointAccountNotFoundException(String message) {
            super(message);
        }
    }

    public static class InsufficientPointsException extends RuntimeException {
        public InsufficientPointsException(String message) {
            super(message);
        }
    }
}

