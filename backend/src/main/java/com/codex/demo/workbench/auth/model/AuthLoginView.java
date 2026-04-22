package com.codex.demo.workbench.auth.model;

public record AuthLoginView(
    String token,
    AuthUserView user
) {
}
