import { Button, Card, Input, Select, Space, Table, Tag, Typography } from 'antd'
import React from 'react'
import { useSearchParams } from 'react-router-dom'
import { useMemberDetail, usePointLedgers } from '../../shared/queries'
import type { PointLedgerItem } from '../../shared/types'
import { PageShell } from '../common/PageShell'

const TYPE_OPTIONS: Array<{ value: 'ALL' | 'EARN' | 'USE' | 'EVENT'; label: string }> = [
  { value: 'ALL', label: '전체' },
  { value: 'EARN', label: '적립' },
  { value: 'USE', label: '사용' },
  { value: 'EVENT', label: '이벤트' },
]

function eventTypeLabel(eventType: string): string {
  return TYPE_OPTIONS.find((o) => o.value === eventType)?.label ?? eventType
}

function formatDateTime(value: string | null | undefined): string {
  if (!value) return '-'
  try {
    const d = new Date(value)
    const pad = (n: number) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  } catch {
    return value
  }
}

export function PointHistoryPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const initialMemberNo = searchParams.get('memberNo') ?? ''
  const initialTypeGroup = (searchParams.get('typeGroup') as 'ALL' | 'EARN' | 'USE' | 'EVENT' | null) ?? 'ALL'
  const [memberNo, setMemberNo] = React.useState('')
  const [typeFilter, setTypeFilter] = React.useState<'ALL' | 'EARN' | 'USE' | 'EVENT'>('ALL')
  const [searched, setSearched] = React.useState(false)
  const [appliedMemberNo, setAppliedMemberNo] = React.useState('')
  const [appliedTypeFilter, setAppliedTypeFilter] = React.useState<'ALL' | 'EARN' | 'USE' | 'EVENT'>('ALL')
  const trimmedMemberNo = memberNo.trim()
  const memberDetail = useMemberDetail(appliedMemberNo)

  const { data: allRows = [], isLoading } = usePointLedgers({
    memberNo: appliedMemberNo || undefined,
    limit: 200,
    enabled: searched,
  })

  const rows = React.useMemo(() => {
    if (appliedTypeFilter === 'ALL') return allRows
    return allRows.filter((r) => {
      if (appliedTypeFilter === 'EARN') return r.eventType === 'EARN' || r.eventType === 'ADJUST_EARN'
      if (appliedTypeFilter === 'USE') return r.eventType === 'USE' || r.eventType === 'ADJUST_USE'
      return r.eventType === 'EXPIRE_AUTO' || r.eventType === 'EXPIRE_MANUAL'
    })
  }, [allRows, appliedTypeFilter])

  React.useEffect(() => {
    if (!initialMemberNo && initialTypeGroup === 'ALL') return
    setMemberNo(initialMemberNo)
    setTypeFilter(initialTypeGroup)
    setAppliedMemberNo(initialMemberNo.trim())
    setAppliedTypeFilter(initialTypeGroup)
    setSearched(true)
  }, [initialMemberNo, initialTypeGroup])

  const onSearch = () => {
    setAppliedMemberNo(trimmedMemberNo)
    setAppliedTypeFilter(typeFilter)
    setSearched(true)
    setSearchParams(
      trimmedMemberNo || typeFilter !== 'ALL'
        ? { ...(trimmedMemberNo ? { memberNo: trimmedMemberNo } : {}), ...(typeFilter !== 'ALL' ? { typeGroup: typeFilter } : {}) }
        : {},
    )
  }

  const onReset = () => {
    setMemberNo('')
    setTypeFilter('ALL')
    setAppliedMemberNo('')
    setAppliedTypeFilter('ALL')
    setSearched(false)
    setSearchParams({})
  }

  return (
    <PageShell title="포인트 이력조회">
      <Card>
        <Space wrap>
          <Input
            placeholder="회원번호"
            value={memberNo}
            onChange={(e) => setMemberNo(e.target.value)}
            onPressEnter={onSearch}
            allowClear
            style={{ width: 240 }}
          />
          <Select
            value={typeFilter}
            onChange={setTypeFilter}
            style={{ width: 160 }}
            options={TYPE_OPTIONS}
          />
          <Button type="primary" onClick={onSearch}>
            조회
          </Button>
          <Button onClick={onReset}>초기화</Button>
        </Space>
        {appliedMemberNo ? (
          <div style={{ marginTop: 12 }}>
            <Typography.Text type="secondary">현재 포인트 잔액 </Typography.Text>
            <Typography.Text strong>
              {memberDetail.data?.pointBalance?.toLocaleString('ko-KR') ?? '0'} P
            </Typography.Text>
          </div>
        ) : null}
      </Card>

      <Card>
        <Table<PointLedgerItem>
          rowKey={(r) => r.id}
          dataSource={rows}
          loading={isLoading}
          pagination={{ pageSize: 50, showSizeChanger: true, showTotal: (t) => `총 ${t}건` }}
          columns={[
            { title: '회원번호', dataIndex: 'memberNo', width: 140 },
            {
              title: '구분',
              dataIndex: 'eventType',
              width: 130,
              render: (v: string) => {
                const color =
                  v === 'EARN' || v === 'ADJUST_EARN' ? 'green' : v === 'USE' || v === 'ADJUST_USE' ? 'volcano' : 'default'
                return <Tag color={color}>{eventTypeLabel(v)}</Tag>
              },
            },
            {
              title: '포인트',
              dataIndex: 'amount',
              width: 120,
              render: (v: number) => `${v >= 0 ? '+' : ''}${v.toLocaleString('ko-KR')} P`,
            },
            {
              title: '총 구매금액',
              dataIndex: 'totalPurchaseAmount',
              width: 120,
              render: (v: number | null) => (v == null ? '-' : `${v.toLocaleString('ko-KR')}원`),
            },
            {
              title: '할인금액',
              dataIndex: 'discountAmount',
              width: 110,
              render: (v: number | null) => (v == null ? '-' : `${v.toLocaleString('ko-KR')}원`),
            },
            {
              title: '적립 대상 금액',
              dataIndex: 'purchaseAmount',
              width: 120,
              render: (v: number | null) => (v == null ? '-' : `${v.toLocaleString('ko-KR')}원`),
            },
            { title: '포인트 유효기간', dataIndex: 'expiresAt', width: 180, render: (v: string | null) => formatDateTime(v) },
            { title: '승인번호', dataIndex: 'approvalNo', width: 170 },
            { title: '참조유형', dataIndex: 'referenceType', width: 140, render: (v: string | null) => v || '-' },
            { title: '처리일시', dataIndex: 'createdAt', width: 180, render: (v: string) => formatDateTime(v) },
            { title: '처리사유', dataIndex: 'reason', ellipsis: true },
          ]}
          locale={{
            emptyText: (
              <Space direction="vertical" size={6}>
                <Typography.Text>{searched ? '이력 데이터가 없습니다.' : '조회 버튼을 눌러 포인트 이력을 확인하세요.'}</Typography.Text>
                {!searched ? (
                  <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                    회원번호를 입력하면 해당 회원만, 비우면 전체 이력이 표시됩니다.
                  </Typography.Text>
                ) : null}
              </Space>
            ),
          }}
        />
      </Card>
    </PageShell>
  )
}

