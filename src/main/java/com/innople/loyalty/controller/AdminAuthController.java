package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.AdminAuthDtos;
import com.innople.loyalty.service.admin.AdminLoginResult;
import com.innople.loyalty.service.admin.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public AdminAuthDtos.LoginResponse login(@Valid @RequestBody AdminAuthDtos.LoginRequest request) {
        AdminLoginResult result = adminAuthService.login(request.email(), request.password());
        return new AdminAuthDtos.LoginResponse(
                result.adminUserId(),
                result.email(),
                result.name(),
                result.accessToken()
        );
    }
}

