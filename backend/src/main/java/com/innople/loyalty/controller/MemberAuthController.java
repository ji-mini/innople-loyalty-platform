package com.innople.loyalty.controller;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.MemberAuthDtos;
import com.innople.loyalty.service.memberauth.MemberAuthResult;
import com.innople.loyalty.service.memberauth.MemberAuthService;
import com.innople.loyalty.service.memberauth.MemberLoginContext;
import com.innople.loyalty.service.memberauth.MemberLoginCommand;
import com.innople.loyalty.service.memberauth.MemberSignupCommand;
import com.innople.loyalty.service.tenant.TenantValidationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class MemberAuthController {
    private final MemberAuthService memberAuthService;
    private final TenantValidationService tenantValidationService;

    @PostMapping("/signup")
    public MemberAuthDtos.AuthResponse signup(@Valid @RequestBody MemberAuthDtos.SignupRequest request) {
        validateTenantId(request.tenantId());
        MemberAuthResult result = memberAuthService.signup(
                new MemberSignupCommand(
                        request.name(),
                        request.email(),
                        request.password(),
                        request.phoneNumber() != null && !request.phoneNumber().isBlank() ? request.phoneNumber() : request.phone()
                )
        );
        return toResponse(result);
    }

    @PostMapping("/login")
    public MemberAuthDtos.AuthResponse login(@Valid @RequestBody MemberAuthDtos.LoginRequest request, HttpServletRequest httpRequest) {
        validateTenantId(request.tenantId());
        MemberAuthResult result = memberAuthService.login(
                new MemberLoginCommand(request.phoneNumber(), request.password()),
                new MemberLoginContext(
                        resolveDeviceName(httpRequest),
                        resolveOsName(httpRequest),
                        resolveClientIp(httpRequest),
                        httpRequest.getHeader("User-Agent")
                )
        );
        return toResponse(result);
    }

    private MemberAuthDtos.AuthResponse toResponse(MemberAuthResult result) {
        return new MemberAuthDtos.AuthResponse(
                result.accessToken(),
                new MemberAuthDtos.MemberInfo(
                        result.memberId(),
                        result.memberNo(),
                        result.name(),
                        result.email(),
                        result.phone(),
                        result.gradeName(),
                        result.pointBalance()
                )
        );
    }

    private void validateTenantId(String tenantIdFromBody) {
        UUID tenantId = TenantContext.requireTenantId();
        UUID bodyTenantId;
        try {
            bodyTenantId = UUID.fromString(tenantIdFromBody);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("tenantId 형식이 올바르지 않습니다.");
        }
        if (!tenantId.equals(bodyTenantId)) {
            throw new IllegalArgumentException("tenantId가 요청 헤더와 일치하지 않습니다.");
        }
        tenantValidationService.requireExistingTenant(tenantId);
    }

    private String resolveDeviceName(HttpServletRequest request) {
        String clientDevice = normalizeHeader(request.getHeader("X-Client-Device"));
        if (clientDevice != null) {
            return clientDevice;
        }

        String userAgent = normalizeHeader(request.getHeader("User-Agent"));
        if (userAgent == null) {
            return "Unknown Device";
        }
        String lowerUserAgent = userAgent.toLowerCase(Locale.ROOT);
        if (lowerUserAgent.contains("ipad")) {
            return "iPad";
        }
        if (lowerUserAgent.contains("iphone")) {
            return "iPhone";
        }
        if (lowerUserAgent.contains("android")) {
            return lowerUserAgent.contains("mobile") ? "Android Phone" : "Android Tablet";
        }
        if (lowerUserAgent.contains("windows")) {
            return "Windows PC";
        }
        if (lowerUserAgent.contains("macintosh") || lowerUserAgent.contains("mac os x")) {
            return "Mac";
        }
        if (lowerUserAgent.contains("linux")) {
            return "Linux PC";
        }
        return "Unknown Device";
    }

    private String resolveOsName(HttpServletRequest request) {
        String clientOs = normalizeHeader(request.getHeader("X-Client-OS"));
        if (clientOs != null) {
            return clientOs;
        }

        String userAgent = normalizeHeader(request.getHeader("User-Agent"));
        if (userAgent == null) {
            return "Unknown OS";
        }
        String lowerUserAgent = userAgent.toLowerCase(Locale.ROOT);
        if (lowerUserAgent.contains("iphone") || lowerUserAgent.contains("ipad") || lowerUserAgent.contains("cpu os")) {
            return "iOS";
        }
        if (lowerUserAgent.contains("android")) {
            return "Android";
        }
        if (lowerUserAgent.contains("windows")) {
            return "Windows";
        }
        if (lowerUserAgent.contains("macintosh") || lowerUserAgent.contains("mac os x")) {
            return "macOS";
        }
        if (lowerUserAgent.contains("linux")) {
            return "Linux";
        }
        return "Unknown OS";
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
