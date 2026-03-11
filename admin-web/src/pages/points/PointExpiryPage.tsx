import { Card, Space, Table, Typography } from 'antd'
import React from 'react'
import { PageShell } from '../common/PageShell'

type ExpiryRow = {
  id: string
  brand: string
  scheduledAt: string
  lots: number
  amount: number
}

export function PointExpiryPage() {
  // TODO: connect to backend expiry jobs / lots APIs
  const rows = React.useMemo<ExpiryRow[]>(() => [], [])

  return (
    <PageShell
      title="포인트 소멸관리"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          자동 소멸/수기 소멸 관리 화면은 준비 중입니다.
        </Typography.Text>
      }
    >
      <Card>
        <Table<ExpiryRow>
          rowKey={(r) => r.id}
          dataSource={rows}
          pagination={false}
          columns={[
            { title: '브랜드', dataIndex: 'brand', width: 220 },
            { title: '예정일', dataIndex: 'scheduledAt', width: 180 },
            { title: '대상 Lot 수', dataIndex: 'lots', width: 140 },
            { title: '소멸 포인트', dataIndex: 'amount', width: 160, render: (v: number) => `${v.toLocaleString('ko-KR')} P` },
          ]}
          locale={{
            emptyText: (
              <Space direction="vertical" size={6}>
                <Typography.Text>소멸 관리 데이터가 없습니다.</Typography.Text>
                <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                  추후 자동 소멸 스케줄/실행 결과를 표시합니다.
                </Typography.Text>
              </Space>
            ),
          }}
        />
      </Card>
    </PageShell>
  )
}

