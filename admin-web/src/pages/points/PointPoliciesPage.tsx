import { Card, Space, Switch, Table, Typography } from 'antd'
import React from 'react'
import { PageShell } from '../common/PageShell'

type PointPolicyRow = {
  id: string
  brand: string
  earnRatePercent: number
  enabled: boolean
  minSpendAmount: number | null
  validityDays: number | null
}

export function PointPoliciesPage() {
  // TODO: connect to backend policy APIs
  const rows = React.useMemo<PointPolicyRow[]>(() => [], [])

  return (
    <PageShell
      title="포인트 정책관리"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          브랜드별 적립/사용 정책을 관리합니다.
        </Typography.Text>
      }
    >
      <Card>
        <Table<PointPolicyRow>
          rowKey={(r) => r.id}
          dataSource={rows}
          pagination={false}
          columns={[
            { title: '브랜드', dataIndex: 'brand', width: 220 },
            {
              title: '적립률',
              dataIndex: 'earnRatePercent',
              width: 180,
              render: (v: number) => <span>{v}%</span>,
            },
            {
              title: '사용가능',
              dataIndex: 'enabled',
              width: 140,
              render: (v: boolean) => <Switch checked={v} disabled />,
            },
            {
              title: '최소사용금액',
              dataIndex: 'minSpendAmount',
              width: 180,
              render: (v: number | null) => (v == null ? '-' : v.toLocaleString('ko-KR')),
            },
            {
              title: '유효기간(일)',
              dataIndex: 'validityDays',
              width: 160,
              render: (v: number | null) => (v == null ? '-' : v),
            },
            { title: '적립 제한', render: () => '-' },
          ]}
          locale={{
            emptyText: (
              <Space direction="vertical" size={6}>
                <Typography.Text>정책 데이터가 없습니다.</Typography.Text>
                <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                  예시: SPAO 1% / MIXXO 2%
                </Typography.Text>
              </Space>
            ),
          }}
        />
      </Card>
    </PageShell>
  )
}

