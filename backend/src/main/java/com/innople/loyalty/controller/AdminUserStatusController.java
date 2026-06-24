package com.innople.loyalty.controller;

import com.innople.loyalty.config.AdminRoleResolver;
import com.innople.loyalty.controller.dto.AdminUserManagementDtos;
import com.innople.loyalty.domain.user.AdminRole;
import com.innople.loyalty.service.admin.AdminUserManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 어드민 계정 상태(승인/비활성화) 변경 API.
 * JWT 검증 필터(AdminAuthFilter) 통과 후, SUPER_ADMIN 권한까지 확인한다.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserStatusController {

    private final AdminUserManagementService adminUserManagementService;
    private final AdminRoleResolver adminRoleResolver;

    @PatchMapping("/{adminUserId}/status")
    public AdminUserManagementDtos.AdminUserResponse updateStatus(
            @PathVariable UUID adminUserId,
            @Valid @RequestBody AdminUserManagementDtos.UpdateStatusRequest request,
            HttpServletRequest httpServletRequest
    ) {
        adminRoleResolver.requireAtLeast(httpServletRequest, AdminRole.SUPER_ADMIN);
        return AdminUserManagementDtos.AdminUserResponse.from(
                adminUserManagementService.updateStatus(adminUserId, request.status())
        );
    }
}
