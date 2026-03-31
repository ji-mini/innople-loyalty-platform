package com.innople.loyalty.service.points;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.repository.CommonCodeRepository;
import com.innople.loyalty.domain.points.PointAccount;
import com.innople.loyalty.domain.points.PointAllocation;
import com.innople.loyalty.domain.points.PointEventType;
import com.innople.loyalty.domain.points.PointLedger;
import com.innople.loyalty.domain.points.PointLot;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

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
    private static final String ADMIN_WEB_MANUAL_USE = "ADMIN_WEB_MANUAL_USE";
    private static final String ADMIN_WEB_MANUAL_EXPIRE = "ADMIN_WEB_MANUAL_EXPIRE";
    private static final String SYSTEM_AUTO_EXPIRE = "SYSTEM_AUTO_EXPIRE";

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
    public PointOperationResult use(UUID memberId, long amount, String reason, String approvalNo,
                                    String referenceType, String referenceId) {
        if (amount <= 0) {
            throw new InvalidPointAmountException("amount must be positive");
        }

        UUID tenantId = TenantContext.requireTenantId();
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
                        ADMIN_WEB_MANUAL_USE,
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

