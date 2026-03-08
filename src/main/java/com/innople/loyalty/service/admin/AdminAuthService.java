package com.innople.loyalty.service.admin;

public interface AdminAuthService {
    AdminLoginResult login(String email, String password);
}

