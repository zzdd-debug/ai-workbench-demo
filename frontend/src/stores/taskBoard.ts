import axios from 'axios'
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { createAgentRun } from '../services/agent'
import { createTask, fetchTaskDetail, fetchTaskKnowledge, fetchTaskLogs, fetchTasks, updateTaskStepStatus } from '../services/task'
import type {
  AgentRun,
  TaskCard,
  TaskDetail,
  TaskKnowledgeReference,
  TaskLog,
  TaskStepStatus,
} from '../types/task'

const DEFAULT_PROMPT = '帮我拆解一个 AI 研发任务工作台 Demo 的 Feature 3 交付任务。'

export const useTaskBoardStore = defineStore('taskBoard', () => {
  const tasks = ref<TaskCard[]>([])
  const currentTask = ref<TaskDetail | null>(null)
  const latestRun = ref<AgentRun | null>(null)
  const taskLogs = ref<TaskLog[]>([])
  const taskKnowledge = ref<TaskKnowledgeReference[]>([])
  const draftPrompt = ref(DEFAULT_PROMPT)
  const selectedTaskId = ref<string | null>(null)
  const submitting = ref(false)
  const loading = ref(false)
  const detailLoading = ref(false)
  const stepUpdatingId = ref<string | null>(null)
  const errorMessage = ref<string | null>(null)

  const hasTasks = computed(() => tasks.value.length > 0)

  const loadTasks = async (): Promise<void> => {
    loading.value = true
    try {
      tasks.value = await fetchTasks()
      if (!selectedTaskId.value && tasks.value.length > 0) {
        await selectTask(tasks.value[0].taskId)
      }
      if (tasks.value.length === 0) {
        currentTask.value = null
        taskLogs.value = []
        taskKnowledge.value = []
      }
    }
    catch (error) {
      errorMessage.value = extractErrorMessage(error)
    }
    finally {
      loading.value = false
    }
  }

  const selectTask = async (taskId: string): Promise<void> => {
    selectedTaskId.value = taskId
    detailLoading.value = true
    try {
      const [taskDetail, logs, knowledge] = await Promise.all([
        fetchTaskDetail(taskId),
        fetchTaskLogs(taskId),
        fetchTaskKnowledge(taskId),
      ])

      currentTask.value = taskDetail
      taskLogs.value = logs
      taskKnowledge.value = knowledge
      errorMessage.value = null
    }
    catch (error) {
      currentTask.value = null
      taskLogs.value = []
      taskKnowledge.value = []
      errorMessage.value = extractErrorMessage(error)
    }
    finally {
      detailLoading.value = false
    }
  }

  const submitPrompt = async (): Promise<void> => {
    const rawPrompt = draftPrompt.value.trim()

    if (!rawPrompt) {
      errorMessage.value = '请输入一条自然语言任务。'
      return
    }

    submitting.value = true
    errorMessage.value = null

    try {
      const task = await createTask({
        rawPrompt,
        creatorId: 'user-demo-001',
        providerMode: 'mock',
      })

      latestRun.value = await createAgentRun({
        taskId: task.taskId,
        providerMode: 'mock',
        triggeredBy: 'user-demo-001',
      })

      tasks.value = await fetchTasks()
      await selectTask(task.taskId)
    }
    catch (error) {
      errorMessage.value = extractErrorMessage(error)
    }
    finally {
      submitting.value = false
    }
  }

  const setTaskStepStatus = async (taskId: string, stepId: string, status: TaskStepStatus): Promise<void> => {
    stepUpdatingId.value = stepId
    errorMessage.value = null

    try {
      currentTask.value = await updateTaskStepStatus(taskId, stepId, { status })
      taskLogs.value = await fetchTaskLogs(taskId)
      taskKnowledge.value = await fetchTaskKnowledge(taskId)
      tasks.value = await fetchTasks()
    }
    catch (error) {
      errorMessage.value = extractErrorMessage(error)
    }
    finally {
      stepUpdatingId.value = null
    }
  }

  return {
    currentTask,
    detailLoading,
    draftPrompt,
    errorMessage,
    hasTasks,
    latestRun,
    loading,
    selectedTaskId,
    stepUpdatingId,
    submitting,
    taskKnowledge,
    taskLogs,
    tasks,
    loadTasks,
    selectTask,
    setTaskStepStatus,
    submitPrompt,
  }
})

const extractErrorMessage = (error: unknown): string => {
  if (axios.isAxiosError(error)) {
    const responseMessage = error.response?.data?.error?.message
    if (typeof responseMessage === 'string' && responseMessage.length > 0) {
      return responseMessage
    }
  }

  if (error instanceof Error) {
    return error.message
  }

  return '请求失败，请稍后重试。'
}
