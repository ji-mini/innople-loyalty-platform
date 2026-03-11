import type { AdminRole } from './types'

export type AdminSession = {
  tenantId: string
  accessToken: string
  adminUserId: string
  phoneNumber: string
  email: string | null
  name: string
  role: AdminRole
}

type AdminLoginRemember = {
  tenantId: string
  phoneNumber: string
  remember: boolean
}

const KEY = 'admin_session_v1'
const REMEMBER_KEY = 'admin_login_remember_v1'

export function getSession(): AdminSession | null {
  const raw = localStorage.getItem(KEY)
  if (!raw) return null
  try {
    const v = JSON.parse(raw) as Partial<AdminSession>
    if (!v?.tenantId || !v.accessToken || !v.adminUserId) return null
    return {
      tenantId: v.tenantId,
      accessToken: v.accessToken,
      adminUserId: v.adminUserId,
      phoneNumber: v.phoneNumber ?? '',
      email: v.email ?? null,
      name: v.name ?? '',
      role: (v.role as AdminRole) ?? 'OPERATOR',
    }
  } catch {
    return null
  }
}

export function setSession(session: AdminSession): void {
  localStorage.setItem(KEY, JSON.stringify(session))
}

export function clearSession(): void {
  localStorage.removeItem(KEY)
}

export function getLoginRemember(): AdminLoginRemember | null {
  const raw = localStorage.getItem(REMEMBER_KEY)
  if (!raw) return null
  try {
    const v = JSON.parse(raw) as AdminLoginRemember
    if (!v?.remember) return null
    if (!v.tenantId || !v.phoneNumber) return null
    return v
  } catch {
    return null
  }
}

export function setLoginRemember(value: AdminLoginRemember | null): void {
  if (!value || !value.remember) {
    localStorage.removeItem(REMEMBER_KEY)
    return
  }
  localStorage.setItem(REMEMBER_KEY, JSON.stringify(value))
}

