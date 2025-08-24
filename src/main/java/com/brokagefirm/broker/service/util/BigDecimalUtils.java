package com.brokagefirm.broker.service.util;

import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static com.brokagefirm.broker.service.config.ServiceConfigParams.*;

@NoArgsConstructor
public final class BigDecimalUtils {
    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b, MC).setScale(SCALE, ROUNDING_MODE);
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return a.subtract(b, MC).setScale(SCALE, ROUNDING_MODE);
    }

    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return a.multiply(b, MC).setScale(SCALE, ROUNDING_MODE);
    }

    public static BigDecimal scale(BigDecimal a) {
        return a.setScale(SCALE, ROUNDING_MODE);
    }
}


