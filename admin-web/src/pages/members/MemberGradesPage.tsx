import { Card, Form, Input, Table, Typography } from 'antd'
import React from 'react'
import { PageShell } from '../common/PageShell'
import { useMemberGrades } from '../../shared/queries'

export function MemberGradesPage() {
  const [keyword, setKeyword] = React.useState('')
  const { data: grades = [], isLoading } = useMemberGrades()

  const rows = React.useMemo(() => {
    if (!keyword.trim()) return grades
    const k = keyword.toLowerCase().trim()
    return grades.filter(
      (g) =>
        g.code.toLowerCase().includes(k) ||
        (g.name?.toLowerCase().includes(k) ?? false) ||
        (g.description?.toLowerCase().includes(k) ?? false)
    )
  }, [grades, keyword])

  return (
    <PageShell
      title="회원등급관리"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          회원 등급별 혜택 정책을 관리합니다.
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
        <Table
          rowKey={(r) => r.id}
          dataSource={rows}
          loading={isLoading}
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

