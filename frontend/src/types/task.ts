export type TaskStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'PENDING_VERIFICATION' | 'COMPLETED'
export type TaskStepStatus = TaskStatus
export type AgentRunStatus = 'QUEUED' | 'RUNNING' | 'SUCCEEDED' | 'FAILED'
export type ExecutionLogType = 'knowledge' | 'decision' | 'tool' | 'error'
export type LogLevel = 'INFO' | 'WARN' | 'ERROR'

export interface ApiError {
  code: string
  message: string
}

export interface ApiResponse<T> {
  success: boolean
  traceId: string
  data: T
  error?: ApiError | null
}

export interface TaskSummary {
  summaryId: string
  taskId: string
  headline: string
  body: string
  nextAction: string
  generatedByRunId: string
  updatedAt: string
}

export interface TaskStep {
  stepId: string
  taskId: string
  title: string
  description: string
  status: TaskStepStatus
  sortOrder: number
  ownerModule: string
  createdAt: string
  updatedAt: string
}

export interface TaskLog {
  logId: string
  taskId: string
  type: ExecutionLogType
  level: LogLevel
  module: string
  message: string
  createdAt: string
  context: Record<string, string>
}

export interface TaskKnowledgeReference {
  knowledgeId: string
  taskId: string
  label: string
  path: string
  reason: string
  excerpt: string
}

export interface TaskCard {
  taskId: string
  title: string
  status: TaskStatus
  boardColumn: string
  priority: string
  stepCount: number
  completedStepCount: number
  latestRunStatus: string | null
  updatedAt: string
}

export interface LatestRun {
  runId: string
  status: string
}

export interface TaskDetail {
  taskId: string
  title: string
  rawPrompt: string
  normalizedGoal: string
  status: TaskStatus
  boardColumn: string
  providerMode: string
  summary: TaskSummary | null
  steps: TaskStep[]
  latestRun: LatestRun | null
  recentLogs: TaskLog[]
  citations: TaskKnowledgeReference[]
}

export interface AgentIntent {
  category: string
  goal: string
}

export interface AgentRun {
  runId: string
  taskId: string
  status: AgentRunStatus
  intent: AgentIntent
  steps: TaskStep[]
  summaryDraft: TaskSummary
  startedAt: string
  finishedAt: string
}

export interface CreateTaskPayload {
  title?: string
  rawPrompt: string
  creatorId: string
  providerMode: 'mock'
}

export interface CreateAgentRunPayload {
  taskId: string
  providerMode: 'mock'
  triggeredBy: string
}

export interface UpdateTaskStepStatusPayload {
  status: TaskStepStatus
}
