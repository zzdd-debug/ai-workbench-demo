package com.codex.demo.workbench.auth.api;

public record AuthLoginRequest(
    String username,
    String password
) {
}
