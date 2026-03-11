import type { AdminRole } from './types'

const ROLE_LEVEL: Record<AdminRole, number> = {
  OPERATOR: 10,
  ADMIN: 20,
  SUPER_ADMIN: 30,
}

export function atLeast(role: AdminRole, required: AdminRole): boolean {
  return ROLE_LEVEL[role] >= ROLE_LEVEL[required]
}

export function canAccessPath(role: AdminRole, pathname: string): boolean {
  if (pathname === '/' || pathname.startsWith('/dashboard')) return true

  // Modifying features: SUPER_ADMIN
  if (pathname.startsWith('/members/register')) return atLeast(role, 'SUPER_ADMIN')
  if (pathname.startsWith('/points/manual/earn')) return atLeast(role, 'SUPER_ADMIN')
  if (pathname.startsWith('/points/manual/deduct')) return atLeast(role, 'SUPER_ADMIN')

  // Read-only: OPERATOR
  if (pathname === '/members' || pathname.startsWith('/members/')) return true
  if (pathname.startsWith('/points/history')) return true
  if (pathname.startsWith('/coupons/history')) return true
  if (pathname.startsWith('/reports/points') || pathname.startsWith('/reports/members')) return true

  // Admin menus: ADMIN+
  if (pathname.startsWith('/member-grades')) return atLeast(role, 'ADMIN')
  if (pathname.startsWith('/points/policies')) return atLeast(role, 'ADMIN')
  if (pathname.startsWith('/points/expiry')) return atLeast(role, 'ADMIN')
  if (pathname === '/tenants' || pathname.startsWith('/tenants/')) return atLeast(role, 'ADMIN')
  if (pathname.startsWith('/system/')) return atLeast(role, 'ADMIN')

  // Modifying features: SUPER_ADMIN
  if (pathname.startsWith('/points/manual')) return atLeast(role, 'SUPER_ADMIN')
  if (pathname.startsWith('/coupons/issue')) return atLeast(role, 'SUPER_ADMIN')

  return false
}

