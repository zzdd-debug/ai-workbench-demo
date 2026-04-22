import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { appEnv } from '../config/env'

export const useAppStore = defineStore('app', () => {
  const providerMode = ref(appEnv.providerMode)
  const apiBaseUrl = ref(appEnv.apiBaseUrl)
  const ragEnabled = ref(appEnv.ragEnabled)
  const mcpEnabled = ref(appEnv.mcpEnabled)

  const capabilitySummary = computed(() => {
    return [
      `Provider: ${providerMode.value}`,
      `RAG placeholder: ${ragEnabled.value ? 'enabled' : 'reserved'}`,
      `MCP placeholder: ${mcpEnabled.value ? 'enabled' : 'reserved'}`,
    ]
  })

  return {
    providerMode,
    apiBaseUrl,
    ragEnabled,
    mcpEnabled,
    capabilitySummary,
  }
})
