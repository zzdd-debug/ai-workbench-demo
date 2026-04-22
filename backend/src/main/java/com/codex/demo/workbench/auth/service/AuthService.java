package com.codex.demo.workbench.auth.service;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.codex.demo.workbench.auth.api.AuthLoginRequest;
import com.codex.demo.workbench.auth.model.AuthLoginView;
import com.codex.demo.workbench.auth.model.AuthUserView;

@Service
public class AuthService {

    private static final String DEMO_USERNAME = "demo";
    private static final String DEMO_PASSWORD = "123456";
    private static final String DEMO_TOKEN = "mock-token-demo";
    private static final AuthUserView DEMO_USER = new AuthUserView(
        "user-demo-001",
        DEMO_USERNAME,
        "Demo User"
    );

    public AuthLoginView login(AuthLoginRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            throw new IllegalArgumentException("username and password must not be blank.");
        }

        if (!DEMO_USERNAME.equals(request.username().trim()) || !DEMO_PASSWORD.equals(request.password().trim())) {
            throw new UnauthorizedException("Invalid username or password.");
        }

        return new AuthLoginView(DEMO_TOKEN, DEMO_USER);
    }

    public AuthUserView me(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (!DEMO_TOKEN.equals(token)) {
            throw new UnauthorizedException("Invalid or missing token.");
        }
        return DEMO_USER;
    }

    private String extractToken(String authorizationHeader) {
        if (isBlank(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing token.");
        }
        return authorizationHeader.substring("Bearer ".length()).trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static String bearerHeader() {
        return HttpHeaders.AUTHORIZATION;
    }
}
