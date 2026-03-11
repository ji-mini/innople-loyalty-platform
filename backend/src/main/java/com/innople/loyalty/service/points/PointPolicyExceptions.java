package com.innople.loyalty.service.points;

public final class PointPolicyExceptions {
    private PointPolicyExceptions() {
    }

    public static class PointPolicyAlreadyExistsException extends RuntimeException {
        public PointPolicyAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class PointPolicyNotFoundException extends RuntimeException {
        public PointPolicyNotFoundException(String message) {
            super(message);
        }
    }
}

