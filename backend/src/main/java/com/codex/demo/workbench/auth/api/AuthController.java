package com.codex.demo.workbench.auth.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codex.demo.workbench.auth.model.AuthLoginView;
import com.codex.demo.workbench.auth.model.AuthUserView;
import com.codex.demo.workbench.auth.service.AuthService;
import com.codex.demo.workbench.common.api.ApiResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthLoginView> login(@RequestBody AuthLoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<AuthUserView> me(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ApiResponse.success(authService.me(authorizationHeader));
    }
}
