package com.brokagefirm.broker.security;

import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SecurityParams {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String REGISTER_PATH = "/api/auth/register";
    public static final String LOGIN_PATH = "/api/auth/authenticate";
    public static final String REFRESH_TOKEN_PATH = "/api/auth/refresh-token";
    public static final String LOGOUT_PATH = "/api/auth/logout";
    public static final String H2_CONSOLE_PATH = "/h2-console/**";
    public static final String OPEN_API_DOCS_PATH = "/v3/api-docs/**";
    public static final String SWAGGER_UI_PATH = "/swagger-ui/**";
    public static final String[] AUTH_WHITELIST = {
            LOGIN_PATH,
            REFRESH_TOKEN_PATH,
            REGISTER_PATH,
            H2_CONSOLE_PATH,
            OPEN_API_DOCS_PATH,
            SWAGGER_UI_PATH
    };
    public static final List<String> CORS_ALLOWED_ORIGINS = new ArrayList<>(Arrays.asList(
            "http://localhost:3000"
    ));
    @Value("${broker.applicationConfig.defaultPageSize}")
    public static final int DEFAULT_PAGE_SIZE = 10;

    private SecurityParams() {
        throw new IllegalStateException("SecurityParams class");
    }
}
