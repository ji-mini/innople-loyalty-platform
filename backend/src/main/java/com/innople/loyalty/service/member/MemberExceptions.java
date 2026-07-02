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

    /**
     * 회원 등록 시 필수 인증(휴대폰/이메일)이 완료되지 않은 경우 발생.
     * verification-required=true 인 환경(운영)에서만 검증되며, 명확한 에러 코드를 함께 전달한다.
     */
    public static class MemberVerificationRequiredException extends RuntimeException {
        public static final String CODE_PHONE_NOT_VERIFIED = "PHONE_NOT_VERIFIED";
        public static final String CODE_EMAIL_NOT_VERIFIED = "EMAIL_NOT_VERIFIED";

        private final String code;

        public MemberVerificationRequiredException(String code, String message) {
            super(message);
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static MemberVerificationRequiredException phoneNotVerified() {
            return new MemberVerificationRequiredException(CODE_PHONE_NOT_VERIFIED, "휴대폰 인증이 완료되지 않았습니다.");
        }

        public static MemberVerificationRequiredException emailNotVerified() {
            return new MemberVerificationRequiredException(CODE_EMAIL_NOT_VERIFIED, "이메일 인증이 완료되지 않았습니다.");
        }
    }
}

