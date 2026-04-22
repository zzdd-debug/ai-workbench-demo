import { http } from '../lib/http'
import type {
  ApiResponse,
  CreateTaskPayload,
  TaskCard,
  TaskDetail,
  TaskKnowledgeReference,
  TaskLog,
  UpdateTaskStepStatusPayload,
} from '../types/task'

const unwrap = <T>(response: ApiResponse<T>): T => {
  if (!response.success) {
    throw new Error(response.error?.message ?? 'Request failed.')
  }
  return response.data
}

export const createTask = async (payload: CreateTaskPayload): Promise<TaskCard> => {
  const { data } = await http.post<ApiResponse<TaskCard>>('/tasks', payload)
  return unwrap(data)
}

export const fetchTasks = async (): Promise<TaskCard[]> => {
  const { data } = await http.get<ApiResponse<TaskCard[]>>('/tasks')
  return unwrap(data)
}

export const fetchTaskDetail = async (taskId: string): Promise<TaskDetail> => {
  const { data } = await http.get<ApiResponse<TaskDetail>>(`/tasks/${taskId}`)
  return unwrap(data)
}

export const fetchTaskLogs = async (taskId: string): Promise<TaskLog[]> => {
  const { data } = await http.get<ApiResponse<TaskLog[]>>(`/tasks/${taskId}/logs`)
  return unwrap(data)
}

export const fetchTaskKnowledge = async (taskId: string): Promise<TaskKnowledgeReference[]> => {
  const { data } = await http.get<ApiResponse<TaskKnowledgeReference[]>>(`/tasks/${taskId}/knowledge`)
  return unwrap(data)
}

export const updateTaskStepStatus = async (
  taskId: string,
  stepId: string,
  payload: UpdateTaskStepStatusPayload,
): Promise<TaskDetail> => {
  const { data } = await http.patch<ApiResponse<TaskDetail>>(`/tasks/${taskId}/steps/${stepId}/status`, payload)
  return unwrap(data)
}
