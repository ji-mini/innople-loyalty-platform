package com.innople.loyalty.controller;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.member.MembershipGrade;
import com.innople.loyalty.repository.MembershipGradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/member-grades")
@RequiredArgsConstructor
public class MembershipGradeController {

    private final MembershipGradeRepository membershipGradeRepository;

    @GetMapping
    public List<MembershipGradeResponse> list() {
        UUID tenantId = TenantContext.requireTenantId();
        List<MembershipGrade> grades = membershipGradeRepository.findAllByTenantIdOrderByLevelAsc(tenantId);
        return grades.stream()
                .map(g -> new MembershipGradeResponse(
                        g.getId(),
                        String.valueOf(g.getLevel()),
                        g.getName(),
                        g.getDescription()
                ))
                .toList();
    }

    public record MembershipGradeResponse(
            UUID id,
            String code,
            String name,
            String description
    ) {
    }
}
