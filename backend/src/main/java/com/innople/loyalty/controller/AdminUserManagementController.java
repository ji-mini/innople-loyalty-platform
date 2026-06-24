package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.AdminUserManagementDtos;
import com.innople.loyalty.service.admin.AdminUserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/admin-users")
@RequiredArgsConstructor
public class AdminUserManagementController {

    private final AdminUserManagementService adminUserManagementService;

    @GetMapping
    public List<AdminUserManagementDtos.AdminUserResponse> list(@RequestParam(required = false) String keyword) {
        return adminUserManagementService.list(keyword).stream()
                .map(AdminUserManagementDtos.AdminUserResponse::from)
                .toList();
    }

    @PostMapping
    public AdminUserManagementDtos.AdminUserResponse create(@Valid @RequestBody AdminUserManagementDtos.CreateRequest request) {
        return AdminUserManagementDtos.AdminUserResponse.from(adminUserManagementService.create(
                request.phoneNumber(),
                request.email(),
                request.name(),
                request.password(),
                request.role()
        ));
    }

    @PutMapping("/{adminUserId}")
    public AdminUserManagementDtos.AdminUserResponse update(
            @PathVariable UUID adminUserId,
            @Valid @RequestBody AdminUserManagementDtos.UpdateRequest request
    ) {
        return AdminUserManagementDtos.AdminUserResponse.from(adminUserManagementService.update(
                adminUserId,
                request.phoneNumber(),
                request.email(),
                request.name(),
                request.role()
        ));
    }
}

