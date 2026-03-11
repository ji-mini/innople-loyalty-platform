import { Card, Input, Space, Table, Typography } from 'antd'
import { useQuery } from '@tanstack/react-query'
import React from 'react'
import { PageShell } from '../common/PageShell'
import { listPublicTenants } from '../../shared/tenants'

type Row = {
  tenantId: string
  name: string
}

export function TenantsPage() {
  const [keyword, setKeyword] = React.useState('')

  const q = useQuery({
    queryKey: ['public', 'tenants'],
    queryFn: listPublicTenants,
  })

  const rows = React.useMemo<Row[]>(() => {
    const k = keyword.trim().toLowerCase()
    const items = (q.data?.items ?? []).map((t) => ({ tenantId: t.tenantId, name: t.name }))
    if (!k) return items
    return items.filter((r) => r.tenantId.toLowerCase().includes(k) || r.name.toLowerCase().includes(k))
  }, [keyword, q.data?.items])

  return (
    <PageShell
      title="테넌트 목록"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          멀티테넌트 SaaS 핵심 관리 화면입니다.
        </Typography.Text>
      }
    >
      <Card>
        <Space wrap>
          <Input
            placeholder="테넌트ID/회사명/도메인"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            allowClear
            style={{ width: 320 }}
          />
        </Space>
      </Card>

      <Card>
        <Table<Row>
          rowKey={(r) => r.tenantId}
          dataSource={rows}
          loading={q.isLoading}
          pagination={{ pageSize: 20 }}
          columns={[
            { title: '테넌트ID', dataIndex: 'tenantId', width: 260 },
            { title: '테넌트명', dataIndex: 'name' },
          ]}
          locale={{ emptyText: q.isError ? '테넌트 목록 조회에 실패했습니다.' : '테넌트 데이터가 없습니다.' }}
        />
      </Card>
    </PageShell>
  )
}

