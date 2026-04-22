import { http } from '../lib/http'
import type { ApiResponse } from '../types/task'
import type { AuthLoginPayload, AuthLoginResponse, AuthUser } from '../types/auth'

const unwrap = <T>(response: ApiResponse<T>): T => {
  if (!response.success) {
    throw new Error(response.error?.message ?? 'Request failed.')
  }
  return response.data
}

export const login = async (payload: AuthLoginPayload): Promise<AuthLoginResponse> => {
  const { data } = await http.post<ApiResponse<AuthLoginResponse>>('/auth/login', payload)
  return unwrap(data)
}

export const fetchCurrentUser = async (): Promise<AuthUser> => {
  const { data } = await http.get<ApiResponse<AuthUser>>('/auth/me')
  return unwrap(data)
}
