package com.innople.loyalty.service.admin;

import java.util.UUID;

public interface AdminAuthService {
    AdminLoginResult login(String phoneNumber, String password);

    AdminRegisterResult register(String phoneNumber, String email, String name, String password);

    /** 유효한 어드민 세션에 대해 새 액세스 토큰을 발급(슬라이딩 연장)합니다. */
    AdminLoginResult refresh(UUID adminUserId);
}

