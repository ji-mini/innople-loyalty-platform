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
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    public AdminUserManagementDtos.AdminUserResponse create(@Valid @RequestBody AdminUserManagementDtos.CreateRequest request) {
        return toResponse(adminUserManagementService.create(
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
        return toResponse(adminUserManagementService.update(
                adminUserId,
                request.phoneNumber(),
                request.email(),
                request.name(),
                request.role()
        ));
    }

    private AdminUserManagementDtos.AdminUserResponse toResponse(AdminUserManagementService.AdminUserItem i) {
        return new AdminUserManagementDtos.AdminUserResponse(
                i.id(),
                i.phoneNumber(),
                i.email(),
                i.name(),
                i.role(),
                i.createdAt(),
                i.updatedAt()
        );
    }
}

