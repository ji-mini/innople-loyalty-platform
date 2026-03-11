import { Card, Input, Space, Table, Tag, Typography } from 'antd'
import React from 'react'
import { PageShell } from '../common/PageShell'

type Row = {
  tenantId: string
  companyName: string
  domain: string
  status: 'active' | 'inactive'
  createdAt: string
}

export function TenantsPage() {
  const [keyword, setKeyword] = React.useState('')

  // TODO: connect to backend tenant admin APIs
  const rows = React.useMemo<Row[]>(() => [], [])

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
          pagination={false}
          columns={[
            { title: '테넌트ID', dataIndex: 'tenantId', width: 260 },
            { title: '회사명', dataIndex: 'companyName', width: 220 },
            { title: '도메인', dataIndex: 'domain' },
            {
              title: '상태',
              dataIndex: 'status',
              width: 120,
              render: (v: Row['status']) => <Tag color={v === 'active' ? 'green' : 'default'}>{v}</Tag>,
            },
            { title: '생성일', dataIndex: 'createdAt', width: 170 },
          ]}
          locale={{ emptyText: '테넌트 데이터가 없습니다.' }}
        />
      </Card>
    </PageShell>
  )
}

