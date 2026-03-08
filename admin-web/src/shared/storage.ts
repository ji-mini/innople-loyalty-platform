export type AdminSession = {
  tenantId: string
  accessToken: string
  adminUserId: string
  email: string
  name: string
}

const KEY = 'admin_session_v1'

export function getSession(): AdminSession | null {
  const raw = localStorage.getItem(KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as AdminSession
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

