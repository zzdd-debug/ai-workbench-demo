package com.codex.demo.workbench.agent.api;

public record CreateAgentRunRequest(
    String taskId,
    String providerMode,
    String triggeredBy
) {
}
