const parseFlag = (value: string | undefined, fallback: boolean): boolean => {
  if (value === undefined) {
    return fallback
  }

  return value.toLowerCase() === 'true'
}

export const appEnv = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? 'http://127.0.0.1:5050',
  providerMode: import.meta.env.VITE_APP_PROVIDER_MODE ?? 'mock',
  ragEnabled: parseFlag(import.meta.env.VITE_APP_RAG_ENABLED, false),
  mcpEnabled: parseFlag(import.meta.env.VITE_APP_MCP_ENABLED, false),
} as const
