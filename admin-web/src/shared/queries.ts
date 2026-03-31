import { useQuery } from '@tanstack/react-query'
import { api } from './api'
import type { MemberDetail, MemberLedger, MemberSummary, PagedResponse, PointLedgerItem } from './types'

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

export type PointPolicyItem = {
  id: string
  pointType: string
  name: string
  validityDays: number
  enabled: boolean
  description: string | null
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

export function usePointPolicies() {
  return useQuery({
    queryKey: ['admin', 'point-policies'],
    queryFn: async () => {
      const res = await api.get<PointPolicyItem[]>('/api/v1/admin/point-policies')
      return res.data ?? []
    },
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

export type MemberGradeItem = {
  id: string
  code: string
  name: string
  description: string | null
  /** 적립 대상 금액 대비 적립률(%) — POS 연동 적립 시 사용 */
  earnRatePercent: number
}

export type DashboardSummary = {
  thisMonthNewMembers: number
  prevMonthNewMembers: number
  avgNewMembers: number
  thisMonthEarn: number
  prevMonthEarn: number
  avgEarn: number
  thisMonthUse: number
  prevMonthUse: number
  avgUse: number
  targetNewMembers: number
  targetEarn: number
  targetUse: number
  totalMembers: number
  totalPointBalance: number
}

export type DashboardGoal = {
  targetYear: number
  targetMonth: number
  targetNewMembers: number
  targetEarn: number
  targetUse: number
}

export type RecentPointActivity = {
  id: string
  createdAt: string
  memberNo: string
  brand: string
  type: 'EARN' | 'USE' | 'EXPIRE'
  amount: number
  reason: string
}

export type RecentAdminAction = {
  id: string
  createdAt: string
  adminName: string
  action: string
  target: string
}

export type TodayStatus = {
  todayEarn: number
  todayUse: number
  todayNewMembers: number
}

/** 잔여가 남은 롯 중 만료일이 7일 이내인 포인트 합계·회원 수 */
export type ExpiringPointsSummary = {
  pointsExpiringWithin7Days: number
  membersWithExpiringLots: number
}

export type DashboardResponse = {
  summary: DashboardSummary
  todayStatus: TodayStatus
  expiringPoints: ExpiringPointsSummary
  recentPoints: RecentPointActivity[]
  recentAdmins: RecentAdminAction[]
}

export function useDashboard() {
  return useQuery({
    queryKey: ['dashboard'],
    queryFn: async () => {
      const res = await api.get<DashboardResponse>('/api/v1/dashboard')
      return res.data
    },
  })
}

export function useMemberGrades() {
  return useQuery({
    queryKey: ['member-grades'],
    queryFn: async () => {
      const res = await api.get<MemberGradeItem[]>('/api/v1/member-grades')
      return res.data ?? []
    },
  })
}

export type MemberReportResponse = {
  fromDate: string
  toDate: string
  totalAsOfDate: string
  newSignups: number
  dormant: number
  withdrawRequested: number
  withdrawn: number
  totalMembers: number
}

export function useMemberReport(params: { fromDate: string; toDate: string; totalAsOfDate: string }) {
  return useQuery({
    queryKey: ['reports', 'members', params.fromDate, params.toDate, params.totalAsOfDate],
    queryFn: async () => {
      const res = await api.get<MemberReportResponse>('/api/v1/reports/members', {
        params: { fromDate: params.fromDate, toDate: params.toDate, totalAsOfDate: params.totalAsOfDate },
      })
      return res.data
    },
    enabled: !!params.fromDate && !!params.toDate && !!params.totalAsOfDate,
  })
}

export type MonthlyTotalItem = { month: number; totalMembers: number }

export type MonthlyTotalsResponse = { year: number; items: MonthlyTotalItem[] }

export function useMemberReportMonthlyTotals(year: number) {
  return useQuery({
    queryKey: ['reports', 'members', 'monthly-totals', year],
    queryFn: async () => {
      const res = await api.get<MonthlyTotalsResponse>('/api/v1/reports/members/monthly-totals', {
        params: { year },
      })
      return res.data
    },
    enabled: !!year,
  })
}

export function usePointLedgers(params: { memberNo?: string; limit?: number; enabled?: boolean }) {
  const { memberNo, limit = 100, enabled = true } = params
  return useQuery({
    queryKey: ['points', 'ledgers', memberNo ?? '', limit],
    queryFn: async () => {
      const res = await api.get<PointLedgerItem[]>('/api/v1/points/ledgers', {
        params: { memberNo: memberNo || undefined, limit },
      })
      return res.data ?? []
    },
    enabled,
  })
}

