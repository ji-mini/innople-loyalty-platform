package com.innople.loyalty.service.points;

import com.innople.loyalty.config.TenantContext;
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
import java.util.UUID;

import static com.innople.loyalty.service.points.PointExceptions.InsufficientPointsException;
import static com.innople.loyalty.service.points.PointExceptions.InvalidPointAmountException;
import static com.innople.loyalty.service.points.PointExceptions.PointAccountNotFoundException;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointAccountRepository pointAccountRepository;
    private final PointLotRepository pointLotRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final PointAllocationRepository pointAllocationRepository;

    @Override
    @Transactional
    public PointOperationResult earn(UUID memberId, long amount, Instant expiresAt, String reason) {
        if (amount <= 0) {
            throw new InvalidPointAmountException("amount must be positive");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt must not be null");
        }

        UUID tenantId = TenantContext.requireTenantId();
        Instant now = Instant.now();
        if (!expiresAt.isAfter(now)) {
            throw new IllegalArgumentException("expiresAt must be in the future");
        }

        PointAccount account = pointAccountRepository
                .findWithLockByTenantIdAndMemberId(tenantId, memberId)
                .orElseGet(() -> pointAccountRepository.save(new PointAccount(memberId)));

        PointLedger ledger = pointLedgerRepository.save(
                new PointLedger(account.getId(), memberId, PointEventType.EARN, amount, reason)
        );

        pointLotRepository.save(new PointLot(account.getId(), memberId, amount, expiresAt));

        account.addBalance(amount);
        pointAccountRepository.save(account);

        return new PointOperationResult(
                ledger.getId(),
                ledger.getEventType(),
                ledger.getAmount(),
                account.getCurrentBalance(),
                ledger.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public PointOperationResult use(UUID memberId, long amount, String reason) {
        if (amount <= 0) {
            throw new InvalidPointAmountException("amount must be positive");
        }

        UUID tenantId = TenantContext.requireTenantId();
        PointAccount account = pointAccountRepository
                .findWithLockByTenantIdAndMemberId(tenantId, memberId)
                .orElseThrow(() -> new PointAccountNotFoundException("PointAccount not found"));

        if (account.getCurrentBalance() < amount) {
            throw new InsufficientPointsException("Insufficient points");
        }

        Instant now = Instant.now();
        PointLedger ledger = pointLedgerRepository.save(
                new PointLedger(account.getId(), memberId, PointEventType.USE, -amount, reason)
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
                ledger.getEventType(),
                ledger.getAmount(),
                account.getCurrentBalance(),
                ledger.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public PointOperationResult manualExpire(UUID memberId, Instant referenceAt, String reason) {
        UUID tenantId = TenantContext.requireTenantId();
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
                    PointEventType.EXPIRE_MANUAL,
                    0L,
                    account.getCurrentBalance(),
                    Instant.now()
            );
        }

        PointLedger ledger = pointLedgerRepository.save(
                new PointLedger(account.getId(), memberId, PointEventType.EXPIRE_MANUAL, -totalToExpire, reason)
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
                ledger.getEventType(),
                ledger.getAmount(),
                account.getCurrentBalance(),
                ledger.getCreatedAt()
        );
    }
}

