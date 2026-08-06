package com.innople.loyalty.service.dashboard;

import com.innople.loyalty.common.AppTimeZones;
import com.innople.loyalty.common.DateRangeUtils;
import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.AdminDashboardDtos;
import com.innople.loyalty.domain.batch.BatchExecutionHistory;
import com.innople.loyalty.domain.batch.BatchJobConfig;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.repository.BatchExecutionHistoryRepository;
import com.innople.loyalty.repository.BatchJobConfigRepository;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.repository.PointLedgerRepository;
import com.innople.loyalty.repository.PointLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 관리자 대시보드 조회 서비스.
 *
 * <p>모든 기간 경계는 {@link DateRangeUtils} 를 경유해 KST half-open 구간 [start, endExclusive) 으로 산출한다.
 * 일자별 추이는 DB GROUP BY 로 한 번에 집계해 일수만큼 반복 질의하지 않는다.</p>
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final String UNASSIGNED_GRADE_KEY = "UNASSIGNED";
    private static final String UNASSIGNED_GRADE_LABEL = "등급 미지정";

    private final MemberRepository memberRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final PointLotRepository pointLotRepository;
    private final BatchJobConfigRepository batchJobConfigRepository;
    private final BatchExecutionHistoryRepository batchExecutionHistoryRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDtos.OverviewResponse getOverview() {
        UUID tenantId = TenantContext.requireTenantId();
        LocalDate today = LocalDate.now(AppTimeZones.KST);
        LocalDate yesterday = today.minusDays(1);

        DateRangeUtils.Range monthToDate = DateRangeUtils.kstMonthToDateRange(today);
        DateRangeUtils.Range previousMonthToDate = DateRangeUtils.kstPreviousMonthToDateRange(today);
        DateRangeUtils.Range todayRange = DateRangeUtils.kstDayRange(today);
        DateRangeUtils.Range yesterdayRange = DateRangeUtils.kstDayRange(yesterday);

        return new AdminDashboardDtos.OverviewResponse(
                today,
                buildTodayKpi(tenantId, monthToDate, previousMonthToDate, todayRange, yesterdayRange),
                buildPointLiability(tenantId, today),
                buildGradeDistribution(tenantId),
                buildStatusDistribution(tenantId),
                buildBatchStatuses(tenantId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDtos.TrendsResponse getTrends(DashboardPeriod period) {
        UUID tenantId = TenantContext.requireTenantId();
        LocalDate today = LocalDate.now(AppTimeZones.KST);
        LocalDate fromDate = period.startDate(today);
        DateRangeUtils.Range range = period.range(today);
        String zone = AppTimeZones.KST.getId();

        Map<LocalDate, long[]> buckets = new HashMap<>();
        for (Object[] row : memberRepository.aggregateDailySignups(
                tenantId, range.start(), range.endExclusive(), zone)) {
            bucketOf(buckets, row[0])[0] = toLong(row[1]);
        }
        for (Object[] row : memberRepository.aggregateDailyWithdrawals(
                tenantId, range.start(), range.endExclusive(), zone)) {
            bucketOf(buckets, row[0])[1] = toLong(row[1]);
        }
        for (Object[] row : pointLedgerRepository.aggregateDailyEarnAndUse(
                tenantId, range.start(), range.endExclusive(), zone)) {
            long[] bucket = bucketOf(buckets, row[0]);
            bucket[2] = toLong(row[1]);
            bucket[3] = toLong(row[2]);
        }

        // 데이터가 없는 날짜도 0으로 채워 차트 x축이 끊기지 않게 한다.
        long days = ChronoUnit.DAYS.between(fromDate, today) + 1;
        List<AdminDashboardDtos.TrendPoint> points = new ArrayList<>((int) days);
        for (LocalDate date = fromDate; !date.isAfter(today); date = date.plusDays(1)) {
            long[] bucket = buckets.getOrDefault(date, new long[4]);
            points.add(new AdminDashboardDtos.TrendPoint(date, bucket[0], bucket[1], bucket[2], bucket[3]));
        }

        return new AdminDashboardDtos.TrendsResponse(period.code(), fromDate, today, points);
    }

    // =====================================================================
    // 1단: 회원 현황 / 2단 보조(이번 달 적립·사용)
    // =====================================================================
    private AdminDashboardDtos.TodayKpiResponse buildTodayKpi(
            UUID tenantId,
            DateRangeUtils.Range monthToDate,
            DateRangeUtils.Range previousMonthToDate,
            DateRangeUtils.Range todayRange,
            DateRangeUtils.Range yesterdayRange
    ) {
        long mtdNewMembers = memberRepository
                .countByTenantIdAndJoinedAtGreaterThanEqualAndJoinedAtLessThan(
                        tenantId, monthToDate.start(), monthToDate.endExclusive());
        long prevNewMembers = memberRepository
                .countByTenantIdAndJoinedAtGreaterThanEqualAndJoinedAtLessThan(
                        tenantId, previousMonthToDate.start(), previousMonthToDate.endExclusive());

        // 최종 탈회(withdrawnAt). 탈퇴 요청(withdrawRequestedAt) 집계는 사용하지 않는다.
        long mtdWithdrawals = memberRepository
                .countByTenantIdAndWithdrawnAtGreaterThanEqualAndWithdrawnAtLessThan(
                        tenantId, monthToDate.start(), monthToDate.endExclusive());
        long prevWithdrawals = memberRepository
                .countByTenantIdAndWithdrawnAtGreaterThanEqualAndWithdrawnAtLessThan(
                        tenantId, previousMonthToDate.start(), previousMonthToDate.endExclusive());

        // 누적 활성 회원: 기준 시점까지 가입했고 그 시점까지 탈회하지 않은 회원. 비교는 전일.
        long activeMembers = memberRepository.countActiveMembersAsOf(tenantId, todayRange.endExclusive());
        long prevActiveMembers = memberRepository.countActiveMembersAsOf(tenantId, yesterdayRange.endExclusive());

        // 2단 우측 보조: 이번 달 적립/사용. 화면에서 증감을 쓰지 않으므로 previous=0.
        long mtdEarn = pointLedgerRepository.sumEarnByTenantIdAndCreatedAtBetween(
                tenantId, monthToDate.start(), monthToDate.endExclusive());
        long mtdUse = pointLedgerRepository.sumPureUseByTenantIdAndCreatedAtBetween(
                tenantId, monthToDate.start(), monthToDate.endExclusive());

        return new AdminDashboardDtos.TodayKpiResponse(
                AdminDashboardDtos.MetricValue.of(mtdNewMembers, prevNewMembers),
                AdminDashboardDtos.MetricValue.of(mtdWithdrawals, prevWithdrawals),
                AdminDashboardDtos.MetricValue.of(activeMembers, prevActiveMembers),
                AdminDashboardDtos.MetricValue.of(mtdEarn, 0),
                AdminDashboardDtos.MetricValue.of(mtdUse, 0)
        );
    }

    // =====================================================================
    // 2단: 포인트 현황
    // =====================================================================
    private AdminDashboardDtos.PointLiabilityResponse buildPointLiability(UUID tenantId, LocalDate today) {
        long totalEarned = pointLedgerRepository.sumTotalEarnByTenantId(tenantId);
        long totalUsed = pointLedgerRepository.sumTotalPureUseByTenantId(tenantId);
        long outstanding = pointLotRepository.sumRemainingAmountByTenantId(tenantId);

        // 이번 달 소멸 예정: 이번 달 말 KST 자정 이전에 만료되는 잔여분.
        // 이미 만료일이 지났지만 소멸 배치가 아직 처리하지 않은 lot 도 이번 달 안에 사라지므로 함께 포함한다.
        DateRangeUtils.Range thisMonth = DateRangeUtils.kstRange(
                today.withDayOfMonth(1),
                today.withDayOfMonth(today.lengthOfMonth())
        );
        long expiringThisMonth = pointLotRepository.sumRemainingAmountExpiringBefore(
                tenantId, thisMonth.endExclusive());

        return new AdminDashboardDtos.PointLiabilityResponse(
                totalEarned, totalUsed, outstanding, expiringThisMonth);
    }

    // =====================================================================
    // 4단: 분포 / 배치 상태
    // =====================================================================
    private List<AdminDashboardDtos.DistributionItem> buildGradeDistribution(UUID tenantId) {
        return memberRepository.aggregateGradeDistribution(tenantId, MemberStatusCodes.WITHDRAWN).stream()
                .sorted(Comparator.comparing(
                        MemberRepository.GradeDistributionView::getGradeLevel,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(v -> {
                    String name = v.getGradeName();
                    boolean unassigned = name == null || name.isBlank();
                    return new AdminDashboardDtos.DistributionItem(
                            unassigned ? UNASSIGNED_GRADE_KEY : name,
                            unassigned ? UNASSIGNED_GRADE_LABEL : name,
                            v.getMemberCount()
                    );
                })
                .toList();
    }

    private List<AdminDashboardDtos.DistributionItem> buildStatusDistribution(UUID tenantId) {
        // label 은 상태 코드 그대로 내려주고, 한글 표기는 화면에서 공통코드(MEMBER_STATUS)로 매핑한다.
        return memberRepository.aggregateStatusDistribution(tenantId).stream()
                .sorted(Comparator.comparingLong(
                        MemberRepository.StatusDistributionView::getMemberCount).reversed())
                .map(v -> new AdminDashboardDtos.DistributionItem(
                        v.getStatusCode(), v.getStatusCode(), v.getMemberCount()))
                .toList();
    }

    private List<AdminDashboardDtos.BatchStatusItem> buildBatchStatuses(UUID tenantId) {
        List<BatchJobConfig> configs = batchJobConfigRepository.findByTenantIdOrderByBatchNameAsc(tenantId);
        List<BatchExecutionHistory> latest = batchExecutionHistoryRepository.findLatestExecutionPerBatch(tenantId);

        Map<String, BatchExecutionHistory> latestByName = new HashMap<>();
        for (BatchExecutionHistory h : latest) {
            latestByName.put(h.getBatchName(), h);
        }
        Map<String, Boolean> enabledByName = new HashMap<>();
        for (BatchJobConfig c : configs) {
            enabledByName.put(c.getBatchName(), c.isEnabled());
        }

        // 설정만 있고 실행 이력이 없는 배치, 설정이 삭제됐지만 이력이 남은 배치를 모두 노출한다.
        Set<String> batchNames = new LinkedHashSet<>(enabledByName.keySet());
        batchNames.addAll(latestByName.keySet());

        return batchNames.stream()
                .sorted()
                .map(name -> {
                    BatchExecutionHistory h = latestByName.get(name);
                    return new AdminDashboardDtos.BatchStatusItem(
                            name,
                            Boolean.TRUE.equals(enabledByName.get(name)),
                            h != null ? h.getStatus().name() : null,
                            h != null ? h.getStartedAt() : null,
                            h != null ? h.getFinishedAt() : null,
                            h != null ? h.getProcessedCount() : 0,
                            h != null ? h.getErrorCount() : 0,
                            h != null ? h.getErrorMessage() : null
                    );
                })
                .toList();
    }

    // =====================================================================
    // 집계 결과 매핑 헬퍼
    // =====================================================================
    private static long[] bucketOf(Map<LocalDate, long[]> buckets, Object rawDate) {
        return buckets.computeIfAbsent(LocalDate.parse(String.valueOf(rawDate)), k -> new long[4]);
    }

    private static long toLong(Object value) {
        return value instanceof Number n ? n.longValue() : 0L;
    }
}
