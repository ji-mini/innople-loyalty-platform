import { Card, Col, DatePicker, Row, Space, Typography } from 'antd'
import dayjs from 'dayjs'
import React from 'react'
import { useMemberReport } from '../../shared/queries'
import { PageShell } from '../common/PageShell'

const nf = new Intl.NumberFormat('ko-KR')

export function MemberReportPage() {
  const [date, setDate] = React.useState(() => dayjs())

  const dateStr = date?.format('YYYY-MM-DD') ?? ''

  const report = useMemberReport({ date: dateStr })

  const data = report.data
  const loading = report.isLoading

  const dailyCards = [
    { title: '신규가입', value: data?.newSignups ?? 0, key: 'new' },
    { title: '휴면', value: data?.dormant ?? 0, key: 'dormant' },
    { title: '탈퇴요청', value: data?.withdrawRequested ?? 0, key: 'withdrawReq' },
    { title: '탈퇴', value: data?.withdrawn ?? 0, key: 'withdrawn' },
  ]

  return (
    <PageShell title="회원 리포트">
      <Card>
        <Space wrap align="center">
          <Typography.Text>날짜</Typography.Text>
          <DatePicker value={date} onChange={(v) => setDate(v ?? dayjs())} allowClear={false} />
        </Space>
      </Card>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        {dailyCards.map((c) => (
          <Col xs={24} sm={12} md={6} key={c.key}>
            <Card loading={loading}>
              <Typography.Text type="secondary" style={{ fontSize: 13, display: 'block', marginBottom: 4 }}>
                {c.title}
              </Typography.Text>
              <Typography.Title level={3} style={{ margin: 0 }}>
                {nf.format(c.value)}
              </Typography.Title>
              <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                계정
              </Typography.Text>
            </Card>
          </Col>
        ))}
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} sm={12} md={8} lg={6}>
          <Card loading={loading}>
            <Typography.Text type="secondary" style={{ fontSize: 13, display: 'block', marginBottom: 4 }}>
              총 가입자수
            </Typography.Text>
            <Typography.Title level={3} style={{ margin: 0 }}>
              {nf.format(data?.totalSignups ?? 0)}
            </Typography.Title>
            <Typography.Text type="secondary" style={{ fontSize: 12 }}>
              계정 (선택일 기준 누적)
            </Typography.Text>
          </Card>
        </Col>
      </Row>
    </PageShell>
  )
}
