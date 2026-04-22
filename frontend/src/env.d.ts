/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<Record<string, never>, Record<string, never>, unknown>
  export default component
}

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string
  readonly VITE_APP_PROVIDER_MODE?: string
  readonly VITE_APP_RAG_ENABLED?: string
  readonly VITE_APP_MCP_ENABLED?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
