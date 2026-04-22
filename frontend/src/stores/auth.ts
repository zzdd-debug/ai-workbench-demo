import axios from 'axios'
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { fetchCurrentUser, login } from '../services/auth'
import type { AuthUser } from '../types/auth'

const TOKEN_KEY = 'ai-rd-workbench-demo.token'
const USER_KEY = 'ai-rd-workbench-demo.user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(loadToken())
  const currentUser = ref<AuthUser | null>(loadUser())
  const loggingIn = ref(false)
  const authReady = ref(false)
  const errorMessage = ref<string | null>(null)

  const isAuthenticated = computed(() => token.value.length > 0 && currentUser.value !== null)

  const bootstrap = async (): Promise<void> => {
    if (!token.value) {
      authReady.value = true
      return
    }

    try {
      currentUser.value = await fetchCurrentUser()
      persist()
      errorMessage.value = null
    }
    catch {
      clearSession()
    }
    finally {
      authReady.value = true
    }
  }

  const loginWithPassword = async (username: string, password: string): Promise<boolean> => {
    loggingIn.value = true
    errorMessage.value = null

    try {
      const response = await login({ username, password })
      token.value = response.token
      currentUser.value = response.user
      persist()
      return true
    }
    catch (error) {
      clearSession(false)
      errorMessage.value = extractErrorMessage(error)
      return false
    }
    finally {
      loggingIn.value = false
      authReady.value = true
    }
  }

  const logout = (): void => {
    clearSession()
  }

  const persist = (): void => {
    localStorage.setItem(TOKEN_KEY, token.value)
    localStorage.setItem(USER_KEY, JSON.stringify(currentUser.value))
  }

  const clearSession = (clearError = true): void => {
    token.value = ''
    currentUser.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    if (clearError) {
      errorMessage.value = null
    }
  }

  return {
    authReady,
    currentUser,
    errorMessage,
    isAuthenticated,
    loggingIn,
    token,
    bootstrap,
    loginWithPassword,
    logout,
  }
})

const loadToken = (): string => localStorage.getItem(TOKEN_KEY) ?? ''

const loadUser = (): AuthUser | null => {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw) as AuthUser
  }
  catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

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

  return 'Login failed. Please try again.'
}
