import { Button, Card, Input, Select, Space, Table, Tag, Typography } from 'antd'
import React from 'react'
import { useMemberDetail, usePointLedgers } from '../../shared/queries'
import type { PointLedgerItem } from '../../shared/types'
import { PageShell } from '../common/PageShell'

const TYPE_OPTIONS = [
  { value: 'ALL', label: '전체' },
  { value: 'EARN', label: '적립' },
  { value: 'ADJUST_EARN', label: '적립 조정' },
  { value: 'USE', label: '사용' },
  { value: 'ADJUST_USE', label: '사용 조정' },
  { value: 'EXPIRE_AUTO', label: '자동 소멸' },
  { value: 'EXPIRE_MANUAL', label: '수동 소멸' },
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
  const [memberNo, setMemberNo] = React.useState('')
  const [typeFilter, setTypeFilter] = React.useState<string>('ALL')
  const [searched, setSearched] = React.useState(false)
  const [appliedMemberNo, setAppliedMemberNo] = React.useState('')
  const [appliedTypeFilter, setAppliedTypeFilter] = React.useState<string>('ALL')
  const trimmedMemberNo = memberNo.trim()
  const memberDetail = useMemberDetail(appliedMemberNo)

  const { data: allRows = [], isLoading } = usePointLedgers({
    memberNo: appliedMemberNo || undefined,
    limit: 200,
    enabled: searched,
  })

  const rows = React.useMemo(() => {
    if (appliedTypeFilter === 'ALL') return allRows
    return allRows.filter((r) => r.eventType === appliedTypeFilter)
  }, [allRows, appliedTypeFilter])

  const onSearch = () => {
    setAppliedMemberNo(trimmedMemberNo)
    setAppliedTypeFilter(typeFilter)
    setSearched(true)
  }

  const onReset = () => {
    setMemberNo('')
    setTypeFilter('ALL')
    setAppliedMemberNo('')
    setAppliedTypeFilter('ALL')
    setSearched(false)
  }

  return (
    <PageShell title="포인트 이력조회">
      <Card>
        <Space wrap>
          <Input
            placeholder="회원번호"
            value={memberNo}
            onChange={(e) => setMemberNo(e.target.value)}
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
              width: 110,
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
            { title: '포인트 유효기간', dataIndex: 'expiresAt', width: 180, render: (v: string | null) => formatDateTime(v) },
            { title: '승인번호', dataIndex: 'approvalNo', width: 130 },
            { title: '참조유형', dataIndex: 'referenceType', width: 140, render: (v: string | null) => v || '-' },
            { title: '참조ID', dataIndex: 'referenceId', width: 180, render: (v: string | null) => v || '-' },
            { title: '처리사유', dataIndex: 'reason', ellipsis: true },
            { title: '처리일시', dataIndex: 'createdAt', width: 180, render: (v: string) => formatDateTime(v) },
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

