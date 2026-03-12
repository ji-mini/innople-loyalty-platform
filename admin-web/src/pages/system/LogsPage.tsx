import { Button, Card, DatePicker, Input, Space, Table, Tabs, Tag, Typography } from 'antd'
import { useQuery } from '@tanstack/react-query'
import dayjs from 'dayjs'
import React from 'react'
import { PageShell } from '../common/PageShell'
import { api } from '../../shared/api'

type Category = 'ADMIN_USAGE' | 'POINT_API'

type Row = {
  id: string
  category: Category
  httpMethod: string
  path: string
  queryString: string | null
  statusCode: number
  durationMs: number
  adminUserId: string | null
  ip: string | null
  createdAt: string
}

type PagedResponse<T> = { items: T[]; page: number; size: number; totalElements: number; totalPages: number }

export function LogsPage() {
  const [keyword, setKeyword] = React.useState('')
  const [range, setRange] = React.useState<[any, any] | null>([dayjs().subtract(7, 'day'), dayjs()])
  const [activeTab, setActiveTab] = React.useState<Category>('ADMIN_USAGE')

  const q = useQuery({
    queryKey: ['admin', 'logs', activeTab, keyword, range?.[0]?.toISOString?.(), range?.[1]?.toISOString?.()],
    queryFn: async () => {
      const params: any = { category: activeTab, page: 0, size: 50 }
      if (keyword.trim()) params.keyword = keyword.trim()
      const fromAt = range?.[0]?.toISOString?.()
      const toAt = range?.[1]?.toISOString?.()
      if (fromAt) params.fromAt = fromAt
      if (toAt) params.toAt = toAt
      const res = await api.get('/api/v1/admin/logs', { params })
      return res.data as PagedResponse<Row>
    },
  })

  const rows = q.data?.items ?? []

  const cols = [
    { title: '일시', dataIndex: 'createdAt', width: 190 },
    {
      title: '구분',
      dataIndex: 'category',
      width: 140,
      render: (v: Category) => <Tag color={v === 'ADMIN_USAGE' ? 'blue' : 'gold'}>{v}</Tag>,
    },
    { title: '관리자ID', dataIndex: 'adminUserId', width: 260, render: (v: string | null) => v ?? '-' },
    { title: 'IP', dataIndex: 'ip', width: 140, render: (v: string | null) => v ?? '-' },
    { title: '메서드', dataIndex: 'httpMethod', width: 90 },
    { title: '상태', dataIndex: 'statusCode', width: 90, render: (v: number) => <Tag color={v >= 400 ? 'red' : 'green'}>{v}</Tag> },
    { title: '지연(ms)', dataIndex: 'durationMs', width: 110 },
    {
      title: '요청',
      render: (_: any, r: Row) => {
        const qs = r.queryString ? `?${r.queryString}` : ''
        return (
          <Typography.Text style={{ wordBreak: 'break-all' }}>
            {r.path}
            {qs}
          </Typography.Text>
        )
      },
    },
  ]

  return (
    <PageShell title="로그조회">
      <Card>
        <Space wrap>
          <Input
            placeholder="경로/키워드(User-Agent 포함)"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            allowClear
            style={{ width: 280 }}
          />
          <DatePicker.RangePicker value={range as any} onChange={(v) => setRange((v as any) ?? null)} />
          <Button onClick={() => q.refetch()} loading={q.isFetching}>
            새로고침
          </Button>
        </Space>
      </Card>

      <Card>
        <Tabs
          activeKey={activeTab}
          onChange={(k) => setActiveTab(k as Category)}
          items={[
            { key: 'ADMIN_USAGE', label: '어드민 사용', children: null },
            { key: 'POINT_API', label: '포인트 관련', children: null },
          ]}
        />
        <Table<Row>
          rowKey={(r) => r.id}
          dataSource={rows}
          loading={q.isLoading}
          pagination={{ pageSize: 50 }}
          columns={cols as any}
          locale={{ emptyText: q.isError ? '로그 조회에 실패했습니다.' : '로그 데이터가 없습니다.' }}
        />
      </Card>
    </PageShell>
  )
}

