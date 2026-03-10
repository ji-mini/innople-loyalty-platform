import axios from 'axios'
import { getSession } from './storage'

export const api = axios.create({
  // In dev, keep this empty to use Vite proxy (see vite.config.ts).
  // In deployed environments, set VITE_API_BASE_URL at build time (e.g. "http://52.79.x.x:3201").
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
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

