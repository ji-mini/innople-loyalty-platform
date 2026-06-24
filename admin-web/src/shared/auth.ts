import { api } from './api'
import { clearSession, getSession, setSession, SESSION_TTL_MS, type AdminSession } from './storage'
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

/**
 * 서버에 새 액세스 토큰을 요청해 세션을 슬라이딩 연장합니다.
 * 서버 토큰이 이미 만료된 경우 401이 반환되어 자동 로그아웃(api 인터셉터)됩니다.
 */
export async function refreshSession(): Promise<AdminSession | null> {
  const current = getSession()
  if (!current) return null
  try {
    const res = await api.post<AdminLoginResponse>('/api/v1/admin/auth/refresh')
    const next: AdminSession = {
      ...current,
      accessToken: res.data.accessToken,
      adminUserId: res.data.adminUserId,
      phoneNumber: res.data.phoneNumber,
      email: res.data.email,
      name: res.data.name,
      role: res.data.role ?? current.role,
      expiresAt: Date.now() + SESSION_TTL_MS,
    }
    setSession(next)
    return next
  } catch {
    // 401 등 실패 시 api 응답 인터셉터가 세션 정리/리다이렉트를 처리합니다.
    return null
  }
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

