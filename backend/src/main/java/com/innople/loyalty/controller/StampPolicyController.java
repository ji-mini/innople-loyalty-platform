package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.StampDtos;
import com.innople.loyalty.service.stamp.StampPolicyAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/stamp-policies")
@RequiredArgsConstructor
public class StampPolicyController {

    private final StampPolicyAdminService stampPolicyAdminService;

    @GetMapping
    public List<StampDtos.StampPolicyResponse> list() {
        return stampPolicyAdminService.list();
    }

    @GetMapping("/{id}")
    public StampDtos.StampPolicyResponse get(@PathVariable UUID id) {
        return stampPolicyAdminService.get(id);
    }

    @PostMapping
    public StampDtos.StampPolicyResponse create(@Valid @RequestBody StampDtos.StampPolicyCreateRequest request) {
        return stampPolicyAdminService.create(request);
    }

    @PutMapping("/{id}")
    public StampDtos.StampPolicyResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody StampDtos.StampPolicyUpdateRequest request
    ) {
        return stampPolicyAdminService.update(id, request);
    }
}
