package com.innople.loyalty.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 회원 등록 시 휴대폰/이메일 인증 강제 여부 설정.
 * app.member.verification-required 프로퍼티로만 제어하며(하드코딩 금지),
 * 기본값은 false(개발 우회)이고 운영에서는 APP_MEMBER_VERIFICATION_REQUIRED=true로 주입한다.
 */
@Component
@ConfigurationProperties(prefix = "app.member")
public class MemberVerificationProperties {

    private boolean verificationRequired = false;

    public boolean isVerificationRequired() {
        return verificationRequired;
    }

    public void setVerificationRequired(boolean verificationRequired) {
        this.verificationRequired = verificationRequired;
    }
}
