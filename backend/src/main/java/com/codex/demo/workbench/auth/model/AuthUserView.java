package com.codex.demo.workbench.auth.model;

public record AuthUserView(
    String userId,
    String username,
    String displayName
) {
}
