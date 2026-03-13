import { api } from './api'
import type { TenantPublicItem, TenantPublicListResponse } from './types'

export async function listPublicTenants(): Promise<TenantPublicListResponse> {
  const res = await api.get<TenantPublicListResponse>('/api/v1/public/tenants')
  return res.data
}

export async function getTenantById(tenantId: string): Promise<TenantPublicItem | null> {
  try {
    const res = await api.get<TenantPublicItem>(`/api/v1/public/tenants/${encodeURIComponent(tenantId)}`)
    return res.data
  } catch {
    return null
  }
}

