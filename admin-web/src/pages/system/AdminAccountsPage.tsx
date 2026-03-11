import { Button, Card, Input, Space, Table, Tag, Typography, message } from 'antd'
import React from 'react'
import { PageShell } from '../common/PageShell'

type Row = {
  id: string
  name: string
  phoneNumber: string
  email: string | null
  status: 'ACTIVE' | 'DISABLED'
}

export function AdminAccountsPage() {
  const [keyword, setKeyword] = React.useState('')
  const rows = React.useMemo<Row[]>(() => [], [])

  return (
    <PageShell
      title="관리자 계정"
      extra={
        <Button
          type="primary"
          onClick={() => {
            message.info('관리자 생성 기능은 준비 중입니다.')
          }}
        >
          관리자 생성
        </Button>
      }
    >
      <Card>
        <Space wrap>
          <Input
            placeholder="이름/휴대폰/이메일"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            allowClear
            style={{ width: 320 }}
          />
        </Space>
      </Card>

      <Card>
        <Table<Row>
          rowKey={(r) => r.id}
          dataSource={rows}
          pagination={false}
          columns={[
            { title: '이름', dataIndex: 'name', width: 180 },
            { title: '휴대폰', dataIndex: 'phoneNumber', width: 180 },
            { title: '이메일', dataIndex: 'email', render: (v: string | null) => v ?? '-' },
            {
              title: '상태',
              dataIndex: 'status',
              width: 140,
              render: (v: Row['status']) => <Tag color={v === 'ACTIVE' ? 'green' : 'default'}>{v}</Tag>,
            },
          ]}
          locale={{
            emptyText: (
              <Space direction="vertical" size={6}>
                <Typography.Text>관리자 계정이 없습니다.</Typography.Text>
                <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                  우측 상단의 “관리자 생성”으로 추가할 수 있습니다.
                </Typography.Text>
              </Space>
            ),
          }}
        />
      </Card>
    </PageShell>
  )
}

