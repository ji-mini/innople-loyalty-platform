package com.innople.loyalty.domain.batch;

import com.innople.loyalty.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 테넌트 × 배치 당 1행의 운영 설정. 운영자가 화면에서 편집한다.
 * <p>V32/V33 스키마(batch_job_config)에 매핑한다. id/tenant_id/created_at/updated_at 는 {@link BaseEntity}가 관리한다.</p>
 * <p>threshold_days 는 배치에 따라 무의미할 수 있어 nullable (V33: NULL 또는 &gt; 0).</p>
 */
@Entity
@Table(
        name = "batch_job_config",
        indexes = {
                @Index(name = "uk_batch_job_config_tenant_batch", columnList = "tenantId,batchName", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchJobConfig extends BaseEntity {

    @Column(nullable = false, length = 100, updatable = false)
    private String batchName;

    @Column(nullable = false)
    private boolean enabled;

    /** 실행 시각(hour). 0~23. named CHECK(ck_batch_job_config_run_hour)와 일치. */
    @Column(nullable = false)
    private short runHour;

    /**
     * 유예기간 일수. 배치에 따라 미사용(null) 가능.
     * named CHECK(ck_batch_job_config_threshold_days): NULL OR &gt; 0.
     */
    @Column
    private Integer thresholdDays;

    /** 마지막 수정 관리자. 최초 seed/시스템 생성은 null 가능. */
    @Column
    private UUID updatedBy;

    public static BatchJobConfig create(
            String batchName,
            boolean enabled,
            short runHour,
            Integer thresholdDays,
            UUID updatedBy
    ) {
        validateRunHour(runHour);
        validateThresholdDays(thresholdDays);
        BatchJobConfig config = new BatchJobConfig();
        config.batchName = requireText(batchName);
        config.enabled = enabled;
        config.runHour = runHour;
        config.thresholdDays = thresholdDays;
        config.updatedBy = updatedBy;
        return config;
    }

    /** enabled / run_hour / threshold_days 를 수정한다. batch_name 은 변경하지 않는다. */
    public void update(boolean enabled, short runHour, Integer thresholdDays, UUID updatedBy) {
        validateRunHour(runHour);
        validateThresholdDays(thresholdDays);
        this.enabled = enabled;
        this.runHour = runHour;
        this.thresholdDays = thresholdDays;
        this.updatedBy = updatedBy;
    }

    private static void validateRunHour(short runHour) {
        if (runHour < 0 || runHour > 23) {
            throw new IllegalArgumentException("runHour must be between 0 and 23");
        }
    }

    private static void validateThresholdDays(Integer thresholdDays) {
        if (thresholdDays != null && thresholdDays <= 0) {
            throw new IllegalArgumentException("thresholdDays must be null or greater than 0");
        }
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("batchName must not be blank");
        }
        return value.trim();
    }
}
