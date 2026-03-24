package com.innople.loyalty.service.points;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.points.PointAccount;
import com.innople.loyalty.repository.PointAccountRepository;
import com.innople.loyalty.repository.PointLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointAccountReconciliationService {
    private final PointAccountRepository pointAccountRepository;
    private final PointLedgerRepository pointLedgerRepository;

    @Transactional
    public ReconcileResult reconcileMember(UUID memberId) {
        UUID tenantId = TenantContext.requireTenantId();
        long ledgerBalance = pointLedgerRepository.sumBalanceByTenantIdAndMemberId(tenantId, memberId);

        PointAccount account = pointAccountRepository.findWithLockByTenantIdAndMemberId(tenantId, memberId)
                .orElseGet(() -> pointAccountRepository.save(new PointAccount(memberId)));

        long previousBalance = account.getCurrentBalance();
        boolean corrected = previousBalance != ledgerBalance;
        if (corrected) {
            account.addBalance(ledgerBalance - previousBalance);
            pointAccountRepository.save(account);
        }

        return new ReconcileResult(memberId, ledgerBalance, previousBalance, account.getCurrentBalance(), corrected);
    }

    @Transactional
    public TenantReconcileResult reconcileCurrentTenant() {
        UUID tenantId = TenantContext.requireTenantId();
        Set<UUID> memberIds = new LinkedHashSet<>();
        pointAccountRepository.findAllByTenantId(tenantId).forEach(account -> memberIds.add(account.getMemberId()));
        memberIds.addAll(pointLedgerRepository.findDistinctMemberIdsByTenantId(tenantId));

        int correctedCount = 0;
        for (UUID memberId : memberIds) {
            if (reconcileMember(memberId).corrected()) {
                correctedCount++;
            }
        }
        return new TenantReconcileResult(memberIds.size(), correctedCount);
    }

    public record ReconcileResult(
            UUID memberId,
            long ledgerBalance,
            long previousBalance,
            long currentBalance,
            boolean corrected
    ) {
    }

    public record TenantReconcileResult(
            int processedAccountCount,
            int correctedAccountCount
    ) {
    }
}
