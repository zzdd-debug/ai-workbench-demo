package com.codex.demo.workbench.task.api;

public record CreateTaskRequest(
    String title,
    String rawPrompt,
    String creatorId,
    String providerMode
) {
}
