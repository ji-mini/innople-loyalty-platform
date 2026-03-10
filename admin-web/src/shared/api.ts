import axios from 'axios'
import { getSession } from './storage'

type RuntimeAppConfig = {
  API_BASE_URL?: string
}

function getRuntimeApiBaseUrl(): string | undefined {
  const w = window as any
  const cfg: RuntimeAppConfig | undefined = w?.__APP_CONFIG__
  const v = cfg?.API_BASE_URL?.trim()
  return v ? v : undefined
}

function inferApiBaseUrl(): string {
  if (import.meta.env.DEV) return ''
  // Default inference for simple deployments:
  // - admin-web: http://<host>:8090
  // - backend:   http://<host>:3201
  return `${location.protocol}//${location.hostname}:3201`
}

function getEnvApiBaseUrl(): string | undefined {
  const v = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.trim()
  return v ? v : undefined
}

export const api = axios.create({
  // In dev, keep this empty to use Vite proxy (see vite.config.ts).
  // In deployed environments, set VITE_API_BASE_URL at build time (e.g. "http://52.79.x.x:3201").
  baseURL: getRuntimeApiBaseUrl() ?? getEnvApiBaseUrl() ?? inferApiBaseUrl(),
  timeout: 15000,
})

api.interceptors.request.use((config) => {
  const session = getSession()
  if (session) {
    config.headers = config.headers ?? {}
    config.headers['X-Tenant-Id'] = session.tenantId
    config.headers['Authorization'] = `Bearer ${session.accessToken}`
  }
  return config
})

