import type { AdminRole } from './types'

export const SESSION_TTL_MS = 30 * 60 * 1000
/** 만료 이전 이 시간 이내면 touch 시 연장 (5분) */
const TOUCH_THRESHOLD_MS = 5 * 60 * 1000

export type AdminSession = {
  tenantId: string
  tenantName?: string
  accessToken: string
  adminUserId: string
  phoneNumber: string
  email: string | null
  name: string
  role: AdminRole
  /**
   * 세션 만료 시각(ms). 만료되면 자동 로그아웃 처리됩니다.
   * - 서버 세션이 아닌 "프론트 세션" TTL 입니다.
   */
  expiresAt: number
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
    if (!v.expiresAt || typeof v.expiresAt !== 'number') {
      // 기존 세션(만료 정보 없음)은 보안상 재로그인을 유도합니다.
      clearSession()
      return null
    }
    if (Date.now() >= v.expiresAt) {
      clearSession()
      return null
    }
    return {
      tenantId: v.tenantId,
      tenantName: v.tenantName ?? undefined,
      accessToken: v.accessToken,
      adminUserId: v.adminUserId,
      phoneNumber: v.phoneNumber ?? '',
      email: v.email ?? null,
      name: v.name ?? '',
      role: (v.role as AdminRole) ?? 'OPERATOR',
      expiresAt: v.expiresAt,
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

/**
 * 사용자 활동 또는 API 요청 시 TTL을 30분으로 연장(슬라이딩)합니다.
 * 임박했을 때만 갱신하여 localStorage write를 줄입니다.
 */
export function touchSession(session: AdminSession): AdminSession {
  const now = Date.now()
  if (session.expiresAt - now > TOUCH_THRESHOLD_MS) return session
  const next: AdminSession = { ...session, expiresAt: now + SESSION_TTL_MS }
  setSession(next)
  return next
}

/** 세션 연장하기 버튼 등으로 수동 연장 시 항상 TTL을 30분으로 갱신합니다. */
export function extendSession(session: AdminSession): AdminSession {
  const next: AdminSession = { ...session, expiresAt: Date.now() + SESSION_TTL_MS }
  setSession(next)
  return next
}

/** 남은 세션 시간(ms). 만료 시 0 이하. */
export function getRemainingSessionMs(): number {
  const s = getSession()
  if (!s) return 0
  return Math.max(0, s.expiresAt - Date.now())
}

/** 남은 세션 시간을 "4:32" 형식으로 반환 */
export function formatRemainingSession(ms: number): string {
  if (ms <= 0) return '0:00'
  const totalSec = Math.ceil(ms / 1000)
  const m = Math.floor(totalSec / 60)
  const s = totalSec % 60
  return `${m}:${String(s).padStart(2, '0')}`
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

