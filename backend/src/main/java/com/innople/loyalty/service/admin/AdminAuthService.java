package com.innople.loyalty.service.admin;

public interface AdminAuthService {
    AdminLoginResult login(String phoneNumber, String password);

    AdminRegisterResult register(String phoneNumber, String email, String name, String password);
}

