import { Card, DatePicker, Input, Select, Space, Table, Tag, Typography } from 'antd'
import React from 'react'
import { PageShell } from '../common/PageShell'

type Row = {
  id: string
  memberNo: string
  brand: string
  type: 'EARN' | 'USE' | 'EXPIRE'
  amount: number
  reason: string
  createdAt: string
  adminName: string
}

const TYPE_OPTIONS = [
  { value: undefined, label: '전체' },
  { value: 'EARN', label: '적립' },
  { value: 'USE', label: '사용' },
  { value: 'EXPIRE', label: '소멸' },
]

export function PointHistoryPage() {
  const [memberNo, setMemberNo] = React.useState('')
  const [brand, setBrand] = React.useState<string | undefined>(undefined)
  const [type, setType] = React.useState<Row['type'] | undefined>(undefined)

  // TODO: connect to backend point ledger APIs
  const rows = React.useMemo<Row[]>(() => [], [])

  return (
    <PageShell title="포인트 이력조회">
      <Card>
        <Space wrap>
          <Input
            placeholder="회원번호"
            value={memberNo}
            onChange={(e) => setMemberNo(e.target.value)}
            allowClear
            style={{ width: 220 }}
          />
          <DatePicker.RangePicker />
          <Select
            placeholder="브랜드"
            value={brand}
            onChange={setBrand}
            allowClear
            style={{ width: 180 }}
            options={[
              { value: 'SPAO', label: 'SPAO' },
              { value: 'MIXXO', label: 'MIXXO' },
            ]}
          />
          <Select value={type} onChange={setType} style={{ width: 160 }} options={TYPE_OPTIONS as any} />
        </Space>
      </Card>

      <Card>
        <Table<Row>
          rowKey={(r) => r.id}
          dataSource={rows}
          pagination={false}
          columns={[
            { title: '회원번호', dataIndex: 'memberNo', width: 140 },
            { title: '브랜드', dataIndex: 'brand', width: 120 },
            {
              title: '구분',
              dataIndex: 'type',
              width: 100,
              render: (v: Row['type']) => {
                const color = v === 'EARN' ? 'green' : v === 'USE' ? 'volcano' : 'default'
                const label = v === 'EARN' ? '적립' : v === 'USE' ? '사용' : '소멸'
                return <Tag color={color}>{label}</Tag>
              },
            },
            { title: '포인트', dataIndex: 'amount', width: 140, render: (v: number) => `${v.toLocaleString('ko-KR')} P` },
            { title: '처리사유', dataIndex: 'reason' },
            { title: '처리일', dataIndex: 'createdAt', width: 170 },
            { title: '관리자', dataIndex: 'adminName', width: 140 },
          ]}
          locale={{
            emptyText: (
              <Space direction="vertical" size={6}>
                <Typography.Text>이력 데이터가 없습니다.</Typography.Text>
                <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                  조회 조건을 입력하면 결과가 표시됩니다.
                </Typography.Text>
              </Space>
            ),
          }}
        />
      </Card>
    </PageShell>
  )
}

