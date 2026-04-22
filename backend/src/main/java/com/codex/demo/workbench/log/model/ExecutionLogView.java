package com.codex.demo.workbench.log.model;

import java.util.Map;

public record ExecutionLogView(
    String logId,
    String taskId,
    String type,
    String level,
    String module,
    String message,
    String createdAt,
    Map<String, String> context
) {
}
