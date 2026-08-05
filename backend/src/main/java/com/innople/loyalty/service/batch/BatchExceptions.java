package com.innople.loyalty.service.batch;

public final class BatchExceptions {
    private BatchExceptions() {
    }

    /** 배치 설정(batch_job_config)이 존재하지 않음 → 404. */
    public static class BatchConfigNotFoundException extends RuntimeException {
        public static final String CODE = "BATCH_CONFIG_NOT_FOUND";

        public BatchConfigNotFoundException(String message) {
            super(message);
        }
    }

    /** 동일 테넌트에 이미 해당 배치 설정이 존재함 → 409. */
    public static class BatchConfigAlreadyExistsException extends RuntimeException {
        public static final String CODE = "BATCH_CONFIG_ALREADY_EXISTS";

        public BatchConfigAlreadyExistsException(String message) {
            super(message);
        }
    }

    /** 배치가 이미 실행 중이라 수동 실행을 받을 수 없음 → 409. */
    public static class BatchAlreadyRunningException extends RuntimeException {
        public static final String CODE = "BATCH_ALREADY_RUNNING";

        public BatchAlreadyRunningException(String message) {
            super(message);
        }
    }

    /** 비활성화(enabled=false) 상태에서 수동 실행을 시도함 → 409. */
    public static class BatchDisabledException extends RuntimeException {
        public static final String CODE = "BATCH_DISABLED";

        public BatchDisabledException(String message) {
            super(message);
        }
    }
}
