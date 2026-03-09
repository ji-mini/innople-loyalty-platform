package com.innople.loyalty.service.admin;

public interface AdminAuthService {
    AdminLoginResult login(String email, String password);

    AdminRegisterResult register(String email, String name, String password);
}

