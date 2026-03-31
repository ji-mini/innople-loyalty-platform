package com.innople.loyalty.controller;

import com.innople.loyalty.config.AdminRoleResolver;
import com.innople.loyalty.config.ApiAuditLogInterceptor;
import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.PointDtos;
import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.user.AdminUser;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.repository.PointLedgerRepository;
import com.innople.loyalty.domain.member.MembershipGrade;
import com.innople.loyalty.service.points.PointEarnCalculator;
import com.innople.loyalty.service.points.PointOperationResult;
import com.innople.loyalty.service.points.PointService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {

    private static final String SOURCE_ADMIN_MANUAL_EARN = "ADMIN_WEB_MANUAL_EARN";
    private static final String SOURCE_POS_PURCHASE_EARN = "POS_PURCHASE_EARN";

    private final PointService pointService;
    private final PointLedgerRepository pointLedgerRepository;
    private final MemberRepository memberRepository;
    private final AdminRoleResolver adminRoleResolver;

    @GetMapping("/ledgers")
    public List<PointDtos.PointLedgerResponse> ledgers(
            @RequestParam(required = false) String memberNo,
            @RequestParam(defaultValue = "100") int limit
    ) {
        UUID tenantId = TenantContext.requireTenantId();
        int size = Math.min(Math.max(limit, 1), 500);
        return pointLedgerRepository.findLedgersForTenant(
                tenantId,
                memberNo != null && !memberNo.isBlank() ? memberNo.trim() : null,
                PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
    }

    @PostMapping("/earn")
    public PointDtos.PointOperationResponse earn(@Valid @RequestBody PointDtos.EarnRequest request, HttpServletRequest httpRequest) {
        UUID tenantId = TenantContext.requireTenantId();
        long resolvedAmount;
        Long purchaseAmountForLedger = null;
        Long totalPurchaseAmountForLedger = null;
        Long discountAmountForLedger = null;
        String sourceChannel;

        if (request.purchaseAmount() != null) {
            if (request.amount() != null) {
                throw new IllegalArgumentException("amount와 purchaseAmount는 동시에 지정할 수 없습니다.");
            }
            if (request.purchaseAmount() <= 0) {
                throw new IllegalArgumentException("purchaseAmount는 양수여야 합니다.");
            }
            Member member = memberRepository.findByTenantIdAndIdWithMembershipGrade(tenantId, request.memberId())
                    .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
            MembershipGrade grade = member.getMembershipGrade();
            if (grade == null) {
                throw new IllegalArgumentException("회원 등급이 없어 적립 대상 금액 기준 적립을 할 수 없습니다.");
            }
            BigDecimal rate = grade.getEarnRatePercent();
            resolvedAmount = PointEarnCalculator.pointsFromPurchase(request.purchaseAmount(), rate);
            if (resolvedAmount <= 0) {
                throw new IllegalArgumentException("적립 포인트가 0 이하입니다. 적립률·적립 대상 금액을 확인하세요.");
            }
            purchaseAmountForLedger = request.purchaseAmount();
            totalPurchaseAmountForLedger = request.totalPurchaseAmount();
            discountAmountForLedger = request.discountAmount();
            sourceChannel = SOURCE_POS_PURCHASE_EARN;
        } else {
            if (request.amount() == null || request.amount() <= 0) {
                throw new IllegalArgumentException("amount는 양수이거나, purchaseAmount로 적립 대상 금액 기준 적립을 지정하세요.");
            }
            resolvedAmount = request.amount();
            sourceChannel = SOURCE_ADMIN_MANUAL_EARN;
        }

        PointOperationResult result = pointService.earn(
                request.memberId(),
                resolvedAmount,
                request.expiresAt(),
                request.reason(),
                request.approvalNo(),
                request.referenceType(),
                request.referenceId(),
                purchaseAmountForLedger,
                totalPurchaseAmountForLedger,
                discountAmountForLedger,
                sourceChannel
        );
        setPointAuditMessage(httpRequest, request.memberId(), "지급", resolvedAmount, true);
        return new PointDtos.PointOperationResponse(
                result.ledgerId(),
                result.approvalNo(),
                result.eventType(),
                result.amount(),
                result.currentBalance(),
                result.occurredAt()
        );
    }

    @PostMapping("/use")
    public PointDtos.PointOperationResponse use(@Valid @RequestBody PointDtos.UseRequest request, HttpServletRequest httpRequest) {
        PointOperationResult result = pointService.use(
                request.memberId(),
                request.amount(),
                request.reason(),
                request.approvalNo(),
                request.referenceType(),
                request.referenceId()
        );
        setPointAuditMessage(httpRequest, request.memberId(), "차감", request.amount(), false);
        return new PointDtos.PointOperationResponse(
                result.ledgerId(),
                result.approvalNo(),
                result.eventType(),
                result.amount(),
                result.currentBalance(),
                result.occurredAt()
        );
    }

    @PostMapping("/expire/manual")
    public PointDtos.PointOperationResponse manualExpire(@Valid @RequestBody PointDtos.ManualExpireRequest request, HttpServletRequest httpRequest) {
        PointOperationResult result = pointService.manualExpire(
                request.memberId(),
                request.referenceAt(),
                request.reason(),
                request.approvalNo(),
                request.referenceType(),
                request.referenceId()
        );
        long absAmount = Math.abs(result.amount());
        setPointExpireAuditMessage(httpRequest, request.memberId(), absAmount);
        return new PointDtos.PointOperationResponse(
                result.ledgerId(),
                result.approvalNo(),
                result.eventType(),
                result.amount(),
                result.currentBalance(),
                result.occurredAt()
        );
    }

    private void setPointAuditMessage(HttpServletRequest httpRequest, UUID memberId, String verb, long amount, boolean plusSign) {
        UUID tenantId = TenantContext.requireTenantId();
        String memberNo = memberRepository.findByTenantIdAndId(tenantId, memberId)
                .map(Member::getMemberNo)
                .orElse("알 수 없음");
        AdminUser admin = adminRoleResolver.resolve(httpRequest);
        String adminName = admin != null ? admin.getName() : "관리자";
        String amountPart = plusSign ? "+%dP".formatted(amount) : "-%dP".formatted(amount);
        ApiAuditLogInterceptor.setAuditMessage(
                httpRequest,
                "포인트 %s (%s → 회원 %s, %s)".formatted(verb, adminName, memberNo, amountPart)
        );
    }

    private void setPointExpireAuditMessage(HttpServletRequest httpRequest, UUID memberId, long absAmount) {
        UUID tenantId = TenantContext.requireTenantId();
        String memberNo = memberRepository.findByTenantIdAndId(tenantId, memberId)
                .map(Member::getMemberNo)
                .orElse("알 수 없음");
        AdminUser admin = adminRoleResolver.resolve(httpRequest);
        String adminName = admin != null ? admin.getName() : "관리자";
        ApiAuditLogInterceptor.setAuditMessage(
                httpRequest,
                "포인트 소멸 (%s → 회원 %s, %dP)".formatted(adminName, memberNo, absAmount)
        );
    }
}

