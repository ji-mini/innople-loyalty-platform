package com.innople.loyalty.service.member;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.member.MembershipGrade;
import com.innople.loyalty.repository.MembershipGradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipGradeServiceImpl implements MembershipGradeService {

    private final MembershipGradeRepository membershipGradeRepository;

    @Override
    @Transactional
    public MembershipGradeItem create(String name, int level, String description, BigDecimal earnRatePercent) {
        UUID tenantId = TenantContext.requireTenantId();
        if (membershipGradeRepository.findByTenantIdAndLevel(tenantId, level).isPresent()) {
            throw new MembershipGradeExceptions.LevelAlreadyExistsException("해당 레벨의 등급이 이미 존재합니다: " + level);
        }
        MembershipGrade grade = new MembershipGrade(
                name != null ? name.trim() : "",
                level,
                description != null ? description.trim() : null,
                earnRatePercent != null ? earnRatePercent : BigDecimal.ZERO
        );
        MembershipGrade saved = membershipGradeRepository.save(grade);
        return toItem(saved);
    }

    @Override
    @Transactional
    public MembershipGradeItem update(UUID id, String name, int level, String description, BigDecimal earnRatePercent) {
        UUID tenantId = TenantContext.requireTenantId();
        MembershipGrade grade = membershipGradeRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new MembershipGradeExceptions.MembershipGradeNotFoundException("회원등급을 찾을 수 없습니다: " + id));
        membershipGradeRepository.findByTenantIdAndLevel(tenantId, level)
                .filter(g -> !g.getId().equals(id))
                .ifPresent(g -> {
                    throw new MembershipGradeExceptions.LevelAlreadyExistsException("해당 레벨의 등급이 이미 존재합니다: " + level);
                });
        grade.update(
                name != null ? name.trim() : grade.getName(),
                level,
                description != null ? description.trim() : grade.getDescription(),
                earnRatePercent != null ? earnRatePercent : BigDecimal.ZERO
        );
        return toItem(membershipGradeRepository.save(grade));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.requireTenantId();
        MembershipGrade grade = membershipGradeRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new MembershipGradeExceptions.MembershipGradeNotFoundException("회원등급을 찾을 수 없습니다: " + id));
        membershipGradeRepository.delete(grade);
    }

    private static MembershipGradeItem toItem(MembershipGrade g) {
        return new MembershipGradeItem(
                g.getId(),
                String.valueOf(g.getLevel()),
                g.getName(),
                g.getDescription(),
                g.getEarnRatePercent()
        );
    }
}
