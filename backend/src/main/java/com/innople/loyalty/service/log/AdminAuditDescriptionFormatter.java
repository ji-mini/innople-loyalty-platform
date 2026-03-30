package com.innople.loyalty.service.log;

import com.innople.loyalty.domain.log.ApiAuditLog;

/**
 * 감사 로그를 대시보드·로그 화면에서 읽기 쉬운 한 줄 요약으로 변환합니다.
 */
public final class AdminAuditDescriptionFormatter {

    private AdminAuditDescriptionFormatter() {
    }

    public static String describe(ApiAuditLog log) {
        if (log.getMessage() != null && !log.getMessage().isBlank()) {
            return log.getMessage().trim();
        }
        return describeFallback(log.getHttpMethod(), log.getPath(), log.getStatusCode());
    }

    private static String describeFallback(String method, String path, int statusCode) {
        if (path == null || path.isBlank()) {
            return (method != null ? method : "?") + " 요청" + statusSuffix(statusCode);
        }
        String m = method != null ? method.toUpperCase() : "";
        String p = path;

        if (p.startsWith("/api/v1/admin/auth/login") && "POST".equals(m)) {
            return "관리자 로그인" + statusSuffix(statusCode);
        }
        if (p.startsWith("/api/v1/admin/auth/register") && "POST".equals(m)) {
            return "관리자 계정 등록" + statusSuffix(statusCode);
        }
        if (p.equals("/api/v1/members") && "POST".equals(m)) {
            return "회원 등록" + statusSuffix(statusCode);
        }
        if (p.startsWith("/api/v1/members/") && p.endsWith("/withdraw") && "PUT".equals(m)) {
            return "회원 탈퇴 처리" + statusSuffix(statusCode);
        }
        if (p.startsWith("/api/v1/members/") && p.endsWith("/status") && "PUT".equals(m)) {
            return "회원 상태 변경" + statusSuffix(statusCode);
        }
        if (p.startsWith("/api/v1/members/") && "PUT".equals(m) && !p.contains("/ledgers")) {
            return "회원 정보 수정" + statusSuffix(statusCode);
        }
        if (p.startsWith("/api/v1/points/earn") && "POST".equals(m)) {
            return "포인트 적립" + statusSuffix(statusCode);
        }
        if (p.startsWith("/api/v1/points/use") && "POST".equals(m)) {
            return "포인트 사용" + statusSuffix(statusCode);
        }
        if (p.startsWith("/api/v1/points/expire/manual") && "POST".equals(m)) {
            return "포인트 수동 소멸" + statusSuffix(statusCode);
        }
        if (p.startsWith("/api/v1/admin/point-policies") && "POST".equals(m)) {
            return "포인트 정책 등록" + statusSuffix(statusCode);
        }
        if (p.startsWith("/api/v1/admin/point-policies/") && "PUT".equals(m)) {
            return "포인트 정책 변경" + statusSuffix(statusCode);
        }
        if (p.startsWith("/api/v1/admin/points/expire/run") && "POST".equals(m)) {
            return "만료 배치 실행" + statusSuffix(statusCode);
        }
        if (p.startsWith("/api/v1/admin/points/reconcile") && "POST".equals(m)) {
            return "포인트 잔액 정합성 점검" + statusSuffix(statusCode);
        }
        if (p.startsWith("/api/v1/dashboard") && "GET".equals(m)) {
            return "대시보드 조회" + statusSuffix(statusCode);
        }
        if (p.startsWith("/api/v1/admin/logs") && "GET".equals(m)) {
            return "관리자 로그 조회" + statusSuffix(statusCode);
        }
        if (p.startsWith("/api/v1/admin/common-codes") && "POST".equals(m)) {
            return "공통코드 등록" + statusSuffix(statusCode);
        }
        if (p.startsWith("/api/v1/member-grades") && "POST".equals(m)) {
            return "회원 등급 등록" + statusSuffix(statusCode);
        }

        return friendlyVerb(m) + " · " + shortenPath(p) + statusSuffix(statusCode);
    }

    private static String statusSuffix(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) {
            return "";
        }
        if (statusCode >= 400) {
            return " (실패 " + statusCode + ")";
        }
        return "";
    }

    private static String friendlyVerb(String method) {
        return switch (method) {
            case "GET" -> "조회";
            case "POST" -> "등록/실행";
            case "PUT", "PATCH" -> "수정";
            case "DELETE" -> "삭제";
            default -> "요청";
        };
    }

    private static String shortenPath(String path) {
        if (path.length() <= 80) {
            return path;
        }
        return path.substring(0, 77) + "...";
    }
}
