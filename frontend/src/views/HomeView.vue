<script setup lang="ts">
import { Lightning, Promotion, Tickets, TrendCharts } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { computed, onMounted } from 'vue'

import { useAppStore } from '../stores/app'
import { useTaskBoardStore } from '../stores/taskBoard'
import type { ExecutionLogType, TaskLog, TaskStatus, TaskStepStatus } from '../types/task'

const appStore = useAppStore()
const taskBoardStore = useTaskBoardStore()

const { apiBaseUrl, providerMode } = storeToRefs(appStore)
const {
  currentTask,
  detailLoading,
  draftPrompt,
  errorMessage,
  latestRun,
  loading,
  selectedTaskId,
  stepUpdatingId,
  submitting,
  taskKnowledge,
  taskLogs,
  tasks,
} = storeToRefs(taskBoardStore)

const boardColumns: Array<{ status: TaskStatus; title: string; helper: string; icon: typeof Tickets }> = [
  { status: 'NOT_STARTED', title: '待开始', helper: '等待进入执行的任务。', icon: Tickets },
  { status: 'IN_PROGRESS', title: '进行中', helper: '正在推进中的任务。', icon: Lightning },
  { status: 'PENDING_VERIFICATION', title: '待验证', helper: '等待确认与验收。', icon: TrendCharts },
  { status: 'COMPLETED', title: '已完成', helper: '已经可以进入下一步。', icon: Promotion },
]

const stepStatusOptions: Array<{ label: string; value: TaskStepStatus }> = [
  { label: '待开始', value: 'NOT_STARTED' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '待验证', value: 'PENDING_VERIFICATION' },
  { label: '已完成', value: 'COMPLETED' },
]

const tasksByStatus = computed<Record<TaskStatus, typeof tasks.value>>(() => {
  return boardColumns.reduce(
    (groups, column) => {
      groups[column.status] = tasks.value.filter(task => task.status === column.status)
      return groups
    },
    {
      NOT_STARTED: [],
      IN_PROGRESS: [],
      PENDING_VERIFICATION: [],
      COMPLETED: [],
    } as Record<TaskStatus, typeof tasks.value>,
  )
})

const stepTagType = (status: TaskStatus): 'info' | 'warning' | 'success' => {
  switch (status) {
    case 'NOT_STARTED':
      return 'info'
    case 'IN_PROGRESS':
      return 'warning'
    case 'PENDING_VERIFICATION':
      return 'warning'
    case 'COMPLETED':
      return 'success'
  }
}

const logTagType = (type: ExecutionLogType): 'success' | 'warning' | 'primary' | 'danger' => {
  switch (type) {
    case 'knowledge':
      return 'success'
    case 'decision':
      return 'primary'
    case 'tool':
      return 'warning'
    case 'error':
      return 'danger'
  }
}

const contextEntries = (context: TaskLog['context']) => Object.entries(context)

const selectTask = (taskId: string) => {
  void taskBoardStore.selectTask(taskId)
}

const submitPrompt = () => {
  void taskBoardStore.submitPrompt()
}

const updateStepStatus = (taskId: string, stepId: string, status: TaskStepStatus) => {
  void taskBoardStore.setTaskStepStatus(taskId, stepId, status)
}

const onStepStatusChange = (taskId: string, stepId: string, value: unknown) => {
  updateStepStatus(taskId, stepId, value as TaskStepStatus)
}

onMounted(() => {
  void taskBoardStore.loadTasks()
})
</script>

<template>
  <div class="page-shell">
    <section class="hero-panel">
      <div class="hero-copy-block">
        <p class="eyebrow">
          Feature Delivery
        </p>
        <h1>自然语言输入到任务摘要与步骤状态更新</h1>
        <p class="hero-copy">
          当前页面延续已通过的主链路，并在任务详情中补上任务级摘要完善与步骤状态手动更新能力。
          顶层任务状态继续由步骤状态自动汇总，不提供顶层状态手动修改。
        </p>
      </div>
      <div class="hero-grid">
        <el-card shadow="never" class="metric-card">
          <span class="metric-label">Provider Mode</span>
          <strong>{{ providerMode }}</strong>
        </el-card>
        <el-card shadow="never" class="metric-card">
          <span class="metric-label">API Base URL</span>
          <strong>{{ apiBaseUrl }}</strong>
        </el-card>
        <el-card shadow="never" class="metric-card">
          <span class="metric-label">Latest Run</span>
          <strong>{{ latestRun?.status ?? 'WAITING' }}</strong>
        </el-card>
      </div>
    </section>

    <section class="feature-grid">
      <el-card shadow="hover" class="composer-card">
        <template #header>
          <div class="card-header">
            <span>任务输入区</span>
            <el-tag effect="light" type="info">MockProvider</el-tag>
          </div>
        </template>
        <p class="section-copy">
          输入自然语言任务描述后，系统会创建任务并触发 Mock Agent 生成任务拆解。
          之后你可以直接在步骤卡片中手动更新步骤状态，并观察摘要与看板状态联动变化。
        </p>
        <el-input
          v-model="draftPrompt"
          type="textarea"
          :rows="5"
          resize="none"
          maxlength="280"
          show-word-limit
          placeholder="例如：帮我拆解一个 AI 研发任务工作台 Demo 的 Feature 3 交付任务。"
        />
        <div class="composer-actions">
          <el-button type="primary" :loading="submitting" @click="submitPrompt">
            提交任务并生成拆解
          </el-button>
          <span class="helper-text">最小链路：Task -> Agent -> Board -> Summary -> Step Status</span>
        </div>
        <el-alert
          v-if="errorMessage"
          title="请求失败"
          type="error"
          :description="errorMessage"
          show-icon
          class="inline-alert"
        />
      </el-card>

      <el-card shadow="hover" class="summary-card">
        <template #header>
          <div class="card-header">
            <span>当前任务摘要</span>
            <el-tag v-if="currentTask" effect="light">{{ currentTask.boardColumn }}</el-tag>
          </div>
        </template>
        <div v-if="currentTask" class="summary-content">
          <h2>{{ currentTask.title }}</h2>
          <p class="summary-text">{{ currentTask.summary?.headline ?? '等待生成任务级摘要。' }}</p>
          <p class="summary-subtext">{{ currentTask.summary?.body ?? currentTask.normalizedGoal }}</p>
          <el-alert
            v-if="currentTask.summary?.nextAction"
            title="下一步建议"
            type="success"
            :description="currentTask.summary.nextAction"
            :closable="false"
            show-icon
          />
          <div class="summary-metadata">
            <span>任务状态：{{ currentTask.status }}</span>
            <span>运行模式：{{ currentTask.providerMode }}</span>
            <span>步骤数量：{{ currentTask.steps.length }}</span>
            <span v-if="currentTask.summary">摘要更新时间：{{ currentTask.summary.updatedAt }}</span>
          </div>
        </div>
        <el-empty v-else description="提交任务后，这里会展示任务级摘要。" />
      </el-card>
    </section>

    <section class="board-section">
      <div class="section-headline">
        <div>
          <p class="eyebrow">Task Board</p>
          <h2>列式分栏看板</h2>
        </div>
        <el-tag effect="plain" type="warning">{{ tasks.length }} 个任务</el-tag>
      </div>
      <div class="board-grid">
        <section v-for="column in boardColumns" :key="column.status" class="board-column">
          <header class="board-column-header">
            <div class="board-title-group">
              <component :is="column.icon" class="board-icon" />
              <div>
                <h3>{{ column.title }}</h3>
                <p>{{ column.helper }}</p>
              </div>
            </div>
            <span class="board-count">{{ tasksByStatus[column.status].length }}</span>
          </header>

          <div class="board-list">
            <button
              v-for="task in tasksByStatus[column.status]"
              :key="task.taskId"
              type="button"
              class="task-card"
              :class="{ selected: selectedTaskId === task.taskId }"
              @click="selectTask(task.taskId)"
            >
              <div class="task-card-top">
                <strong>{{ task.title }}</strong>
                <el-tag size="small" effect="light">{{ task.status }}</el-tag>
              </div>
              <p>{{ task.boardColumn }}</p>
              <div class="task-card-footer">
                <span>{{ task.completedStepCount }}/{{ task.stepCount }} steps</span>
                <span>{{ task.latestRunStatus ?? 'WAITING' }}</span>
              </div>
            </button>

            <el-empty
              v-if="tasksByStatus[column.status].length === 0"
              :description="loading ? '正在读取任务...' : '当前列暂无任务。'"
            />
          </div>
        </section>
      </div>
    </section>

    <section class="detail-section">
      <div class="section-headline">
        <div>
          <p class="eyebrow">Task Detail</p>
          <h2>任务拆解结果</h2>
        </div>
        <el-tag v-if="currentTask?.latestRun" effect="plain" type="success">
          {{ currentTask.latestRun.status }}
        </el-tag>
      </div>

      <el-card shadow="hover" class="detail-card">
        <template #header>
          <div class="card-header">
            <span>步骤卡片</span>
            <span v-if="currentTask">{{ currentTask.steps.length }} steps</span>
          </div>
        </template>

        <template v-if="currentTask">
          <div class="detail-meta">
            <p><strong>原始输入：</strong>{{ currentTask.rawPrompt }}</p>
            <p><strong>结构化目标：</strong>{{ currentTask.normalizedGoal }}</p>
          </div>

          <div class="step-grid">
            <article v-for="step in currentTask.steps" :key="step.stepId" class="step-card">
              <div class="step-top">
                <span class="step-index">STEP {{ step.sortOrder }}</span>
                <el-tag :type="stepTagType(step.status)" effect="light">
                  {{ step.status }}
                </el-tag>
              </div>
              <h3>{{ step.title }}</h3>
              <p>{{ step.description }}</p>
              <div class="step-actions">
                <span class="step-action-label">手动更新状态</span>
                <el-select
                  :model-value="step.status"
                  size="small"
                  class="step-status-select"
                  :disabled="stepUpdatingId === step.stepId"
                  @change="onStepStatusChange(step.taskId, step.stepId, $event)"
                >
                  <el-option
                    v-for="option in stepStatusOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </div>
              <div class="step-footer">
                <span>owner: {{ step.ownerModule }}</span>
                <span>{{ stepUpdatingId === step.stepId ? 'updating...' : step.updatedAt }}</span>
              </div>
            </article>
          </div>
        </template>

        <el-empty v-else description="提交任务后，这里会展示至少 3 个步骤卡片。" />
      </el-card>
    </section>

    <section class="insight-section">
      <div class="section-headline">
        <div>
          <p class="eyebrow">Feature 2</p>
          <h2>执行日志与知识引用</h2>
        </div>
        <el-tag v-if="currentTask" effect="plain" type="success">
          {{ currentTask.title }}
        </el-tag>
      </div>

      <div class="insight-grid">
        <el-card shadow="hover" class="insight-card">
          <template #header>
            <div class="card-header">
              <span>执行日志</span>
              <span v-if="currentTask">{{ taskLogs.length }} 条记录</span>
            </div>
          </template>

          <template v-if="currentTask">
            <div v-if="taskLogs.length > 0" class="log-list">
              <article v-for="log in taskLogs" :key="log.logId" class="log-item">
                <div class="log-top">
                  <div class="log-tag-group">
                    <el-tag :type="logTagType(log.type)" effect="dark">{{ log.type }}</el-tag>
                    <el-tag effect="light">{{ log.module }}</el-tag>
                    <el-tag effect="plain" size="small">{{ log.level }}</el-tag>
                  </div>
                  <span class="log-time">{{ log.createdAt }}</span>
                </div>
                <p class="log-message">{{ log.message }}</p>
                <div v-if="contextEntries(log.context).length > 0" class="log-context">
                  <span
                    v-for="[key, value] in contextEntries(log.context)"
                    :key="`${log.logId}-${key}`"
                    class="log-context-chip"
                  >
                    {{ key }}: {{ value }}
                  </span>
                </div>
              </article>
            </div>
            <el-empty v-else :description="detailLoading ? '正在读取执行日志...' : '当前任务暂无执行日志。'" />
          </template>

          <el-empty v-else description="选择或提交任务后，这里会展示当前任务的执行日志。" />
        </el-card>

        <el-card shadow="hover" class="insight-card">
          <template #header>
            <div class="card-header">
              <span>知识引用</span>
              <span v-if="currentTask">{{ taskKnowledge.length }} 条引用</span>
            </div>
          </template>

          <template v-if="currentTask">
            <div v-if="taskKnowledge.length > 0" class="knowledge-list">
              <article v-for="item in taskKnowledge" :key="item.knowledgeId" class="knowledge-item">
                <div class="knowledge-top">
                  <h3>{{ item.label }}</h3>
                  <el-tag effect="light" type="success">static</el-tag>
                </div>
                <p class="knowledge-path">{{ item.path }}</p>
                <p class="knowledge-reason">{{ item.reason }}</p>
                <p class="knowledge-excerpt">{{ item.excerpt }}</p>
              </article>
            </div>
            <el-empty v-else :description="detailLoading ? '正在读取知识引用...' : '当前任务暂无知识引用。'" />
          </template>

          <el-empty v-else description="选择或提交任务后，这里会展示当前任务的知识引用。" />
        </el-card>
      </div>
    </section>
  </div>
</template>
