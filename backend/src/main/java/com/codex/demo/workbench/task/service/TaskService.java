package com.codex.demo.workbench.task.service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.codex.demo.workbench.knowledge.model.KnowledgeReferenceView;
import com.codex.demo.workbench.log.model.ExecutionLogView;
import com.codex.demo.workbench.task.api.CreateTaskRequest;
import com.codex.demo.workbench.task.api.UpdateTaskStepStatusRequest;
import com.codex.demo.workbench.task.model.LatestRunView;
import com.codex.demo.workbench.task.model.TaskCardView;
import com.codex.demo.workbench.task.model.TaskDetailView;
import com.codex.demo.workbench.task.model.TaskState;
import com.codex.demo.workbench.task.model.TaskStatus;
import com.codex.demo.workbench.task.model.TaskStepRecord;
import com.codex.demo.workbench.task.model.TaskStepStatus;
import com.codex.demo.workbench.task.model.TaskSummaryRecord;

@Service
public class TaskService {

    private final AtomicLong taskSequence = new AtomicLong(1);
    private final Map<String, TaskState> tasks = new ConcurrentHashMap<>();

    public TaskCardView createTask(CreateTaskRequest request) {
        String rawPrompt = normalizePrompt(request.rawPrompt());
        String now = now();
        String taskId = "task-" + String.format("%03d", taskSequence.getAndIncrement());
        String providerMode = normalizeProviderMode(request.providerMode());
        String title = normalizeTitle(request.title(), rawPrompt);
        String creatorId = normalizeCreatorId(request.creatorId());

        TaskState state = new TaskState(
            taskId,
            title,
            rawPrompt,
            rawPrompt,
            TaskStatus.NOT_STARTED,
            TaskStatus.NOT_STARTED.boardColumn(),
            providerMode,
            "P0",
            creatorId,
            null,
            null,
            null,
            List.of(),
            now,
            now
        );

        tasks.put(taskId, state);
        return toCardView(state);
    }

    public List<TaskCardView> listTasks() {
        return tasks.values().stream()
            .sorted(Comparator.comparing(TaskState::updatedAt).reversed())
            .map(this::toCardView)
            .toList();
    }

    public TaskDetailView getTaskDetail(String taskId) {
        return toDetailView(requireTask(taskId));
    }

    public TaskDetailView updateTaskStepStatus(String taskId, String stepId, UpdateTaskStepStatusRequest request) {
        TaskState current = requireTask(taskId);
        TaskStepStatus targetStatus = parseStepStatus(request);
        String updatedAt = now();
        boolean found = false;

        List<TaskStepRecord> updatedSteps = current.steps().stream()
            .map(step -> {
                if (!step.stepId().equals(stepId)) {
                    return step;
                }

                return new TaskStepRecord(
                    step.stepId(),
                    step.taskId(),
                    step.title(),
                    step.description(),
                    targetStatus,
                    step.sortOrder(),
                    step.ownerModule(),
                    step.createdAt(),
                    updatedAt
                );
            })
            .toList();

        for (TaskStepRecord step : current.steps()) {
            if (step.stepId().equals(stepId)) {
                found = true;
                break;
            }
        }

        if (!found) {
            throw new NoSuchElementException("Task step does not exist: " + stepId);
        }

        TaskStatus taskStatus = TaskStatus.fromSteps(updatedSteps);
        TaskSummaryRecord summary = buildSummary(current, updatedSteps, taskStatus, updatedAt);

        TaskState updated = new TaskState(
            current.taskId(),
            current.title(),
            current.rawPrompt(),
            current.normalizedGoal(),
            taskStatus,
            taskStatus.boardColumn(),
            current.providerMode(),
            current.priority(),
            current.creatorId(),
            current.latestRunId(),
            current.latestRunStatus(),
            summary,
            updatedSteps,
            current.createdAt(),
            updatedAt
        );

        tasks.put(taskId, updated);
        return toDetailView(updated);
    }

    public List<ExecutionLogView> listTaskLogs(String taskId) {
        TaskState state = requireTask(taskId);
        String runId = state.latestRunId() == null ? "run-pending" : state.latestRunId();
        String stepCount = String.valueOf(state.steps().size());
        String latestStepTitle = state.steps().isEmpty()
            ? "Waiting for mock decomposition."
            : state.steps().get(Math.min(1, state.steps().size() - 1)).title();

        return List.of(
            new ExecutionLogView(
                taskId + "-log-001",
                taskId,
                "decision",
                "INFO",
                "task",
                "Task accepted and queued for mock planning.",
                state.createdAt(),
                orderedContext(
                    "providerMode", state.providerMode(),
                    "taskStatus", state.status().name()
                )
            ),
            new ExecutionLogView(
                taskId + "-log-002",
                taskId,
                "tool",
                "INFO",
                "agent",
                "MockProvider generated the current task breakdown.",
                state.updatedAt(),
                orderedContext(
                    "runId", runId,
                    "stepCount", stepCount,
                    "latestStep", latestStepTitle
                )
            ),
            new ExecutionLogView(
                taskId + "-log-003",
                taskId,
                "knowledge",
                "INFO",
                "knowledge",
                "Static knowledge references were prepared for the task detail panel.",
                state.updatedAt(),
                orderedContext(
                    "sourceCount", "3",
                    "sourceScope", "docs/knowledge/*"
                )
            ),
            new ExecutionLogView(
                taskId + "-log-004",
                taskId,
                "error",
                "WARN",
                "agent",
                "Real provider invocation was skipped because mock mode is enforced in Feature 3.",
                state.updatedAt(),
                orderedContext(
                    "providerMode", state.providerMode(),
                    "fallback", "MockProvider"
                )
            )
        );
    }

    public List<KnowledgeReferenceView> listTaskKnowledge(String taskId) {
        requireTask(taskId);

        return List.of(
            new KnowledgeReferenceView(
                taskId + "-knowledge-001",
                taskId,
                "模块拆分结果",
                "docs/knowledge/modules/module-split.md",
                "用于保持 task 作为主实体聚合模块，并让日志与知识引用保持独立展示能力。",
                "task 模块负责看板聚合，log 和 knowledge 作为独立展示能力存在。"
            ),
            new KnowledgeReferenceView(
                taskId + "-knowledge-002",
                taskId,
                "接口契约草案",
                "docs/knowledge/contracts/api-draft.md",
                "用于对齐 Feature 3 的步骤状态更新与任务摘要返回结构。",
                "任务详情继续读取 /tasks/{taskId}，并新增 PATCH /tasks/{taskId}/steps/{stepId}/status。"
            ),
            new KnowledgeReferenceView(
                taskId + "-knowledge-003",
                taskId,
                "数据结构草案",
                "docs/knowledge/data-models/domain-draft.md",
                "用于保持任务级摘要与步骤状态汇总的一致性。",
                "顶层 task.status 由步骤状态汇总，摘要第一版只保留任务级摘要。"
            )
        );
    }

    public TaskState requireTask(String taskId) {
        TaskState state = tasks.get(taskId);
        if (state == null) {
            throw new NoSuchElementException("Task does not exist: " + taskId);
        }
        return state;
    }

    public TaskState applyAgentResult(
        String taskId,
        String latestRunId,
        String latestRunStatus,
        String normalizedGoal,
        List<TaskStepRecord> steps,
        TaskSummaryRecord summary
    ) {
        TaskState current = requireTask(taskId);
        TaskStatus taskStatus = TaskStatus.fromSteps(steps);
        String updatedAt = now();

        TaskState updated = new TaskState(
            current.taskId(),
            current.title(),
            current.rawPrompt(),
            normalizedGoal,
            taskStatus,
            taskStatus.boardColumn(),
            current.providerMode(),
            current.priority(),
            current.creatorId(),
            latestRunId,
            latestRunStatus,
            summary,
            List.copyOf(steps),
            current.createdAt(),
            updatedAt
        );

        tasks.put(taskId, updated);
        return updated;
    }

    private TaskCardView toCardView(TaskState state) {
        int completedStepCount = (int) state.steps().stream()
            .filter(step -> step.status() == TaskStepStatus.COMPLETED)
            .count();

        return new TaskCardView(
            state.taskId(),
            state.title(),
            state.status(),
            state.boardColumn(),
            state.priority(),
            state.steps().size(),
            completedStepCount,
            state.latestRunStatus(),
            state.updatedAt()
        );
    }

    private TaskDetailView toDetailView(TaskState state) {
        LatestRunView latestRun = state.latestRunId() == null
            ? null
            : new LatestRunView(state.latestRunId(), state.latestRunStatus());

        return new TaskDetailView(
            state.taskId(),
            state.title(),
            state.rawPrompt(),
            state.normalizedGoal(),
            state.status(),
            state.boardColumn(),
            state.providerMode(),
            state.summary(),
            state.steps(),
            latestRun,
            List.of(),
            List.of()
        );
    }

    private TaskSummaryRecord buildSummary(
        TaskState current,
        List<TaskStepRecord> steps,
        TaskStatus taskStatus,
        String updatedAt
    ) {
        int completedCount = (int) steps.stream()
            .filter(step -> step.status() == TaskStepStatus.COMPLETED)
            .count();
        int inProgressCount = (int) steps.stream()
            .filter(step -> step.status() == TaskStepStatus.IN_PROGRESS)
            .count();
        int verificationCount = (int) steps.stream()
            .filter(step -> step.status() == TaskStepStatus.PENDING_VERIFICATION)
            .count();

        String headline = switch (taskStatus) {
            case NOT_STARTED -> "任务已创建，等待开始执行";
            case IN_PROGRESS -> "任务正在推进，关键步骤已进入执行";
            case PENDING_VERIFICATION -> "任务进入待验证阶段，等待验收确认";
            case COMPLETED -> "任务步骤已全部完成，可以进入下一阶段";
        };

        String body = String.format(
            "当前共 %d 个步骤，其中 %d 个已完成，%d 个进行中，%d 个待验证。",
            steps.size(),
            completedCount,
            inProgressCount,
            verificationCount
        );

        String nextAction = switch (taskStatus) {
            case NOT_STARTED -> "从第一步开始推进任务执行。";
            case IN_PROGRESS -> "继续更新步骤状态，直到进入待验证或完成。";
            case PENDING_VERIFICATION -> "优先完成验收确认，确认后将步骤标记为 COMPLETED。";
            case COMPLETED -> "当前任务已闭环，可准备进入下一功能交付。";
        };

        String summaryId = current.summary() == null ? "summary-" + current.taskId() : current.summary().summaryId();
        String generatedByRunId = current.latestRunId() == null ? "manual-update" : current.latestRunId();

        return new TaskSummaryRecord(
            summaryId,
            current.taskId(),
            headline,
            body,
            nextAction,
            generatedByRunId,
            updatedAt
        );
    }

    private TaskStepStatus parseStepStatus(UpdateTaskStepStatusRequest request) {
        if (request == null || request.status() == null || request.status().isBlank()) {
            throw new IllegalArgumentException("status must not be blank.");
        }

        try {
            return TaskStepStatus.valueOf(request.status().trim());
        }
        catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported task step status: " + request.status());
        }
    }

    private String normalizePrompt(String rawPrompt) {
        if (rawPrompt == null || rawPrompt.isBlank()) {
            throw new IllegalArgumentException("rawPrompt must not be blank.");
        }
        return rawPrompt.trim();
    }

    private String normalizeProviderMode(String providerMode) {
        if (providerMode == null || providerMode.isBlank()) {
            return "mock";
        }
        if (!"mock".equalsIgnoreCase(providerMode.trim())) {
            throw new IllegalArgumentException("Only mock provider mode is supported in Feature 3.");
        }
        return "mock";
    }

    private String normalizeTitle(String title, String rawPrompt) {
        if (title != null && !title.isBlank()) {
            return title.trim();
        }

        String normalized = rawPrompt.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 24) {
            return normalized;
        }
        return normalized.substring(0, 24) + "...";
    }

    private String normalizeCreatorId(String creatorId) {
        if (creatorId == null || creatorId.isBlank()) {
            return "user-demo-001";
        }
        return creatorId.trim();
    }

    private Map<String, String> orderedContext(String... keyValues) {
        Map<String, String> context = new LinkedHashMap<>();
        for (int index = 0; index + 1 < keyValues.length; index += 2) {
            context.put(keyValues[index], keyValues[index + 1]);
        }
        return context;
    }

    private String now() {
        return OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }
}
