package com.innople.loyalty.service.memberauth;

public record MemberLoginContext(
        String deviceName,
        String osName,
        String ip,
        String userAgent
) {
}
