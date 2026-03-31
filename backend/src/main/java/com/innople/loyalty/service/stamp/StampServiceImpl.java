package com.innople.loyalty.service.stamp;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.stamp.StampAccount;
import com.innople.loyalty.domain.stamp.StampCouponIssue;
import com.innople.loyalty.domain.stamp.StampEventType;
import com.innople.loyalty.domain.stamp.StampIssuanceMode;
import com.innople.loyalty.domain.stamp.StampLedger;
import com.innople.loyalty.domain.stamp.StampPolicy;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.repository.StampAccountRepository;
import com.innople.loyalty.repository.StampCouponIssueRepository;
import com.innople.loyalty.repository.StampLedgerRepository;
import com.innople.loyalty.repository.StampPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StampServiceImpl implements StampService {

    public static final String REF_POS_ORDER = "POS_ORDER";

    private final StampPolicyRepository stampPolicyRepository;
    private final StampAccountRepository stampAccountRepository;
    private final StampLedgerRepository stampLedgerRepository;
    private final StampCouponIssueRepository stampCouponIssueRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public PosEarnResult earnFromPos(UUID memberId, long purchaseAmountWon, String posOrderReferenceId) {
        UUID tenantId = TenantContext.requireTenantId();
        if (purchaseAmountWon <= 0) {
            throw new IllegalArgumentException("purchaseAmountWon must be positive");
        }
        if (posOrderReferenceId == null || posOrderReferenceId.isBlank()) {
            throw new IllegalArgumentException("posOrderReferenceId is required for POS earn");
        }
        String refId = posOrderReferenceId.trim();
        memberRepository.findByTenantIdAndId(tenantId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));

        if (stampLedgerRepository.existsByTenantIdAndReferenceTypeAndReferenceId(tenantId, REF_POS_ORDER, refId)) {
            StampAccount acc = loadOrCreateAccount(memberId);
            return new PosEarnResult(null, 0, acc.getCurrentBalance(), true);
        }

        StampPolicy policy = stampPolicyRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("활성 스탬프 정책이 없습니다."));

        int earned = (int) (purchaseAmountWon / policy.getAmountWonPerStamp());
        if (earned <= 0) {
            StampAccount acc = loadOrCreateAccount(memberId);
            return new PosEarnResult(null, 0, acc.getCurrentBalance(), false);
        }

        StampAccount account = loadOrCreateAccountWithLock(memberId);
        StampLedger ledger = new StampLedger(
                account.getId(),
                memberId,
                policy.getId(),
                StampEventType.EARN_POS,
                earned,
                null,
                REF_POS_ORDER,
                refId,
                purchaseAmountWon
        );
        stampLedgerRepository.save(ledger);
        account.addStamps(earned);
        stampAccountRepository.save(account);

        if (policy.getIssuanceMode() == StampIssuanceMode.AUTO) {
            processAutoRedemptions(account.getMemberId(), policy);
        }

        StampAccount refreshed = stampAccountRepository.findByTenantIdAndMemberId(tenantId, memberId).orElseThrow();
        return new PosEarnResult(ledger.getId(), earned, refreshed.getCurrentBalance(), false);
    }

    @Override
    @Transactional
    public ManualGrantResult grantManual(UUID memberId, int stamps, String reason) {
        UUID tenantId = TenantContext.requireTenantId();
        if (stamps <= 0) {
            throw new IllegalArgumentException("stamps must be positive");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }
        memberRepository.findByTenantIdAndId(tenantId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));

        StampPolicy policy = stampPolicyRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("활성 스탬프 정책이 없습니다."));

        StampAccount account = loadOrCreateAccountWithLock(memberId);
        StampLedger ledger = new StampLedger(
                account.getId(),
                memberId,
                policy.getId(),
                StampEventType.EARN_MANUAL,
                stamps,
                reason.trim(),
                "ADMIN_MANUAL",
                null,
                null
        );
        stampLedgerRepository.save(ledger);
        account.addStamps(stamps);
        stampAccountRepository.save(account);

        if (policy.getIssuanceMode() == StampIssuanceMode.AUTO) {
            processAutoRedemptions(memberId, policy);
        }

        StampAccount refreshed = stampAccountRepository.findByTenantIdAndMemberId(tenantId, memberId).orElseThrow();
        return new ManualGrantResult(ledger.getId(), refreshed.getCurrentBalance());
    }

    @Override
    @Transactional
    public ClaimResult claimCouponForMember(UUID memberId) {
        UUID tenantId = TenantContext.requireTenantId();
        memberRepository.findByTenantIdAndId(tenantId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));

        StampPolicy policy = stampPolicyRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("활성 스탬프 정책이 없습니다."));
        if (policy.getIssuanceMode() != StampIssuanceMode.MANUAL) {
            throw new IllegalArgumentException("발급 방식이 MANUAL일 때만 고객이 직접 받을 수 있습니다.");
        }

        StampAccount account = loadOrCreateAccountWithLock(memberId);
        if (account.getCurrentBalance() < policy.getStampsRequiredForCoupon()) {
            throw new IllegalArgumentException("스탬프가 부족합니다.");
        }

        RedeemOnceResult r = redeemOneCoupon(account, policy);
        StampAccount refreshed = stampAccountRepository.findByTenantIdAndMemberId(tenantId, memberId).orElseThrow();
        return new ClaimResult(r.ledger().getId(), r.issue().getId(), refreshed.getCurrentBalance());
    }

    @Override
    @Transactional
    public AutoRedeemBatchResult runAutoRedeemForCurrentTenant() {
        UUID tenantId = TenantContext.requireTenantId();
        StampPolicy policy = stampPolicyRepository.findByTenantIdAndActiveTrue(tenantId)
                .orElse(null);
        if (policy == null || policy.getIssuanceMode() != StampIssuanceMode.AUTO) {
            return new AutoRedeemBatchResult(0, 0);
        }

        int required = policy.getStampsRequiredForCoupon();
        List<StampAccount> candidates = stampAccountRepository.findByTenantIdAndCurrentBalanceAtLeast(tenantId, required);
        int accountsProcessed = 0;
        int couponsIssued = 0;

        for (StampAccount candidate : candidates) {
            try {
                int issued = processAutoRedemptionsWithRetry(candidate.getMemberId(), policy);
                if (issued > 0) {
                    accountsProcessed++;
                    couponsIssued += issued;
                }
            } catch (ObjectOptimisticLockingFailureException ignored) {
                // 다른 트랜잭션에서 처리됨 — 다음 배치에 위임
            }
        }

        return new AutoRedeemBatchResult(accountsProcessed, couponsIssued);
    }

    private int processAutoRedemptionsWithRetry(UUID memberId, StampPolicy policy) {
        int totalIssued = 0;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                totalIssued = processAutoRedemptions(memberId, policy);
                break;
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt == 2) {
                    throw e;
                }
            }
        }
        return totalIssued;
    }

    /**
     * @return 발급된 쿠폰 수
     */
    private int processAutoRedemptions(UUID memberId, StampPolicy policy) {
        int issued = 0;
        while (true) {
            StampAccount account = loadOrCreateAccountWithLock(memberId);
            if (account.getCurrentBalance() < policy.getStampsRequiredForCoupon()) {
                break;
            }
            redeemOneCoupon(account, policy);
            issued++;
        }
        return issued;
    }

    private RedeemOnceResult redeemOneCoupon(StampAccount account, StampPolicy policy) {
        int need = policy.getStampsRequiredForCoupon();
        StampLedger ledger = new StampLedger(
                account.getId(),
                account.getMemberId(),
                policy.getId(),
                StampEventType.REDEEM_COUPON,
                -need,
                "스탬프 보상 쿠폰 전환",
                "STAMP_REDEEM",
                null,
                null
        );
        stampLedgerRepository.save(ledger);
        account.addStamps(-need);
        stampAccountRepository.save(account);

        UUID tenantId = TenantContext.requireTenantId();
        if (stampCouponIssueRepository.existsByTenantIdAndRedemptionLedgerId(tenantId, ledger.getId())) {
            throw new IllegalArgumentException("duplicate coupon issue for ledger");
        }
        StampCouponIssue issue = new StampCouponIssue(
                account.getMemberId(),
                policy.getId(),
                policy.getCouponTemplateId(),
                ledger.getId()
        );
        stampCouponIssueRepository.save(issue);
        return new RedeemOnceResult(ledger, issue);
    }

    private StampAccount loadOrCreateAccount(UUID memberId) {
        UUID tenantId = TenantContext.requireTenantId();
        return stampAccountRepository.findByTenantIdAndMemberId(tenantId, memberId)
                .orElseGet(() -> stampAccountRepository.save(new StampAccount(memberId)));
    }

    private StampAccount loadOrCreateAccountWithLock(UUID memberId) {
        UUID tenantId = TenantContext.requireTenantId();
        return stampAccountRepository.findWithLockByTenantIdAndMemberId(tenantId, memberId)
                .orElseGet(() -> {
                    StampAccount created = stampAccountRepository.save(new StampAccount(memberId));
                    return stampAccountRepository.findWithLockByTenantIdAndMemberId(tenantId, memberId)
                            .orElseThrow();
                });
    }

    private record RedeemOnceResult(StampLedger ledger, StampCouponIssue issue) {
    }
}
