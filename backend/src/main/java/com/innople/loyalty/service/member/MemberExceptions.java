package com.innople.loyalty.service.member;

public final class MemberExceptions {
    private MemberExceptions() {
    }

    public static class MemberAlreadyExistsException extends RuntimeException {
        public MemberAlreadyExistsException(String message) {
            super(message);
        }
    }

    /**
     * 테넌트 스코프 내에서 이미 등록된 휴대폰번호로 회원 등록을 시도한 경우 발생.
     * 에러 코드({@code PHONE_ALREADY_EXISTS})는 PHONE_NOT_VERIFIED 등 기존 회원 도메인 에러코드 패턴을 따른다.
     */
    public static class MemberPhoneAlreadyExistsException extends RuntimeException {
        public static final String CODE = "PHONE_ALREADY_EXISTS";
        // 프론트(중복 체크/폼 검증) 문구와 일관되게 유지한다.
        public static final String MESSAGE = "이미 등록된 휴대폰 번호입니다.";

        private final String code;

        public MemberPhoneAlreadyExistsException() {
            super(MESSAGE);
            this.code = CODE;
        }

        public String getCode() {
            return code;
        }
    }

    public static class MemberNotFoundException extends RuntimeException {
        public static final String CODE = "MEMBER_NOT_FOUND";

        public MemberNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidMemberStatusException extends RuntimeException {
        public static final String CODE = "INVALID_MEMBER_STATUS";

        public InvalidMemberStatusException(String message) {
            super(message);
        }
    }

    /**
     * 회원 상태상 수기 포인트 적립/차감이 허용되지 않는 경우 발생.
     * 에러 코드({@code MEMBER_STATUS_NOT_ALLOWED})와 메시지는 프론트가 그대로 노출 가능하도록 전달한다.
     * (MemberPhoneAlreadyExistsException 의 code 전달 패턴을 따른다.)
     */
    public static class MemberStatusNotAllowedException extends RuntimeException {
        public static final String CODE = "MEMBER_STATUS_NOT_ALLOWED";

        private final String code;

        public MemberStatusNotAllowedException(String message) {
            super(message);
            this.code = CODE;
        }

        public String getCode() {
            return code;
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

