import { Card, Input, Select, Space, Table, Tag } from 'antd'
import React from 'react'
import { PageShell } from '../common/PageShell'

type Row = {
  id: string
  name: string
  loginId: string
  role: 'SUPER_ADMIN' | 'ADMIN' | 'OPERATOR'
  status: 'ACTIVE' | 'DISABLED'
}

const ROLE_OPTIONS = [
  { value: undefined, label: '전체' },
  { value: 'SUPER_ADMIN', label: 'SUPER ADMIN' },
  { value: 'ADMIN', label: 'ADMIN' },
  { value: 'OPERATOR', label: 'OPERATOR' },
]

export function TenantAdminsPage() {
  const [keyword, setKeyword] = React.useState('')
  const [role, setRole] = React.useState<Row['role'] | undefined>(undefined)

  // TODO: connect to backend tenant admin user APIs
  const rows = React.useMemo<Row[]>(() => [], [])

  return (
    <PageShell title="테넌트 관리자">
      <Card>
        <Space wrap>
          <Input
            placeholder="이름/아이디"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            allowClear
            style={{ width: 280 }}
          />
          <Select value={role} onChange={setRole} style={{ width: 200 }} options={ROLE_OPTIONS as any} />
        </Space>
      </Card>

      <Card>
        <Table<Row>
          rowKey={(r) => r.id}
          dataSource={rows}
          pagination={false}
          columns={[
            { title: '이름', dataIndex: 'name', width: 180 },
            { title: '아이디', dataIndex: 'loginId', width: 220 },
            { title: '권한', dataIndex: 'role', width: 180 },
            {
              title: '상태',
              dataIndex: 'status',
              width: 140,
              render: (v: Row['status']) => <Tag color={v === 'ACTIVE' ? 'green' : 'default'}>{v}</Tag>,
            },
          ]}
          locale={{ emptyText: '관리자 데이터가 없습니다.' }}
        />
      </Card>
    </PageShell>
  )
}

