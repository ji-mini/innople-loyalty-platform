package com.innople.loyalty.service.dashboard;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.DashboardDtos;
import com.innople.loyalty.domain.dashboard.DashboardMonthlyGoal;
import com.innople.loyalty.domain.log.ApiAuditLog;
import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.domain.points.PointEventType;
import com.innople.loyalty.domain.points.PointLedger;
import com.innople.loyalty.repository.AdminUserRepository;
import com.innople.loyalty.repository.ApiAuditLogRepository;
import com.innople.loyalty.repository.DashboardMonthlyGoalRepository;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.repository.PointAccountRepository;
import com.innople.loyalty.repository.PointLedgerRepository;
import com.innople.loyalty.repository.PointLotRepository;
import com.innople.loyalty.service.log.AdminAuditDescriptionFormatter;
import com.innople.loyalty.service.tenant.TenantQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.YearMonth;
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
    private final PointLotRepository pointLotRepository;
    private final ApiAuditLogRepository apiAuditLogRepository;
    private final AdminUserRepository adminUserRepository;
    private final DashboardMonthlyGoalRepository dashboardMonthlyGoalRepository;
    private final TenantQueryService tenantQueryService;

    @Override
    @Transactional(readOnly = true)
    public DashboardDtos.DashboardResponse getDashboard() {
        UUID tenantId = TenantContext.requireTenantId();
        LocalDate today = LocalDate.now();
        ZoneId zone = ZoneId.systemDefault();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate prevMonthStart = monthStart.minusMonths(1);
        LocalDate nextMonthStart = monthStart.plusMonths(1);
        LocalDate tomorrow = today.plusDays(1);
        Instant prevMonthFrom = prevMonthStart.atStartOfDay(zone).toInstant();
        Instant prevMonthTo = monthStart.atStartOfDay(zone).toInstant();
        Instant todayFrom = today.atStartOfDay(zone).toInstant();
        Instant todayTo = tomorrow.atStartOfDay(zone).toInstant();
        Instant monthFrom = monthStart.atStartOfDay(zone).toInstant();
        Instant monthTo = nextMonthStart.atStartOfDay(zone).toInstant();
        Instant nowInstant = Instant.now();
        Instant expiringDeadline = nowInstant.plusSeconds(7L * 24 * 60 * 60);
        long pointsExpiring7d = pointLotRepository.sumRemainingAmountExpiringBetween(tenantId, nowInstant, expiringDeadline);
        long membersExpiring7d = pointLotRepository.countDistinctMembersWithLotsExpiringBetween(tenantId, nowInstant, expiringDeadline);

        long todayNewMembers = memberRepository.countByTenantIdAndCreatedAtBetween(tenantId, todayFrom, todayTo);
        long todayEarn = pointLedgerRepository.sumEarnByTenantIdAndCreatedAtBetween(tenantId, todayFrom, todayTo);
        long todayUse = pointLedgerRepository.sumUseByTenantIdAndCreatedAtBetween(tenantId, todayFrom, todayTo);
        long prevMonthNewMembers = memberRepository.countByTenantIdAndCreatedAtBetween(tenantId, prevMonthFrom, prevMonthTo);
        long thisMonthNewMembers = memberRepository.countByTenantIdAndCreatedAtBetween(tenantId, monthFrom, monthTo);
        long prevMonthEarn = pointLedgerRepository.sumEarnByTenantIdAndCreatedAtBetween(tenantId, prevMonthFrom, prevMonthTo);
        long thisMonthEarn = pointLedgerRepository.sumEarnByTenantIdAndCreatedAtBetween(tenantId, monthFrom, monthTo);
        long prevMonthUse = pointLedgerRepository.sumUseByTenantIdAndCreatedAtBetween(tenantId, prevMonthFrom, prevMonthTo);
        long thisMonthUse = pointLedgerRepository.sumUseByTenantIdAndCreatedAtBetween(tenantId, monthFrom, monthTo);
        long avgNewMembers = averageRecentMonthsNewMembers(tenantId, zone, monthStart, 3);
        long avgEarn = averageRecentMonthsEarn(tenantId, zone, monthStart, 3);
        long avgUse = averageRecentMonthsUse(tenantId, zone, monthStart, 3);
        long totalMembers = memberRepository.countByTenantIdAndStatusCodeNot(tenantId, MemberStatusCodes.WITHDRAWN);
        long totalPointBalance = pointAccountRepository.sumCurrentBalanceByTenantId(tenantId);
        DashboardDtos.DashboardGoalResponse currentGoal = getCurrentGoal();

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
                        AdminAuditDescriptionFormatter.describe(l),
                        ""
                ))
                .toList();

        return new DashboardDtos.DashboardResponse(
                new DashboardDtos.DashboardSummaryResponse(
                        thisMonthNewMembers,
                        prevMonthNewMembers,
                        avgNewMembers,
                        thisMonthEarn,
                        prevMonthEarn,
                        avgEarn,
                        thisMonthUse,
                        prevMonthUse,
                        avgUse,
                        currentGoal.targetNewMembers(),
                        currentGoal.targetEarn(),
                        currentGoal.targetUse(),
                        totalMembers,
                        totalPointBalance
                ),
                new DashboardDtos.TodayStatusResponse(
                        todayEarn,
                        todayUse,
                        todayNewMembers
                ),
                new DashboardDtos.ExpiringPointsSummaryResponse(
                        pointsExpiring7d,
                        membersExpiring7d
                ),
                recentPoints,
                recentAdmins
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardDtos.DashboardGoalResponse getCurrentGoal() {
        UUID tenantId = TenantContext.requireTenantId();
        YearMonth currentMonth = YearMonth.now();
        return dashboardMonthlyGoalRepository
                .findByTenantIdAndTargetYearAndTargetMonth(tenantId, currentMonth.getYear(), currentMonth.getMonthValue())
                .map(this::toGoalResponse)
                .orElseGet(() -> new DashboardDtos.DashboardGoalResponse(
                        currentMonth.getYear(),
                        currentMonth.getMonthValue(),
                        0,
                        0,
                        0
                ));
    }

    @Override
    @Transactional
    public DashboardDtos.DashboardGoalResponse upsertCurrentGoal(long targetNewMembers, long targetEarn, long targetUse) {
        UUID tenantId = TenantContext.requireTenantId();
        YearMonth currentMonth = YearMonth.now();
        DashboardMonthlyGoal goal = dashboardMonthlyGoalRepository
                .findByTenantIdAndTargetYearAndTargetMonth(tenantId, currentMonth.getYear(), currentMonth.getMonthValue())
                .orElseGet(() -> new DashboardMonthlyGoal(
                        currentMonth.getYear(),
                        currentMonth.getMonthValue(),
                        targetNewMembers,
                        targetEarn,
                        targetUse
                ));
        goal.changeTargets(targetNewMembers, targetEarn, targetUse);
        DashboardMonthlyGoal saved = dashboardMonthlyGoalRepository.save(goal);
        return toGoalResponse(saved);
    }

    private static String toActivityType(PointEventType eventType) {
        return switch (eventType) {
            case EARN, ADJUST_EARN -> "EARN";
            case USE, ADJUST_USE -> "USE";
            case EXPIRE_AUTO, EXPIRE_MANUAL -> "EXPIRE";
        };
    }

    private DashboardDtos.DashboardGoalResponse toGoalResponse(DashboardMonthlyGoal goal) {
        return new DashboardDtos.DashboardGoalResponse(
                goal.getTargetYear(),
                goal.getTargetMonth(),
                goal.getTargetNewMembers(),
                goal.getTargetEarn(),
                goal.getTargetUse()
        );
    }

    private long averageRecentMonthsNewMembers(UUID tenantId, ZoneId zone, LocalDate currentMonthStart, int months) {
        long total = 0;
        for (int i = 1; i <= months; i++) {
            LocalDate fromDate = currentMonthStart.minusMonths(i);
            LocalDate toDate = currentMonthStart.minusMonths(i - 1);
            total += memberRepository.countByTenantIdAndCreatedAtBetween(
                    tenantId,
                    fromDate.atStartOfDay(zone).toInstant(),
                    toDate.atStartOfDay(zone).toInstant()
            );
        }
        return Math.round((double) total / months);
    }

    private long averageRecentMonthsEarn(UUID tenantId, ZoneId zone, LocalDate currentMonthStart, int months) {
        long total = 0;
        for (int i = 1; i <= months; i++) {
            LocalDate fromDate = currentMonthStart.minusMonths(i);
            LocalDate toDate = currentMonthStart.minusMonths(i - 1);
            total += pointLedgerRepository.sumEarnByTenantIdAndCreatedAtBetween(
                    tenantId,
                    fromDate.atStartOfDay(zone).toInstant(),
                    toDate.atStartOfDay(zone).toInstant()
            );
        }
        return Math.round((double) total / months);
    }

    private long averageRecentMonthsUse(UUID tenantId, ZoneId zone, LocalDate currentMonthStart, int months) {
        long total = 0;
        for (int i = 1; i <= months; i++) {
            LocalDate fromDate = currentMonthStart.minusMonths(i);
            LocalDate toDate = currentMonthStart.minusMonths(i - 1);
            total += pointLedgerRepository.sumUseByTenantIdAndCreatedAtBetween(
                    tenantId,
                    fromDate.atStartOfDay(zone).toInstant(),
                    toDate.atStartOfDay(zone).toInstant()
            );
        }
        return Math.round((double) total / months);
    }
}
