import { Card, DatePicker, Input, Space, Table, Typography } from 'antd'
import React from 'react'
import { PageShell } from '../common/PageShell'

type Row = {
  id: string
  couponName: string
  memberNo: string
  status: 'ISSUED' | 'USED' | 'EXPIRED'
  issuedAt: string
  usedAt: string | null
}

export function CouponHistoryPage() {
  const [keyword, setKeyword] = React.useState('')
  const rows = React.useMemo<Row[]>(() => [], [])

  return (
    <PageShell title="쿠폰 이력">
      <Card>
        <Space wrap>
          <Input
            placeholder="쿠폰명/회원번호"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            allowClear
            style={{ width: 280 }}
          />
          <DatePicker.RangePicker />
        </Space>
      </Card>

      <Card>
        <Table<Row>
          rowKey={(r) => r.id}
          dataSource={rows}
          pagination={false}
          columns={[
            { title: '쿠폰명', dataIndex: 'couponName' },
            { title: '회원번호', dataIndex: 'memberNo', width: 160 },
            { title: '상태', dataIndex: 'status', width: 120 },
            { title: '발급일', dataIndex: 'issuedAt', width: 170 },
            { title: '사용일', dataIndex: 'usedAt', width: 170, render: (v: string | null) => v ?? '-' },
          ]}
          locale={{
            emptyText: (
              <Space direction="vertical" size={6}>
                <Typography.Text>쿠폰 이력이 없습니다.</Typography.Text>
                <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                  추후 발급/사용 이력을 조회할 수 있습니다.
                </Typography.Text>
              </Space>
            ),
          }}
        />
      </Card>
    </PageShell>
  )
}

