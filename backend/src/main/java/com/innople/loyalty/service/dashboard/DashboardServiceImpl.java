package com.innople.loyalty.service.dashboard;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.DashboardDtos;
import com.innople.loyalty.domain.log.ApiAuditLog;
import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.domain.points.PointEventType;
import com.innople.loyalty.domain.points.PointLedger;
import com.innople.loyalty.repository.AdminUserRepository;
import com.innople.loyalty.repository.ApiAuditLogRepository;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.repository.PointAccountRepository;
import com.innople.loyalty.repository.PointLedgerRepository;
import com.innople.loyalty.service.tenant.TenantQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final MemberRepository memberRepository;
    private final PointAccountRepository pointAccountRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final ApiAuditLogRepository apiAuditLogRepository;
    private final AdminUserRepository adminUserRepository;
    private final TenantQueryService tenantQueryService;

    @Override
    @Transactional(readOnly = true)
    public DashboardDtos.DashboardResponse getDashboard() {
        UUID tenantId = TenantContext.requireTenantId();
        LocalDate today = LocalDate.now();
        ZoneId zone = ZoneId.systemDefault();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate nextMonthStart = monthStart.plusMonths(1);
        Instant monthFrom = monthStart.atStartOfDay(zone).toInstant();
        Instant monthTo = nextMonthStart.atStartOfDay(zone).toInstant();

        long thisMonthNewMembers = memberRepository.countByTenantIdAndCreatedAtBetween(tenantId, monthFrom, monthTo);
        long thisMonthEarn = pointLedgerRepository.sumEarnByTenantIdAndCreatedAtBetween(tenantId, monthFrom, monthTo);
        long thisMonthUse = pointLedgerRepository.sumUseByTenantIdAndCreatedAtBetween(tenantId, monthFrom, monthTo);
        long totalMembers = memberRepository.countByTenantIdAndStatusCodeNot(tenantId, MemberStatusCodes.WITHDRAWN);
        long totalPointBalance = pointAccountRepository.sumCurrentBalanceByTenantId(tenantId);

        String brand = tenantQueryService.findByTenantId(tenantId)
                .map(t -> t.name())
                .orElse("");

        List<PointLedger> recentLedgers = pointLedgerRepository.findTop20ByTenantIdOrderByCreatedAtDesc(tenantId);
        List<UUID> memberIds = recentLedgers.stream()
                .map(PointLedger::getMemberId)
                .distinct()
                .toList();
        Map<UUID, String> memberNoMap = memberRepository.findByTenantIdAndIdIn(tenantId, memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getMemberNo));

        List<DashboardDtos.RecentPointActivityResponse> recentPoints = recentLedgers.stream()
                .map(l -> new DashboardDtos.RecentPointActivityResponse(
                        l.getId().toString(),
                        l.getCreatedAt(),
                        memberNoMap.getOrDefault(l.getMemberId(), "-"),
                        brand,
                        toActivityType(l.getEventType()),
                        Math.abs(l.getAmount()),
                        l.getReason() != null ? l.getReason() : ""
                ))
                .toList();

        List<ApiAuditLog> recentLogs = apiAuditLogRepository.findTop20ByTenantIdOrderByCreatedAtDesc(tenantId);

        List<UUID> adminIds = recentLogs.stream()
                .map(ApiAuditLog::getAdminUserId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<UUID, String> adminNameMap = adminIds.stream()
                .flatMap(id -> adminUserRepository.findByTenantIdAndId(tenantId, id).stream())
                .collect(Collectors.toMap(a -> a.getId(), a -> a.getName()));

        List<DashboardDtos.RecentAdminActionResponse> recentAdmins = recentLogs.stream()
                .map(l -> new DashboardDtos.RecentAdminActionResponse(
                        l.getId().toString(),
                        l.getCreatedAt(),
                        l.getAdminUserId() != null ? adminNameMap.getOrDefault(l.getAdminUserId(), "-") : "-",
                        l.getHttpMethod() + " " + l.getPath(),
                        l.getPath()
                ))
                .toList();

        return new DashboardDtos.DashboardResponse(
                new DashboardDtos.DashboardSummaryResponse(
                        thisMonthNewMembers,
                        thisMonthEarn,
                        thisMonthUse,
                        totalMembers,
                        totalPointBalance
                ),
                recentPoints,
                recentAdmins
        );
    }

    private static String toActivityType(PointEventType eventType) {
        return switch (eventType) {
            case EARN, ADJUST_EARN -> "EARN";
            case USE, ADJUST_USE -> "USE";
            case EXPIRE_AUTO, EXPIRE_MANUAL -> "EXPIRE";
        };
    }
}
