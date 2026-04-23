import { http } from '../lib/http'
import type { AgentRun, ApiResponse, CreateAgentRunPayload } from '../types/task'

const unwrap = <T>(response: ApiResponse<T>): T => {
  if (!response.success) {
    throw new Error(response.error?.message ?? 'Request failed.')
  }
  return response.data
}

export const createAgentRun = async (payload: CreateAgentRunPayload): Promise<AgentRun> => {
  const { data } = await http.post<ApiResponse<AgentRun>>('/api/v1/agent/runs', payload)
  return unwrap(data)
}
