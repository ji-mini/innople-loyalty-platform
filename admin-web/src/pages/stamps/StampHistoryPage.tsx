import { Button, Card, Input, Space, Table, Tag, Typography } from 'antd'
import { useQuery } from '@tanstack/react-query'
import React from 'react'
import { api } from '../../shared/api'
import { PageShell } from '../common/PageShell'

export type StampLedgerItem = {
  id: string
  memberNo: string
  eventType: 'EARN_POS' | 'EARN_MANUAL' | 'REDEEM_COUPON' | 'ADJUST'
  stampDelta: number
  reason: string | null
  referenceType: string | null
  referenceId: string | null
  purchaseAmountWon: number | null
  createdAt: string
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

function eventLabel(t: string): string {
  switch (t) {
    case 'EARN_POS':
      return 'POS 적립'
    case 'EARN_MANUAL':
      return '수기 지급'
    case 'REDEEM_COUPON':
      return '쿠폰 전환'
    case 'ADJUST':
      return '조정'
    default:
      return t
  }
}

export function StampHistoryPage() {
  const [memberNo, setMemberNo] = React.useState('')
  const [filterKey, setFilterKey] = React.useState('')

  const q = useQuery({
    queryKey: ['admin', 'stamp-ledgers', filterKey],
    queryFn: async () => {
      const res = await api.get<StampLedgerItem[]>('/api/v1/admin/stamps/ledgers', {
        params: { memberNo: filterKey.trim() || undefined, limit: 200 },
      })
      return res.data ?? []
    },
  })

  const onSearch = () => {
    setFilterKey(memberNo.trim())
  }

  const rows = q.data ?? []

  return (
    <PageShell title="스탬프 이력 조회">
      <Card style={{ marginBottom: 16 }}>
        <Space wrap>
          <Input
            placeholder="회원번호 (비우면 전체)"
            value={memberNo}
            onChange={(e) => setMemberNo(e.target.value)}
            style={{ width: 220 }}
            onPressEnter={onSearch}
          />
          <Button type="primary" onClick={onSearch} loading={q.isFetching}>
            조회
          </Button>
        </Space>
      </Card>

      <Card>
        <Table<StampLedgerItem>
          rowKey={(r) => r.id}
          dataSource={rows}
          loading={q.isLoading}
          pagination={{ pageSize: 50, showSizeChanger: true, showTotal: (t) => `총 ${t}건` }}
          columns={[
            { title: '회원번호', dataIndex: 'memberNo', width: 140 },
            {
              title: '구분',
              dataIndex: 'eventType',
              width: 110,
              render: (v: string) => {
                const color =
                  v === 'EARN_POS' || v === 'EARN_MANUAL' ? 'green' : v === 'REDEEM_COUPON' ? 'volcano' : 'default'
                return <Tag color={color}>{eventLabel(v)}</Tag>
              },
            },
            {
              title: '스탬프',
              dataIndex: 'stampDelta',
              width: 100,
              render: (v: number) => `${v >= 0 ? '+' : ''}${v.toLocaleString('ko-KR')}`,
            },
            {
              title: '구매금액(원)',
              dataIndex: 'purchaseAmountWon',
              width: 120,
              render: (v: number | null) => (v == null ? '-' : v.toLocaleString('ko-KR')),
            },
            { title: '참조', dataIndex: 'referenceType', width: 120, render: (v: string | null) => v || '-' },
            { title: '참조ID', dataIndex: 'referenceId', width: 140, ellipsis: true, render: (v: string | null) => v || '-' },
            { title: '사유', dataIndex: 'reason', ellipsis: true },
            { title: '일시', dataIndex: 'createdAt', width: 180, render: (v: string) => formatDateTime(v) },
          ]}
          locale={{
            emptyText: (
              <Space direction="vertical" size={6}>
                <Typography.Text>이력이 없습니다.</Typography.Text>
              </Space>
            ),
          }}
        />
      </Card>
    </PageShell>
  )
}
