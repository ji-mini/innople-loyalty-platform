package com.innople.loyalty.service.points;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.domain.member.MembershipGrade;
import com.innople.loyalty.repository.CommonCodeRepository;
import com.innople.loyalty.service.member.MemberExceptions;
import com.innople.loyalty.domain.points.PointAccount;
import com.innople.loyalty.domain.points.PointAllocation;
import com.innople.loyalty.domain.points.PointEventType;
import com.innople.loyalty.domain.points.PointLedger;
import com.innople.loyalty.domain.points.PointLot;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.repository.PointAccountRepository;
import com.innople.loyalty.repository.PointAllocationRepository;
import com.innople.loyalty.repository.PointLedgerRepository;
import com.innople.loyalty.repository.PointLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;
import java.math.BigDecimal;

import static com.innople.loyalty.service.points.PointExceptions.InsufficientPointsException;
import static com.innople.loyalty.service.points.PointExceptions.InvalidPointAmountException;
import static com.innople.loyalty.service.points.PointExceptions.PointAccountNotFoundException;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {
    private static final int APPROVAL_NO_MAX_LENGTH = 12;
    private static final int APPROVAL_RANDOM_LENGTH = 4;
    private static final int REFERENCE_TYPE_MAX_LENGTH = 50;
    private static final int REFERENCE_ID_MAX_LENGTH = 100;
    private static final String POINT_REFERENCE_TYPE_GROUP = "POINT_REFERENCE_TYPE";
    private static final String ADMIN_WEB_MANUAL_EARN = "ADMIN_WEB_MANUAL_EARN";
    private static final String ADMIN_WEB_MANUAL_EXPIRE = "ADMIN_WEB_MANUAL_EXPIRE";
    private static final String SYSTEM_AUTO_EXPIRE = "SYSTEM_AUTO_EXPIRE";

    // 수기/POS 포인트 적립·차감이 허용되는 회원 상태 allow-list. 이 집합에 없는 상태는 전부 차단한다.
    // (block-list 가 아니라 allow-list 이므로 새 상태가 추가돼도 기본이 차단이다.)
    // 시스템 만료(autoExpire/manualExpire/expire)는 이 가드를 거치지 않으므로 비활성 회원도 만료가 계속 동작한다.
    private static final Set<String> POINT_OPERATION_ALLOWED_STATUS = Set.of(
            MemberStatusCodes.ACTIVE, MemberStatusCodes.LEGACY_NORMAL);

    private final MemberRepository memberRepository;
    private final PointAccountRepository pointAccountRepository;
    private final PointLotRepository pointLotRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final PointAllocationRepository pointAllocationRepository;
    private final CommonCodeRepository commonCodeRepository;

    @Override
    @Transactional
    public PointOperationResult earn(UUID memberId, long amount, Instant expiresAt, String reason, String approvalNo,
                                     String referenceType, String referenceId,
                                     Long purchaseAmount, Long totalPurchaseAmount, Long discountAmount,
                                     String sourceChannel) {
        if (amount <= 0) {
            throw new InvalidPointAmountException("amount must be positive");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt must not be null");
        }
        if (sourceChannel == null || sourceChannel.isBlank()) {
            throw new IllegalArgumentException("sourceChannel must not be blank");
        }

        UUID tenantId = TenantContext.requireTenantId();
        Member member = memberRepository.findByTenantIdAndId(tenantId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        assertPointOperationAllowed(member.getStatusCode());

        String resolvedApprovalNo = resolveApprovalNo(tenantId, approvalNo);
        ReferenceInfo referenceInfo = validateReferenceInfo(tenantId, referenceType, referenceId);
        Instant now = Instant.now();
        if (!expiresAt.isAfter(now)) {
            throw new IllegalArgumentException("expiresAt must be in the future");
        }

        PointAccount account = pointAccountRepository
                .findWithLockByTenantIdAndMemberId(tenantId, memberId)
                .orElseGet(() -> pointAccountRepository.save(new PointAccount(memberId)));

        PointLedger ledger = pointLedgerRepository.save(
                new PointLedger(
                        account.getId(),
                        memberId,
                        PointEventType.EARN,
                        amount,
                        reason,
                        sourceChannel,
                        resolvedApprovalNo,
                        referenceInfo.referenceType(),
                        referenceInfo.referenceId(),
                        purchaseAmount,
                        totalPurchaseAmount,
                        discountAmount
                )
        );

        pointLotRepository.save(new PointLot(account.getId(), memberId, amount, expiresAt, ledger.getId()));

        account.addBalance(amount);
        pointAccountRepository.save(account);

        return new PointOperationResult(
                ledger.getId(),
                ledger.getApprovalNo(),
                ledger.getEventType(),
                ledger.getAmount(),
                account.getCurrentBalance(),
                ledger.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public PointOperationResult earnFromPurchase(UUID memberId, long purchaseAmount, Long totalPurchaseAmount, Long discountAmount,
                                                 Instant expiresAt, String reason, String approvalNo,
                                                 String referenceType, String referenceId, String sourceChannel) {
        if (purchaseAmount <= 0) {
            throw new IllegalArgumentException("purchaseAmount must be positive");
        }

        UUID tenantId = TenantContext.requireTenantId();
        Member member = memberRepository.findByTenantIdAndIdWithMembershipGrade(tenantId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        assertPointOperationAllowed(member.getStatusCode());

        MembershipGrade grade = member.getMembershipGrade();
        if (grade == null) {
            throw new IllegalArgumentException("회원 등급이 없어 적립 대상 금액 기준 적립을 할 수 없습니다.");
        }

        BigDecimal rate = grade.getEarnRatePercent();
        long resolvedAmount = PointEarnCalculator.pointsFromPurchase(purchaseAmount, rate);
        if (resolvedAmount <= 0) {
            throw new IllegalArgumentException("적립 포인트가 0 이하입니다. 적립률·적립 대상 금액을 확인하세요.");
        }

        return earn(
                memberId,
                resolvedAmount,
                expiresAt,
                reason,
                approvalNo,
                referenceType,
                referenceId,
                purchaseAmount,
                totalPurchaseAmount,
                discountAmount,
                sourceChannel
        );
    }

    @Override
    @Transactional
    public PointOperationResult use(UUID memberId, long amount, String reason, String approvalNo,
                                    String referenceType, String referenceId, String sourceChannel) {
        if (amount <= 0) {
            throw new InvalidPointAmountException("amount must be positive");
        }
        if (sourceChannel == null || sourceChannel.isBlank()) {
            throw new IllegalArgumentException("sourceChannel must not be blank");
        }

        UUID tenantId = TenantContext.requireTenantId();
        Member member = memberRepository.findByTenantIdAndId(tenantId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        assertPointOperationAllowed(member.getStatusCode());

        String resolvedApprovalNo = resolveApprovalNo(tenantId, approvalNo);
        ReferenceInfo referenceInfo = validateReferenceInfo(tenantId, referenceType, referenceId);
        PointAccount account = pointAccountRepository
                .findWithLockByTenantIdAndMemberId(tenantId, memberId)
                .orElseThrow(() -> new PointAccountNotFoundException("PointAccount not found"));

        if (account.getCurrentBalance() < amount) {
            throw new InsufficientPointsException("Insufficient points");
        }

        Instant now = Instant.now();
        PointLedger ledger = pointLedgerRepository.save(
                new PointLedger(
                        account.getId(),
                        memberId,
                        PointEventType.USE,
                        -amount,
                        reason,
                        sourceChannel,
                        resolvedApprovalNo,
                        referenceInfo.referenceType(),
                        referenceInfo.referenceId()
                )
        );

        List<PointLot> lots = pointLotRepository.findDeductionCandidatesFefo(tenantId, account.getId(), now);
        long remainingToDeduct = amount;
        List<PointAllocation> allocations = new ArrayList<>();

        for (PointLot lot : lots) {
            if (remainingToDeduct == 0) {
                break;
            }

            long available = lot.getRemainingAmount();
            if (available <= 0) {
                continue;
            }

            long allocate = Math.min(available, remainingToDeduct);
            lot.deduct(allocate);
            allocations.add(new PointAllocation(account.getId(), ledger.getId(), lot.getId(), allocate));
            remainingToDeduct -= allocate;
        }

        if (remainingToDeduct != 0) {
            throw new InsufficientPointsException("Insufficient unexpired point lots for FEFO deduction");
        }

        pointLotRepository.saveAll(lots);
        pointAllocationRepository.saveAll(allocations);

        account.addBalance(-amount);
        pointAccountRepository.save(account);

        return new PointOperationResult(
                ledger.getId(),
                ledger.getApprovalNo(),
                ledger.getEventType(),
                ledger.getAmount(),
                account.getCurrentBalance(),
                ledger.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public PointOperationResult manualExpire(UUID memberId, Instant referenceAt, String reason, String approvalNo,
                                             String referenceType, String referenceId) {
        return expire(
                memberId,
                referenceAt,
                reason,
                approvalNo,
                referenceType,
                referenceId,
                PointEventType.EXPIRE_MANUAL,
                ADMIN_WEB_MANUAL_EXPIRE
        );
    }

    @Override
    @Transactional
    public PointOperationResult autoExpire(UUID memberId, Instant referenceAt, String reason, String referenceType, String referenceId) {
        return expire(
                memberId,
                referenceAt,
                reason,
                null,
                referenceType,
                referenceId,
                PointEventType.EXPIRE_AUTO,
                SYSTEM_AUTO_EXPIRE
        );
    }

    /**
     * 탈회 소각. 만료(expire) 계열과 코드를 공유하지 않으며(공통 expire 시그니처 보존을 위해 로직을 복제),
     * 상태 가드(assertPointOperationAllowed)를 호출하지 않는다. PointAccount 부재/잔량 0 은 예외 없이 no-op.
     * 만료일 도래 여부와 무관하게 잔량이 남은 전체 lot 을 소각 대상으로 한다.
     */
    @Override
    @Transactional
    public PointOperationResult burnAll(UUID memberId, Instant referenceAt, String reason, String sourceChannel) {
        if (sourceChannel == null || sourceChannel.isBlank()) {
            throw new IllegalArgumentException("sourceChannel must not be blank");
        }

        UUID tenantId = TenantContext.requireTenantId();

        // 포인트를 한 번도 받은 적 없는 회원(계정 미존재)의 탈회를 실패시키지 않도록 no-op 처리한다.
        PointAccount account = pointAccountRepository
                .findWithLockByTenantIdAndMemberId(tenantId, memberId)
                .orElse(null);
        if (account == null) {
            return noOpResult(0L);
        }

        List<PointLot> remainingLots = pointLotRepository.findAllRemainingLots(tenantId, account.getId());
        long totalToBurn = 0L;
        for (PointLot lot : remainingLots) {
            totalToBurn = Math.addExact(totalToBurn, lot.getRemainingAmount());
        }

        if (totalToBurn == 0L) {
            return noOpResult(account.getCurrentBalance());
        }

        String resolvedApprovalNo = resolveApprovalNo(tenantId, null);
        PointLedger ledger = pointLedgerRepository.save(
                new PointLedger(
                        account.getId(),
                        memberId,
                        PointEventType.BURN_WITHDRAW,
                        -totalToBurn,
                        reason,
                        sourceChannel,
                        resolvedApprovalNo,
                        null,
                        null
                )
        );

        List<PointAllocation> allocations = new ArrayList<>();
        for (PointLot lot : remainingLots) {
            long burnAmount = lot.getRemainingAmount();
            if (burnAmount <= 0) {
                continue;
            }
            lot.deduct(burnAmount);
            allocations.add(new PointAllocation(account.getId(), ledger.getId(), lot.getId(), burnAmount));
        }

        pointLotRepository.saveAll(remainingLots);
        pointAllocationRepository.saveAll(allocations);

        account.addBalance(-totalToBurn);
        pointAccountRepository.save(account);

        return new PointOperationResult(
                ledger.getId(),
                ledger.getApprovalNo(),
                ledger.getEventType(),
                ledger.getAmount(),
                account.getCurrentBalance(),
                ledger.getCreatedAt()
        );
    }

    private PointOperationResult noOpResult(long currentBalance) {
        return new PointOperationResult(
                null,
                null,
                PointEventType.BURN_WITHDRAW,
                0L,
                currentBalance,
                Instant.now()
        );
    }

    /**
     * 수기/POS 포인트 적립·차감 허용 여부를 회원 상태로 판정한다. statusCode 만 받는 순수 검증이며 내부에서 조회하지 않는다.
     * allow-list 에 없는 상태는 전부 차단하여 409(MEMBER_STATUS_NOT_ALLOWED)로 응답한다.
     */
    private void assertPointOperationAllowed(String statusCode) {
        if (statusCode != null && POINT_OPERATION_ALLOWED_STATUS.contains(statusCode)) {
            return;
        }
        throw new MemberExceptions.MemberStatusNotAllowedException(
                "%s 상태 회원은 포인트 적립/차감이 불가합니다".formatted(statusLabel(statusCode)));
    }

    private String statusLabel(String statusCode) {
        if (statusCode == null) {
            return "알 수 없음";
        }
        return switch (statusCode) {
            case MemberStatusCodes.DORMANT -> "휴면";
            case MemberStatusCodes.SUSPENDED -> "정지";
            case MemberStatusCodes.WITHDRAW_REQUESTED -> "탈회요청";
            case MemberStatusCodes.WITHDRAWN -> "탈회";
            default -> statusCode;
        };
    }

    private String resolveApprovalNo(UUID tenantId, String requestedApprovalNo) {
        String normalized = normalizeApprovalNo(requestedApprovalNo);
        if (normalized != null) {
            if (pointLedgerRepository.existsByTenantIdAndApprovalNo(tenantId, normalized)) {
                throw new IllegalArgumentException("approvalNo already exists");
            }
            return normalized;
        }

        String generated;
        do {
            generated = generateApprovalNo();
        } while (pointLedgerRepository.existsByTenantIdAndApprovalNo(tenantId, generated));
        return generated;
    }

    private String normalizeApprovalNo(String rawApprovalNo) {
        if (rawApprovalNo == null) {
            return null;
        }
        String normalized = rawApprovalNo.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > APPROVAL_NO_MAX_LENGTH) {
            throw new IllegalArgumentException("approvalNo must be 12 characters or less");
        }
        if (!normalized.matches("^[A-Z0-9]{1,12}$")) {
            throw new IllegalArgumentException("approvalNo must contain only letters and digits");
        }
        return normalized;
    }

    private String generateApprovalNo() {
        String timePart = Long.toString(System.currentTimeMillis(), 36).toUpperCase(Locale.ROOT);
        int bound = (int) Math.pow(36, APPROVAL_RANDOM_LENGTH);
        String randomPart = Integer.toString(ThreadLocalRandom.current().nextInt(bound), 36).toUpperCase(Locale.ROOT);
        randomPart = "0".repeat(APPROVAL_RANDOM_LENGTH - randomPart.length()) + randomPart;
        String candidate = timePart + randomPart;
        if (candidate.length() > APPROVAL_NO_MAX_LENGTH) {
            return candidate.substring(candidate.length() - APPROVAL_NO_MAX_LENGTH);
        }
        return "0".repeat(APPROVAL_NO_MAX_LENGTH - candidate.length()) + candidate;
    }

    private PointOperationResult expire(UUID memberId, Instant referenceAt, String reason, String approvalNo,
                                        String referenceType, String referenceId, PointEventType eventType,
                                        String sourceChannel) {
        UUID tenantId = TenantContext.requireTenantId();
        String resolvedApprovalNo = resolveApprovalNo(tenantId, approvalNo);
        ReferenceInfo referenceInfo = validateReferenceInfo(tenantId, referenceType, referenceId);
        Instant ref = (referenceAt != null) ? referenceAt : Instant.now();

        PointAccount account = pointAccountRepository
                .findWithLockByTenantIdAndMemberId(tenantId, memberId)
                .orElseThrow(() -> new PointAccountNotFoundException("PointAccount not found"));

        List<PointLot> expirableLots = pointLotRepository.findExpirableLotsFefo(tenantId, account.getId(), ref);
        long totalToExpire = 0L;
        for (PointLot lot : expirableLots) {
            totalToExpire = Math.addExact(totalToExpire, lot.getRemainingAmount());
        }

        if (totalToExpire == 0L) {
            return new PointOperationResult(
                    null,
                    null,
                    eventType,
                    0L,
                    account.getCurrentBalance(),
                    Instant.now()
            );
        }

        PointLedger ledger = pointLedgerRepository.save(
                new PointLedger(
                        account.getId(),
                        memberId,
                        eventType,
                        -totalToExpire,
                        reason,
                        sourceChannel,
                        resolvedApprovalNo,
                        referenceInfo.referenceType(),
                        referenceInfo.referenceId()
                )
        );

        List<PointAllocation> allocations = new ArrayList<>();
        for (PointLot lot : expirableLots) {
            long expireAmount = lot.getRemainingAmount();
            if (expireAmount <= 0) {
                continue;
            }
            lot.deduct(expireAmount);
            allocations.add(new PointAllocation(account.getId(), ledger.getId(), lot.getId(), expireAmount));
        }

        pointLotRepository.saveAll(expirableLots);
        pointAllocationRepository.saveAll(allocations);

        account.addBalance(-totalToExpire);
        pointAccountRepository.save(account);

        return new PointOperationResult(
                ledger.getId(),
                ledger.getApprovalNo(),
                ledger.getEventType(),
                ledger.getAmount(),
                account.getCurrentBalance(),
                ledger.getCreatedAt()
        );
    }

    private ReferenceInfo validateReferenceInfo(UUID tenantId, String rawReferenceType, String rawReferenceId) {
        String normalizedReferenceType = normalizeReferenceType(rawReferenceType);
        String normalizedReferenceId = normalizeOptional(rawReferenceId, REFERENCE_ID_MAX_LENGTH);

        if ((normalizedReferenceType == null) != (normalizedReferenceId == null)) {
            throw new IllegalArgumentException("referenceType and referenceId must be provided together");
        }
        if (normalizedReferenceType == null) {
            return ReferenceInfo.empty();
        }

        if (commonCodeRepository.findByTenantIdAndCodeGroupAndCodeAndActiveIsTrue(
                tenantId,
                POINT_REFERENCE_TYPE_GROUP,
                normalizedReferenceType
        ).isEmpty()) {
            throw new IllegalArgumentException("Invalid point reference type: " + normalizedReferenceType);
        }
        return new ReferenceInfo(normalizedReferenceType, normalizedReferenceId);
    }

    private String normalizeReferenceType(String value) {
        String normalized = normalizeOptional(value, REFERENCE_TYPE_MAX_LENGTH);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeOptional(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException("value exceeds max length: " + maxLength);
        }
        return normalized;
    }

    private record ReferenceInfo(String referenceType, String referenceId) {
        private static ReferenceInfo empty() {
            return new ReferenceInfo(null, null);
        }
    }
}

