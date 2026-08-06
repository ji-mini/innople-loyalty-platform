package com.innople.loyalty.service.batch;

import com.innople.loyalty.common.AppTimeZones;
import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.batch.BatchJobConfig;
import com.innople.loyalty.domain.batch.BatchNames;
import com.innople.loyalty.domain.member.HistoryActorType;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * 자동 탈퇴 배치 실행 엔진 ({@link BatchRunner}).
 *
 * <p>WITHDRAW_REQUESTED 상태에서 유예기간(threshold_days) 경과 회원을 {@link MemberService#withdraw}
 * (SYSTEM 경로)로 전환한다. 상태 직접 세팅은 하지 않는다.</p>
 *
 * <p>트랜잭션 경계: 배치 루프를 하나의 큰 트랜잭션으로 감싸지 않는다. 회원별 처리는 {@code withdraw(...)}가
 * 각자 독립 커밋하며(@Transactional), 한 회원의 실패가 다른 회원을 롤백시키지 않도록 try-catch 로 격리한다.</p>
 *
 * <p>config 조회·catch-up·실행이력·중복 락은 {@link BatchDispatchService}가 담당한다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoWithdrawalBatchService implements BatchRunner {

    static final String AUTO_WITHDRAWAL_REASON = "자동 탈퇴 (유예기간 경과)";

    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @Override
    public String batchName() {
        return BatchNames.AUTO_WITHDRAWAL;
    }

    @Override
    public RunResult execute(BatchJobConfig config, Instant now) {
        UUID tenantId = TenantContext.requireTenantId();
        Integer thresholdDays = config.getThresholdDays();
        if (thresholdDays == null || thresholdDays <= 0) {
            throw new IllegalStateException(
                    "AUTO_WITHDRAWAL requires threshold_days > 0 (tenant=" + tenantId + ")");
        }

        Instant threshold = now.minus(thresholdDays, ChronoUnit.DAYS);
        LocalDate withdrawnDate = now.atZone(AppTimeZones.KST).toLocalDate();

        List<String> targetMemberNos = memberRepository.findWithdrawTargetMemberNos(
                tenantId, MemberStatusCodes.WITHDRAW_REQUESTED, threshold);

        int processed = 0;
        int error = 0;
        String lastError = null;

        for (String memberNo : targetMemberNos) {
            try {
                memberService.withdraw(
                        memberNo,
                        new MemberService.WithdrawCommand(withdrawnDate, AUTO_WITHDRAWAL_REASON),
                        null,
                        HistoryActorType.SYSTEM
                );
                processed++;
            } catch (Exception e) {
                error++;
                lastError = e.getMessage();
                log.warn("Auto-withdrawal failed for member {} (tenant {}): {}",
                        memberNo, tenantId, e.getMessage());
            }
        }

        return new RunResult(processed, error, lastError);
    }
}
