package com.innople.loyalty.controller;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.PosPointDtos;
import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.points.PointLedger;
import com.innople.loyalty.domain.points.PointPolicy;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.repository.PointAccountRepository;
import com.innople.loyalty.repository.PointLedgerRepository;
import com.innople.loyalty.repository.PointPolicyRepository;
import com.innople.loyalty.service.points.PointExceptions;
import com.innople.loyalty.service.points.PointOperationResult;
import com.innople.loyalty.service.points.PointPolicyExceptions;
import com.innople.loyalty.service.points.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/points")
@RequiredArgsConstructor
public class PosPointController {

    private static final String SOURCE_POS_PURCHASE_EARN = "POS_PURCHASE_EARN";
    private static final String SOURCE_POS_PURCHASE_USE = "POS_PURCHASE_USE";
    private static final String REF_POS_EARN_TXN = "POS_EARN_TXN";
    private static final String REF_POS_USE_TXN = "POS_USE_TXN";

    private final PointService pointService;
    private final MemberRepository memberRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final PointAccountRepository pointAccountRepository;
    private final PointPolicyRepository pointPolicyRepository;

    @PostMapping("/earn")
    public PosPointDtos.PosPointOperationResponse earn(@Valid @RequestBody PosPointDtos.PosEarnRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        Member member = resolveMember(tenantId, request.memberId(), request.memberNo());
        String referenceId = buildReferenceId(request.storeCode(), request.posNo(), request.transactionNo());

        Optional<PointLedger> existing = pointLedgerRepository
                .findFirstByTenantIdAndReferenceTypeAndReferenceIdOrderByCreatedAtDesc(tenantId, REF_POS_EARN_TXN, referenceId);
        if (existing.isPresent()) {
            return validateDuplicateEarnAndRespond(member, request, existing.get());
        }

        Instant expiresAt = resolveEarnExpiresAt(tenantId);
        String reason = normalizeReason(request.reason(), "POS 적립");

        try {
            PointOperationResult result = pointService.earnFromPurchase(
                    member.getId(),
                    request.paymentAmount(),
                    request.totalPaymentAmount(),
                    request.discountAmount(),
                    expiresAt,
                    reason,
                    null,
                    REF_POS_EARN_TXN,
                    referenceId,
                    SOURCE_POS_PURCHASE_EARN
            );
            return new PosPointDtos.PosPointOperationResponse(
                    result.ledgerId(),
                    result.approvalNo(),
                    result.eventType(),
                    result.amount(),
                    result.currentBalance(),
                    result.occurredAt(),
                    false
            );
        } catch (DataIntegrityViolationException ex) {
            PointLedger ledger = pointLedgerRepository
                    .findFirstByTenantIdAndReferenceTypeAndReferenceIdOrderByCreatedAtDesc(tenantId, REF_POS_EARN_TXN, referenceId)
                    .orElseThrow(() -> ex);
            return validateDuplicateEarnAndRespond(member, request, ledger);
        }
    }

    @PostMapping("/use")
    public PosPointDtos.PosPointOperationResponse use(@Valid @RequestBody PosPointDtos.PosUseRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        Member member = resolveMember(tenantId, request.memberId(), request.memberNo());
        String referenceId = buildReferenceId(request.storeCode(), request.posNo(), request.transactionNo());

        Optional<PointLedger> existing = pointLedgerRepository
                .findFirstByTenantIdAndReferenceTypeAndReferenceIdOrderByCreatedAtDesc(tenantId, REF_POS_USE_TXN, referenceId);
        if (existing.isPresent()) {
            return validateDuplicateUseAndRespond(member, request, existing.get());
        }

        String reason = normalizeReason(request.reason(), "POS 사용");
        try {
            PointOperationResult result = pointService.use(
                    member.getId(),
                    request.amount(),
                    reason,
                    null,
                    REF_POS_USE_TXN,
                    referenceId,
                    SOURCE_POS_PURCHASE_USE
            );
            return new PosPointDtos.PosPointOperationResponse(
                    result.ledgerId(),
                    result.approvalNo(),
                    result.eventType(),
                    result.amount(),
                    result.currentBalance(),
                    result.occurredAt(),
                    false
            );
        } catch (DataIntegrityViolationException ex) {
            PointLedger ledger = pointLedgerRepository
                    .findFirstByTenantIdAndReferenceTypeAndReferenceIdOrderByCreatedAtDesc(tenantId, REF_POS_USE_TXN, referenceId)
                    .orElseThrow(() -> ex);
            return validateDuplicateUseAndRespond(member, request, ledger);
        }
    }

    private Member resolveMember(UUID tenantId, UUID memberId, String memberNo) {
        String normalizedMemberNo = normalizeOptional(memberNo);
        if (memberId == null && normalizedMemberNo == null) {
            throw new IllegalArgumentException("memberId 또는 memberNo 중 하나는 반드시 필요합니다.");
        }

        Member member;
        if (memberId != null) {
            member = memberRepository.findByTenantIdAndId(tenantId, memberId)
                    .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
            if (normalizedMemberNo != null && !normalizedMemberNo.equals(member.getMemberNo())) {
                throw new PointExceptions.DuplicatePointTransactionException("memberId와 memberNo가 서로 다른 회원을 가리킵니다.");
            }
            return member;
        }

        return memberRepository.findByTenantIdAndMemberNo(tenantId, normalizedMemberNo)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }

    private Instant resolveEarnExpiresAt(UUID tenantId) {
        PointPolicy policy = pointPolicyRepository.findFirstByTenantIdAndEnabledTrueOrderByUpdatedAtDesc(tenantId)
                .orElseThrow(() -> new PointPolicyExceptions.PointPolicyNotFoundException("활성 포인트 정책이 없습니다."));
        return Instant.now().plus(policy.getValidityDays(), ChronoUnit.DAYS);
    }

    private PosPointDtos.PosPointOperationResponse validateDuplicateEarnAndRespond(
            Member member,
            PosPointDtos.PosEarnRequest request,
            PointLedger existing
    ) {
        if (!Objects.equals(existing.getMemberId(), member.getId())) {
            throw new PointExceptions.DuplicatePointTransactionException("동일 transactionNo가 다른 회원으로 이미 처리되었습니다.");
        }
        if (!Objects.equals(existing.getPurchaseAmount(), request.paymentAmount())) {
            throw new PointExceptions.DuplicatePointTransactionException("동일 transactionNo가 다른 결제금액으로 이미 처리되었습니다.");
        }
        if (!Objects.equals(existing.getTotalPurchaseAmount(), request.totalPaymentAmount())) {
            throw new PointExceptions.DuplicatePointTransactionException("동일 transactionNo가 다른 총 결제금액으로 이미 처리되었습니다.");
        }
        if (!Objects.equals(existing.getDiscountAmount(), request.discountAmount())) {
            throw new PointExceptions.DuplicatePointTransactionException("동일 transactionNo가 다른 할인금액으로 이미 처리되었습니다.");
        }
        return toDuplicateResponse(member.getId(), existing);
    }

    private PosPointDtos.PosPointOperationResponse validateDuplicateUseAndRespond(
            Member member,
            PosPointDtos.PosUseRequest request,
            PointLedger existing
    ) {
        if (!Objects.equals(existing.getMemberId(), member.getId())) {
            throw new PointExceptions.DuplicatePointTransactionException("동일 transactionNo가 다른 회원으로 이미 처리되었습니다.");
        }
        if (Math.abs(existing.getAmount()) != request.amount()) {
            throw new PointExceptions.DuplicatePointTransactionException("동일 transactionNo가 다른 사용 포인트로 이미 처리되었습니다.");
        }
        return toDuplicateResponse(member.getId(), existing);
    }

    private PosPointDtos.PosPointOperationResponse toDuplicateResponse(UUID memberId, PointLedger ledger) {
        UUID tenantId = TenantContext.requireTenantId();
        long currentBalance = pointAccountRepository.findByTenantIdAndMemberId(tenantId, memberId)
                .map(account -> account.getCurrentBalance())
                .orElse(0L);

        return new PosPointDtos.PosPointOperationResponse(
                ledger.getId(),
                ledger.getApprovalNo(),
                ledger.getEventType(),
                ledger.getAmount(),
                currentBalance,
                ledger.getCreatedAt(),
                true
        );
    }

    private String buildReferenceId(String storeCode, String posNo, String transactionNo) {
        String normalizedStoreCode = normalizeRequired(storeCode, "storeCode");
        String normalizedPosNo = normalizeRequired(posNo, "posNo");
        String normalizedTransactionNo = normalizeRequired(transactionNo, "transactionNo");
        String referenceId = normalizedStoreCode + ":" + normalizedPosNo + ":" + normalizedTransactionNo;
        if (referenceId.length() > 100) {
            throw new IllegalArgumentException("referenceId는 100자를 초과할 수 없습니다.");
        }
        return referenceId;
    }

    private String normalizeReason(String reason, String defaultValue) {
        String normalized = normalizeOptional(reason);
        return normalized != null ? normalized : defaultValue;
    }

    private String normalizeRequired(String value, String fieldName) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + "는 필수입니다.");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
