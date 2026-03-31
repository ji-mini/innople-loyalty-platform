package com.innople.loyalty.controller;

import com.innople.loyalty.config.AdminRoleResolver;
import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.MembershipGradeDtos;
import com.innople.loyalty.domain.member.MembershipGrade;
import com.innople.loyalty.domain.user.AdminRole;
import com.innople.loyalty.repository.MembershipGradeRepository;
import com.innople.loyalty.service.member.MembershipGradeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/member-grades")
@RequiredArgsConstructor
public class MembershipGradeController {

    private final MembershipGradeRepository membershipGradeRepository;
    private final MembershipGradeService membershipGradeService;
    private final AdminRoleResolver adminRoleResolver;

    @GetMapping
    public List<MembershipGradeResponse> list() {
        UUID tenantId = TenantContext.requireTenantId();
        List<MembershipGrade> grades = membershipGradeRepository.findAllByTenantIdOrderByLevelDesc(tenantId);
        return grades.stream()
                .map(g -> new MembershipGradeResponse(
                        g.getId(),
                        String.valueOf(g.getLevel()),
                        g.getName(),
                        g.getDescription(),
                        g.getEarnRatePercent()
                ))
                .toList();
    }

    @PostMapping
    public MembershipGradeResponse create(
            HttpServletRequest request,
            @Valid @RequestBody MembershipGradeDtos.CreateRequest body
    ) {
        adminRoleResolver.requireAtLeast(request, AdminRole.ADMIN);
        var item = membershipGradeService.create(
                body.name(),
                body.level(),
                body.description(),
                body.earnRatePercent()
        );
        return new MembershipGradeResponse(item.id(), item.code(), item.name(), item.description(), item.earnRatePercent());
    }

    @PutMapping("/{id}")
    public MembershipGradeResponse update(
            HttpServletRequest request,
            @PathVariable UUID id,
            @Valid @RequestBody MembershipGradeDtos.UpdateRequest body
    ) {
        adminRoleResolver.requireAtLeast(request, AdminRole.ADMIN);
        var item = membershipGradeService.update(
                id,
                body.name(),
                body.level(),
                body.description(),
                body.earnRatePercent()
        );
        return new MembershipGradeResponse(item.id(), item.code(), item.name(), item.description(), item.earnRatePercent());
    }

    @DeleteMapping("/{id}")
    public void delete(HttpServletRequest request, @PathVariable UUID id) {
        adminRoleResolver.requireAtLeast(request, AdminRole.ADMIN);
        membershipGradeService.delete(id);
    }

    public record MembershipGradeResponse(
            UUID id,
            String code,
            String name,
            String description,
            java.math.BigDecimal earnRatePercent
    ) {
    }
}
