import { api } from './api'
import { clearSession, setSession, SESSION_TTL_MS, type AdminSession } from './storage'
import type { AdminLoginRequest, AdminLoginResponse, AdminRegisterRequest, AdminRegisterResponse } from './types'

export async function login(tenantId: string, req: AdminLoginRequest, tenantName?: string): Promise<AdminSession> {
  const res = await api.post<AdminLoginResponse>('/api/v1/admin/auth/login', req, {
    headers: { 'X-Tenant-Id': tenantId },
  })

  const now = Date.now()
  const session: AdminSession = {
    tenantId,
    tenantName,
    accessToken: res.data.accessToken,
    adminUserId: res.data.adminUserId,
    phoneNumber: res.data.phoneNumber,
    email: res.data.email,
    name: res.data.name,
    role: res.data.role ?? 'OPERATOR',
    expiresAt: now + SESSION_TTL_MS,
  }

  setSession(session)
  return session
}

export async function registerAdmin(tenantId: string, req: AdminRegisterRequest): Promise<AdminRegisterResponse> {
  const res = await api.post<AdminRegisterResponse>('/api/v1/admin/auth/register', req, {
    headers: { 'X-Tenant-Id': tenantId },
  })
  return res.data
}

export function logout(): void {
  clearSession()
}

