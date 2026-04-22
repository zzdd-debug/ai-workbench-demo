import axios from 'axios'

import { appEnv } from '../config/env'

export const http = axios.create({
  baseURL: appEnv.apiBaseUrl,
  timeout: 10000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('ai-rd-workbench-demo.token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
