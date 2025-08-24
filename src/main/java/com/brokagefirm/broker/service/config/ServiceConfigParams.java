package com.brokagefirm.broker.service.config;

import org.springframework.beans.factory.annotation.Value;

import java.math.MathContext;
import java.math.RoundingMode;

public class ServiceConfigParams {
    public static final MathContext MC = new MathContext(18, RoundingMode.HALF_UP);
    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    @Value("${broker.applicationConfig.defaultPageSize}")
    public static final int DEFAULT_PAGE_SIZE = 10;

    private ServiceConfigParams() {
        throw new IllegalStateException("ServiceConfig class constructor called");
    }
}
