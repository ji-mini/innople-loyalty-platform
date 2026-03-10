import { api } from './api'
import type { TenantPublicListResponse } from './types'

export async function listPublicTenants(): Promise<TenantPublicListResponse> {
  const res = await api.get<TenantPublicListResponse>('/api/v1/public/tenants')
  return res.data
}

