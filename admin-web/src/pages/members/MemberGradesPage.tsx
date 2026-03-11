import { Card, Form, Input, Table, Typography } from 'antd'
import React from 'react'
import { PageShell } from '../common/PageShell'

type MemberGrade = {
  id: string
  code: string
  name: string
  description: string
}

export function MemberGradesPage() {
  const [keyword, setKeyword] = React.useState('')

  const rows = React.useMemo<MemberGrade[]>(() => [], [])

  return (
    <PageShell
      title="회원등급관리"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          등급 정책은 준비 중입니다.
        </Typography.Text>
      }
    >
      <Card>
        <Form layout="inline">
          <Form.Item label="검색">
            <Input
              placeholder="등급 코드/명"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              allowClear
              style={{ width: 260 }}
            />
          </Form.Item>
        </Form>
      </Card>

      <Card>
        <Table<MemberGrade>
          rowKey={(r) => r.id}
          dataSource={rows}
          pagination={false}
          columns={[
            { title: '코드', dataIndex: 'code', width: 160 },
            { title: '등급명', dataIndex: 'name', width: 200 },
            { title: '설명', dataIndex: 'description' },
          ]}
          locale={{ emptyText: '등급 데이터가 없습니다.' }}
        />
      </Card>
    </PageShell>
  )
}

