import { useQuery } from '@tanstack/react-query'
import { api } from './api'

/** 3단 추이 차트 기간 토글. 백엔드 DashboardPeriod 코드와 1:1 대응. */
export const DASHBOARD_PERIODS = [
  { value: '7D', label: '7일' },
  { value: '30D', label: '30일' },
  { value: '3M', label: '3개월' },
] as const

export type DashboardPeriod = (typeof DASHBOARD_PERIODS)[number]['value']

/**
 * current / previous / delta(=current-previous).
 * 비교 구간은 지표마다 다름(전월 동기 또는 전일). earned/used 의 previous 는 미사용(0).
 */
export type MetricValue = {
  current: number
  previous: number
  delta: number
}

export type TodayKpi = {
  /** 이번 달 신규 가입 vs 전월 동기 */
  newMembers: MetricValue
  /** 이번 달 최종 탈회(withdrawnAt) vs 전월 동기 */
  withdrawals: MetricValue
  /** 누적 활성 회원 vs 전일 */
  activeMembers: MetricValue
  /** 이번 달 적립 (증감 미표시) */
  earnedPoints: MetricValue
  /** 이번 달 사용 (증감 미표시) */
  usedPoints: MetricValue
}

export type PointLiability = {
  /** 기간 제한 없는 누적 적립(EARN, ADJUST_EARN) 합계. */
  totalEarned: number
  /** 기간 제한 없는 누적 사용(USE, ADJUST_USE) 합계. 만료/소각 제외. */
  totalUsed: number
  outstandingBalance: number
  /** 이번 달 말(KST) 이전에 만료되는 lot 잔여분 합계. */
  expiringThisMonth: number
}

export type DistributionItem = {
  key: string
  label: string
  count: number
}

export type BatchExecutionStatus = 'RUNNING' | 'SUCCESS' | 'PARTIAL' | 'FAILED'

export type BatchStatusItem = {
  batchName: string
  enabled: boolean
  /** 실행 이력이 없으면 null. */
  status: BatchExecutionStatus | null
  startedAt: string | null
  finishedAt: string | null
  processedCount: number
  errorCount: number
  errorMessage: string | null
}

export type DashboardOverview = {
  baseDate: string
  today: TodayKpi
  pointLiability: PointLiability
  gradeDistribution: DistributionItem[]
  statusDistribution: DistributionItem[]
  batches: BatchStatusItem[]
}

export type TrendPoint = {
  date: string
  signups: number
  withdrawals: number
  earnedPoints: number
  usedPoints: number
}

export type DashboardTrends = {
  period: DashboardPeriod
  fromDate: string
  toDate: string
  points: TrendPoint[]
}

export function useDashboardOverview() {
  return useQuery({
    queryKey: ['admin', 'dashboard', 'overview'],
    queryFn: async () => {
      const res = await api.get<DashboardOverview>('/api/v1/admin/dashboard/overview')
      return res.data
    },
  })
}

export function useDashboardTrends(period: DashboardPeriod) {
  return useQuery({
    queryKey: ['admin', 'dashboard', 'trends', period],
    queryFn: async () => {
      const res = await api.get<DashboardTrends>('/api/v1/admin/dashboard/trends', {
        params: { period },
      })
      return res.data
    },
  })
}
