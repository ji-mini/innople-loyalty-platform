import { Card, Space, Table, Typography } from 'antd'
import React from 'react'
import { PageShell } from '../common/PageShell'

type Row = {
  id: string
  role: string
  description: string
}

export function PermissionsPage() {
  const rows = React.useMemo<Row[]>(
    () => [
      { id: 'SUPER_ADMIN', role: 'SUPER ADMIN', description: '전체 테넌트/시스템 관리' },
      { id: 'ADMIN', role: 'ADMIN', description: '테넌트 운영 관리' },
      { id: 'OPERATOR', role: 'OPERATOR', description: '조회/일부 운영 처리' },
    ],
    [],
  )

  return (
    <PageShell
      title="권한관리"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          권한 정책은 확정 후 API 연동 예정입니다.
        </Typography.Text>
      }
    >
      <Card>
        <Table<Row>
          rowKey={(r) => r.id}
          dataSource={rows}
          pagination={false}
          columns={[
            { title: '권한', dataIndex: 'role', width: 220 },
            { title: '설명', dataIndex: 'description' },
          ]}
        />
      </Card>

      <Card>
        <Space direction="vertical" size={6}>
          <Typography.Text strong>로그 설계 팁</Typography.Text>
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            관리자 활동 로그, API 호출 로그, 포인트 변경 로그를 권한 레벨에 따라 접근 제어하는 구성을 추천합니다.
          </Typography.Text>
        </Space>
      </Card>
    </PageShell>
  )
}

