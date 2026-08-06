import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from './api'
import type { PagedResponse } from './types'

/** 현재 지원하는 배치 식별자. 향후 배치 추가 시 여기에 확장한다. */
export const AUTO_WITHDRAWAL = 'AUTO_WITHDRAWAL'
export const POINT_EXPIRATION = 'POINT_EXPIRATION'

export type BatchConfig = {
  id: string
  batchName: string
  enabled: boolean
  runHour: number
  /** 배치에 따라 미사용(null). AUTO_WITHDRAWAL 은 필수. */
  thresholdDays: number | null
  createdAt: string
  updatedAt: string
  updatedBy: string | null
  /** 가장 최근 실행의 started_at. 실행 이력이 없으면 null. */
  lastExecutedAt: string | null
}

export type BatchExecutionStatus = 'RUNNING' | 'SUCCESS' | 'PARTIAL' | 'FAILED'

export type BatchExecution = {
  id: string
  batchName: string
  status: BatchExecutionStatus
  startedAt: string
  finishedAt: string | null
  processedCount: number
  errorCount: number
  errorMessage: string | null
  createdAt: string
}

/**
 * 시스템이 지원하는 배치 종류의 단일 소스.
 * 배치를 새로 추가할 땐 여기에 `{ name, label }` 한 줄만 추가하면
 * "배치 추가" 드롭다운 선택지와 화면 라벨이 함께 확장된다.
 */
export type BatchType = { name: string; label: string }

export const BATCH_TYPES: BatchType[] = [
  { name: AUTO_WITHDRAWAL, label: '회원 자동탈회' },
  { name: POINT_EXPIRATION, label: '포인트 자동소멸' },
]

/**
 * 배치명 → 한글 라벨 매핑. 코드값(AUTO_WITHDRAWAL)을 화면에 그대로 노출하지 않는다.
 * BATCH_TYPES 에서 파생하므로 별도로 관리하는 지점이 아니다.
 */
export const BATCH_LABELS: Record<string, string> = Object.fromEntries(
  BATCH_TYPES.map((t) => [t.name, t.label])
)

export function batchLabel(batchName: string): string {
  return BATCH_LABELS[batchName] ?? batchName
}

/** 실행 상태 → 태그 색상/라벨 매핑. */
export const BATCH_STATUS_META: Record<BatchExecutionStatus, { color: string; label: string }> = {
  RUNNING: { color: 'blue', label: '실행중' },
  SUCCESS: { color: 'green', label: '성공' },
  PARTIAL: { color: 'orange', label: '부분성공' },
  FAILED: { color: 'red', label: '실패' },
}

export function batchStatusMeta(status: string): { color: string; label: string } {
  return BATCH_STATUS_META[status as BatchExecutionStatus] ?? { color: 'default', label: status }
}

const CONFIGS_KEY = ['admin', 'batches', 'configs'] as const
const configKey = (batchName: string) => ['admin', 'batches', 'config', batchName] as const
const EXECUTIONS_ROOT_KEY = ['admin', 'batches', 'executions'] as const

export function useBatchConfigs() {
  return useQuery({
    queryKey: CONFIGS_KEY,
    queryFn: async () => {
      const res = await api.get<BatchConfig[]>('/api/v1/admin/batches/configs')
      return res.data ?? []
    },
  })
}

export function useBatchConfig(batchName: string) {
  return useQuery({
    queryKey: configKey(batchName),
    queryFn: async () => {
      const res = await api.get<BatchConfig>(`/api/v1/admin/batches/configs/${encodeURIComponent(batchName)}`)
      return res.data
    },
    enabled: !!batchName,
  })
}

export type CreateBatchConfigInput = {
  batchName?: string
  enabled: boolean
  runHour: number
  thresholdDays?: number | null
}

export function useCreateBatchConfig() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: CreateBatchConfigInput) => {
      const res = await api.post<BatchConfig>('/api/v1/admin/batches/configs', input)
      return res.data
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: CONFIGS_KEY })
    },
  })
}

export type UpdateBatchConfigInput = {
  enabled: boolean
  runHour: number
  thresholdDays?: number | null
}

export function useUpdateBatchConfig(batchName: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (input: UpdateBatchConfigInput) => {
      const res = await api.put<BatchConfig>(
        `/api/v1/admin/batches/configs/${encodeURIComponent(batchName)}`,
        input
      )
      return res.data
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: CONFIGS_KEY })
      qc.invalidateQueries({ queryKey: configKey(batchName) })
    },
  })
}

export function useBatchExecutions(batchName: string | undefined, page: number, size: number) {
  return useQuery({
    queryKey: ['admin', 'batches', 'executions', batchName ?? '', page, size],
    queryFn: async () => {
      const res = await api.get<PagedResponse<BatchExecution>>('/api/v1/admin/batches/executions', {
        params: { batchName: batchName || undefined, page, size },
      })
      return res.data
    },
  })
}

export function useRunBatch(batchName: string) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async () => {
      const res = await api.post<BatchExecution>(
        `/api/v1/admin/batches/${encodeURIComponent(batchName)}/run`,
        {}
      )
      return res.data
    },
    onSuccess: () => {
      // 새 실행 이력이 즉시 반영되도록 이력/설정 쿼리를 무효화한다.
      qc.invalidateQueries({ queryKey: EXECUTIONS_ROOT_KEY })
      qc.invalidateQueries({ queryKey: CONFIGS_KEY })
      qc.invalidateQueries({ queryKey: configKey(batchName) })
    },
  })
}
