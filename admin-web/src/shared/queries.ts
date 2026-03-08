import { useQuery } from '@tanstack/react-query'
import { api } from './api'
import type { MemberDetail, MemberLedger, MemberSummary, PagedResponse } from './types'

export function useMemberList(params: { keyword?: string; statusCode?: string; page: number; size: number }) {
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

