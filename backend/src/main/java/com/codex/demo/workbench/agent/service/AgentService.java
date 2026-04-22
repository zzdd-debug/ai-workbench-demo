package com.codex.demo.workbench.agent.service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.codex.demo.workbench.agent.api.CreateAgentRunRequest;
import com.codex.demo.workbench.agent.model.AgentIntent;
import com.codex.demo.workbench.agent.model.AgentRunRecord;
import com.codex.demo.workbench.agent.model.AgentRunStatus;
import com.codex.demo.workbench.task.model.TaskState;
import com.codex.demo.workbench.task.model.TaskStepRecord;
import com.codex.demo.workbench.task.model.TaskStepStatus;
import com.codex.demo.workbench.task.model.TaskSummaryRecord;
import com.codex.demo.workbench.task.service.TaskService;

@Service
public class AgentService {

    private final AtomicLong runSequence = new AtomicLong(1);
    private final AtomicLong summarySequence = new AtomicLong(1);
    private final AtomicLong stepSequence = new AtomicLong(1);

    private final TaskService taskService;

    public AgentService(TaskService taskService) {
        this.taskService = taskService;
    }

    public AgentRunRecord createRun(CreateAgentRunRequest request) {
        TaskState task = taskService.requireTask(request.taskId());
        validateProviderMode(request.providerMode());

        String startedAt = now();
        String finishedAt = now();
        String runId = "run-" + String.format("%03d", runSequence.getAndIncrement());
        String summaryId = "summary-" + String.format("%03d", summarySequence.getAndIncrement());
        String normalizedGoal = "Complete the minimal Feature 3 workflow for: " + task.rawPrompt();

        List<TaskStepRecord> steps = List.of(
            step(task.taskId(), "Clarify task goal", "Identify the delivery target, boundary, and mock-mode constraints.", TaskStepStatus.COMPLETED, 1, startedAt),
            step(task.taskId(), "Prepare summary view", "Keep a task-level summary ready for detail page display.", TaskStepStatus.IN_PROGRESS, 2, startedAt),
            step(task.taskId(), "Verify board aggregation", "Confirm that top-level task status is derived from step statuses.", TaskStepStatus.PENDING_VERIFICATION, 3, startedAt),
            step(task.taskId(), "Finalize feature acceptance", "Leave the task ready for manual status updates and final verification.", TaskStepStatus.NOT_STARTED, 4, startedAt)
        );

        TaskSummaryRecord summary = new TaskSummaryRecord(
            summaryId,
            task.taskId(),
            "Feature 3 task breakdown has been generated",
            "The current task includes a task-level summary and supports manual step status updates in mock mode.",
            "Update step statuses on the task detail cards to verify summary and board aggregation.",
            runId,
            finishedAt
        );

        taskService.applyAgentResult(
            task.taskId(),
            runId,
            AgentRunStatus.SUCCEEDED.name(),
            normalizedGoal,
            steps,
            summary
        );

        return new AgentRunRecord(
            runId,
            task.taskId(),
            AgentRunStatus.SUCCEEDED,
            new AgentIntent("feature-delivery-p0", normalizedGoal),
            steps,
            summary,
            startedAt,
            finishedAt
        );
    }

    private TaskStepRecord step(
        String taskId,
        String title,
        String description,
        TaskStepStatus status,
        int sortOrder,
        String timestamp
    ) {
        String stepId = "step-" + String.format("%03d", stepSequence.getAndIncrement());
        return new TaskStepRecord(
            stepId,
            taskId,
            title,
            description,
            status,
            sortOrder,
            "task",
            timestamp,
            timestamp
        );
    }

    private void validateProviderMode(String providerMode) {
        if (providerMode == null || providerMode.isBlank()) {
            return;
        }
        if (!"mock".equalsIgnoreCase(providerMode.trim())) {
            throw new IllegalArgumentException("Only mock provider mode is supported in Feature 3.");
        }
    }

    private String now() {
        return OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }
}
