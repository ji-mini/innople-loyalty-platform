package com.innople.loyalty.service.points;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PointEarnCalculator {

    private PointEarnCalculator() {
    }

    /**
     * 적립 대상 금액(원)과 적립률(%)으로 적립 포인트를 계산합니다. 소수점 이하는 버립니다.
     */
    public static long pointsFromPurchase(long purchaseAmountKrw, BigDecimal earnRatePercent) {
        if (purchaseAmountKrw <= 0) {
            return 0L;
        }
        if (earnRatePercent == null || earnRatePercent.signum() <= 0) {
            return 0L;
        }
        BigDecimal purchase = BigDecimal.valueOf(purchaseAmountKrw);
        return purchase.multiply(earnRatePercent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
                .longValue();
    }
}
