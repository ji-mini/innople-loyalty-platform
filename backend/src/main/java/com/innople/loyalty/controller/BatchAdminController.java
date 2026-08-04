package com.innople.loyalty.controller;

import com.innople.loyalty.config.AdminRoleResolver;
import com.innople.loyalty.controller.dto.BatchDtos;
import com.innople.loyalty.domain.batch.BatchExecutionHistory;
import com.innople.loyalty.domain.batch.BatchJobConfig;
import com.innople.loyalty.domain.batch.BatchNames;
import com.innople.loyalty.domain.user.AdminRole;
import com.innople.loyalty.domain.user.AdminUser;
import com.innople.loyalty.service.batch.AutoWithdrawalBatchService;
import com.innople.loyalty.service.batch.BatchJobConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

/**
 * 배치 설정/실행 이력/수동 실행 관리자 API. 전 엔드포인트 관리자 인증 가드(requireAtLeast ADMIN)를 적용한다.
 */
@RestController
@RequestMapping("/api/v1/admin/batches")
@RequiredArgsConstructor
public class BatchAdminController {

    private final AdminRoleResolver adminRoleResolver;
    private final BatchJobConfigService batchJobConfigService;
    private final AutoWithdrawalBatchService autoWithdrawalBatchService;

    @GetMapping("/configs")
    public List<BatchDtos.BatchJobConfigResponse> listConfigs(HttpServletRequest httpRequest) {
        adminRoleResolver.requireAtLeast(httpRequest, AdminRole.ADMIN);
        return batchJobConfigService.listConfigs().stream()
                .map(this::toConfigResponse)
                .toList();
    }

    @GetMapping("/configs/{batchName}")
    public BatchDtos.BatchJobConfigResponse getConfig(
            @PathVariable String batchName,
            HttpServletRequest httpRequest
    ) {
        adminRoleResolver.requireAtLeast(httpRequest, AdminRole.ADMIN);
        return toConfigResponse(batchJobConfigService.getConfig(batchName));
    }

    @PostMapping("/configs")
    public BatchDtos.BatchJobConfigResponse createConfig(
            @Valid @RequestBody BatchDtos.CreateBatchJobConfigRequest request,
            HttpServletRequest httpRequest
    ) {
        adminRoleResolver.requireAtLeast(httpRequest, AdminRole.ADMIN);
        UUID adminUserId = resolveAdminUserId(httpRequest);
        String batchName = (request.batchName() == null || request.batchName().isBlank())
                ? BatchNames.AUTO_WITHDRAWAL
                : request.batchName().trim();
        boolean enabled = Boolean.TRUE.equals(request.enabled());
        BatchJobConfigService.BatchJobConfigView created = batchJobConfigService.createConfig(
                batchName,
                enabled,
                request.runHour(),
                request.thresholdDays(),
                adminUserId
        );
        return toConfigResponse(created);
    }

    @PutMapping("/configs/{batchName}")
    public BatchDtos.BatchJobConfigResponse updateConfig(
            @PathVariable String batchName,
            @Valid @RequestBody BatchDtos.UpdateBatchJobConfigRequest request,
            HttpServletRequest httpRequest
    ) {
        adminRoleResolver.requireAtLeast(httpRequest, AdminRole.ADMIN);
        UUID adminUserId = resolveAdminUserId(httpRequest);
        BatchJobConfigService.BatchJobConfigView updated = batchJobConfigService.updateConfig(
                batchName,
                request.enabled(),
                request.runHour(),
                request.thresholdDays(),
                adminUserId
        );
        return toConfigResponse(updated);
    }

    @GetMapping("/executions")
    public BatchDtos.PagedResponse<BatchDtos.BatchExecutionHistoryResponse> listExecutions(
            @RequestParam(required = false) String batchName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest
    ) {
        adminRoleResolver.requireAtLeast(httpRequest, AdminRole.ADMIN);
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "startedAt")
        );
        Page<BatchExecutionHistory> result = batchJobConfigService.listExecutions(batchName, pageable);
        List<BatchDtos.BatchExecutionHistoryResponse> items = result.getContent().stream()
                .map(this::toHistoryResponse)
                .toList();
        return new BatchDtos.PagedResponse<>(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @PostMapping("/{batchName}/run")
    public BatchDtos.BatchExecutionHistoryResponse runManually(
            @PathVariable String batchName,
            HttpServletRequest httpRequest
    ) {
        adminRoleResolver.requireAtLeast(httpRequest, AdminRole.ADMIN);
        BatchExecutionHistory history = autoWithdrawalBatchService.runManualForCurrentTenant(batchName);
        return toHistoryResponse(history);
    }

    private UUID resolveAdminUserId(HttpServletRequest httpRequest) {
        AdminUser admin = adminRoleResolver.resolve(httpRequest);
        return admin != null ? admin.getId() : null;
    }

    private BatchDtos.BatchJobConfigResponse toConfigResponse(BatchJobConfigService.BatchJobConfigView view) {
        BatchJobConfig c = view.config();
        return new BatchDtos.BatchJobConfigResponse(
                c.getId(),
                c.getBatchName(),
                c.isEnabled(),
                c.getRunHour(),
                c.getThresholdDays(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getUpdatedBy(),
                view.lastExecutedAt()
        );
    }

    private BatchDtos.BatchExecutionHistoryResponse toHistoryResponse(BatchExecutionHistory h) {
        return new BatchDtos.BatchExecutionHistoryResponse(
                h.getId(),
                h.getBatchName(),
                h.getStatus().name(),
                h.getStartedAt(),
                h.getFinishedAt(),
                h.getProcessedCount(),
                h.getErrorCount(),
                h.getErrorMessage(),
                h.getCreatedAt()
        );
    }
}
