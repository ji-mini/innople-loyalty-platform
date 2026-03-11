import { Card, DatePicker, Input, Select, Space, Table, Tag } from 'antd'
import React from 'react'
import { PageShell } from '../common/PageShell'

type Row = {
  id: string
  createdAt: string
  adminName: string
  category: 'ADMIN_ACTION' | 'API' | 'POINT'
  message: string
}

const CATEGORY_OPTIONS = [
  { value: undefined, label: '전체' },
  { value: 'ADMIN_ACTION', label: '관리자 활동' },
  { value: 'API', label: 'API 호출' },
  { value: 'POINT', label: '포인트 변경' },
]

export function LogsPage() {
  const [keyword, setKeyword] = React.useState('')
  const [category, setCategory] = React.useState<Row['category'] | undefined>(undefined)

  // TODO: connect to backend log APIs
  const rows = React.useMemo<Row[]>(() => [], [])

  return (
    <PageShell title="로그조회">
      <Card>
        <Space wrap>
          <Input
            placeholder="관리자/대상/메시지"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            allowClear
            style={{ width: 280 }}
          />
          <Select value={category} onChange={setCategory} style={{ width: 180 }} options={CATEGORY_OPTIONS as any} />
          <DatePicker.RangePicker />
        </Space>
      </Card>

      <Card>
        <Table<Row>
          rowKey={(r) => r.id}
          dataSource={rows}
          pagination={false}
          columns={[
            { title: '일시', dataIndex: 'createdAt', width: 170 },
            { title: '관리자', dataIndex: 'adminName', width: 160 },
            {
              title: '구분',
              dataIndex: 'category',
              width: 140,
              render: (v: Row['category']) => {
                const color = v === 'ADMIN_ACTION' ? 'blue' : v === 'API' ? 'purple' : 'gold'
                return <Tag color={color}>{v}</Tag>
              },
            },
            { title: '내용', dataIndex: 'message' },
          ]}
          locale={{ emptyText: '로그 데이터가 없습니다.' }}
        />
      </Card>
    </PageShell>
  )
}

