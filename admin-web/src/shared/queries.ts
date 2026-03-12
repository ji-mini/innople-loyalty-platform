import { useQuery } from '@tanstack/react-query'
import { api } from './api'
import type { MemberDetail, MemberLedger, MemberSummary, PagedResponse } from './types'

export type CommonCodeItem = {
  id: string
  codeGroup: string
  code: string
  name: string
  active: boolean
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export function useCommonCodes(codeGroup: string) {
  return useQuery({
    queryKey: ['admin', 'common-codes', codeGroup],
    queryFn: async () => {
      const res = await api.get<CommonCodeItem[]>('/api/v1/admin/common-codes', {
        params: { codeGroup, active: true },
      })
      const items = res.data ?? []
      return [...items].sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0) || a.code.localeCompare(b.code))
    },
    enabled: !!codeGroup,
  })
}

export function useMemberList(params: {
  keyword?: string
  statusCode?: string
  memberNo?: string
  phoneNumber?: string
  name?: string
  webId?: string
  joinedFrom?: string
  joinedTo?: string
  page: number
  size: number
}) {
  return useQuery({
    queryKey: ['members', 'list', params],
    queryFn: async () => {
      const res = await api.get<PagedResponse<MemberSummary>>('/api/v1/members', { params })
      return res.data
    },
  })
}

export function useMemberDetail(memberNo: string) {
  return useQuery({
    queryKey: ['members', 'detail', memberNo],
    queryFn: async () => {
      const res = await api.get<MemberDetail>(`/api/v1/members/${encodeURIComponent(memberNo)}`)
      return res.data
    },
    enabled: !!memberNo,
  })
}

export function useMemberLedgers(memberNo: string, limit = 50) {
  return useQuery({
    queryKey: ['members', 'ledgers', memberNo, limit],
    queryFn: async () => {
      const res = await api.get<MemberLedger[]>(`/api/v1/members/${encodeURIComponent(memberNo)}/ledgers`, {
        params: { limit },
      })
      return res.data
    },
    enabled: !!memberNo,
  })
}

