import { Card, Input, Select, Space, Table, Tag } from 'antd'
import { useQuery } from '@tanstack/react-query'
import React from 'react'
import { PageShell } from '../common/PageShell'
import { api } from '../../shared/api'
import type { AdminRole } from '../../shared/types'

type Row = {
  id: string
  name: string
  phoneNumber: string
  email: string | null
  role: AdminRole
  createdAt: string
  updatedAt: string
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

  const q = useQuery({
    queryKey: ['admin', 'admin-users', keyword],
    queryFn: async () => {
      const res = await api.get('/api/v1/admin/admin-users', { params: keyword ? { keyword } : {} })
      return (res.data ?? []) as Row[]
    },
  })

  const rows = React.useMemo<Row[]>(() => {
    const items = q.data ?? []
    if (!role) return items
    return items.filter((r) => r.role === role)
  }, [q.data, role])

  return (
    <PageShell title="테넌트 관리자">
      <Card>
        <Space wrap>
          <Input
            placeholder="이름/휴대폰/이메일"
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
          loading={q.isLoading}
          pagination={{ pageSize: 20 }}
          columns={[
            { title: '이름', dataIndex: 'name', width: 180 },
            { title: '휴대폰', dataIndex: 'phoneNumber', width: 180 },
            { title: '이메일', dataIndex: 'email', render: (v: string | null) => v ?? '-' },
            { title: '권한', dataIndex: 'role', width: 180, render: (v: Row['role']) => <Tag>{v}</Tag> },
            {
              title: '수정일시',
              dataIndex: 'updatedAt',
              width: 190,
            },
          ]}
          locale={{ emptyText: q.isError ? '관리자 목록 조회에 실패했습니다.' : '관리자 데이터가 없습니다.' }}
        />
      </Card>
    </PageShell>
  )
}

