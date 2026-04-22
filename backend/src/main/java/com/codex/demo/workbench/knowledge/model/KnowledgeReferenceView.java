package com.codex.demo.workbench.knowledge.model;

public record KnowledgeReferenceView(
    String knowledgeId,
    String taskId,
    String label,
    String path,
    String reason,
    String excerpt
) {
}
