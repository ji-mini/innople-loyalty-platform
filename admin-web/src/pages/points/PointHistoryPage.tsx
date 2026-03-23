import { Card, Input, Select, Space, Table, Tag, Typography } from 'antd'
import React from 'react'
import { usePointLedgers } from '../../shared/queries'
import type { PointLedgerItem } from '../../shared/types'
import { PageShell } from '../common/PageShell'

const TYPE_OPTIONS = [
  { value: undefined, label: '전체' },
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

export function PointHistoryPage() {
  const [memberNo, setMemberNo] = React.useState('')
  const [typeFilter, setTypeFilter] = React.useState<string | undefined>(undefined)

  const { data: allRows = [], isLoading } = usePointLedgers({
    memberNo: memberNo.trim() || undefined,
    limit: 200,
  })

  const rows = React.useMemo(() => {
    if (!typeFilter) return allRows
    return allRows.filter((r) => r.eventType === typeFilter)
  }, [allRows, typeFilter])

  return (
    <PageShell title="포인트 이력조회">
      <Card>
        <Space wrap>
          <Input
            placeholder="회원번호 (미입력 시 전체)"
            value={memberNo}
            onChange={(e) => setMemberNo(e.target.value)}
            allowClear
            style={{ width: 240 }}
          />
          <Select
            placeholder="구분"
            value={typeFilter}
            onChange={setTypeFilter}
            allowClear
            style={{ width: 160 }}
            options={TYPE_OPTIONS}
          />
        </Space>
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
            { title: '처리사유', dataIndex: 'reason', ellipsis: true },
            { title: '처리일시', dataIndex: 'createdAt', width: 180 },
          ]}
          locale={{
            emptyText: (
              <Space direction="vertical" size={6}>
                <Typography.Text>이력 데이터가 없습니다.</Typography.Text>
                <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                  회원번호를 입력하면 해당 회원만, 비우면 전체 이력이 표시됩니다.
                </Typography.Text>
              </Space>
            ),
          }}
        />
      </Card>
    </PageShell>
  )
}

