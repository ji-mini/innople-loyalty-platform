import axios from 'axios'
import { getSession } from './storage'

export const api = axios.create({
  baseURL: '',
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

