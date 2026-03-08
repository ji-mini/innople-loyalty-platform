import { api } from './api'
import { clearSession, setSession, type AdminSession } from './storage'
import type { AdminLoginRequest, AdminLoginResponse } from './types'

export async function login(tenantId: string, req: AdminLoginRequest): Promise<AdminSession> {
  const res = await api.post<AdminLoginResponse>('/api/v1/admin/auth/login', req, {
    headers: { 'X-Tenant-Id': tenantId },
  })

  const session: AdminSession = {
    tenantId,
    accessToken: res.data.accessToken,
    adminUserId: res.data.adminUserId,
    email: res.data.email,
    name: res.data.name,
  }

  setSession(session)
  return session
}

export function logout(): void {
  clearSession()
}

