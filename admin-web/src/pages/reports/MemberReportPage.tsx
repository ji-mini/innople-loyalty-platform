import { Card, Col, DatePicker, Row, Space, Typography } from 'antd'
import dayjs from 'dayjs'
import React from 'react'
import { BarChart, Bar, Cell, PieChart, Pie, ResponsiveContainer, XAxis, YAxis } from 'recharts'
import { useMemberReport, useMemberReportMonthlyTotals } from '../../shared/queries'
import { PageShell } from '../common/PageShell'

const nf = new Intl.NumberFormat('ko-KR')

const DONUT_COLORS = ['#ff8b7a', '#8bd8c2', '#9be1ff', '#ffb4d2']

function thisMonthRange(): [dayjs.Dayjs, dayjs.Dayjs] {
  return [dayjs().startOf('month'), dayjs().endOf('month')]
}

export function MemberReportPage() {
  const [period, setPeriod] = React.useState<[dayjs.Dayjs, dayjs.Dayjs]>(thisMonthRange)
  const [chartYear, setChartYear] = React.useState(() => dayjs().year())

  const fromDate = period?.[0]?.format('YYYY-MM-DD') ?? ''
  const toDate = period?.[1]?.format('YYYY-MM-DD') ?? ''
  const totalAsOfDate = dayjs().format('YYYY-MM-DD')

  const report = useMemberReport({
    fromDate,
    toDate,
    totalAsOfDate,
  })
  const monthlyTotals = useMemberReportMonthlyTotals(chartYear)

  const data = report.data
  const loading = report.isLoading

  const periodCards = [
    { title: '신규가입', value: data?.newSignups ?? 0, key: 'new' },
    { title: '휴면', value: data?.dormant ?? 0, key: 'dormant' },
    { title: '탈퇴요청', value: data?.withdrawRequested ?? 0, key: 'withdrawReq' },
    { title: '탈퇴', value: data?.withdrawn ?? 0, key: 'withdrawn' },
  ]

  const donutData = periodCards.map((c, i) => ({
    name: c.title,
    value: c.value,
    fill: DONUT_COLORS[i],
  })).filter((d) => d.value > 0)

  const barData = React.useMemo(() => {
    const items = monthlyTotals.data?.items ?? []
    const monthNames = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월']
    return items.map((item, i) => ({
      month: monthNames[i],
      total: item.totalMembers,
    }))
  }, [monthlyTotals.data?.items])

  return (
    <PageShell title="회원 리포트">
      <Card>
        <Space wrap align="center">
          <Typography.Text>기간</Typography.Text>
          <DatePicker.RangePicker
            value={period as any}
            onChange={(v) => setPeriod((v as any) ?? thisMonthRange())}
            allowClear={false}
          />
        </Space>
      </Card>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }} align="stretch">
        <Col xs={24} md={14}>
          <Row gutter={[16, 16]}>
            {periodCards.map((c) => (
              <Col xs={24} sm={12} key={c.key}>
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
        </Col>
        <Col xs={24} md={10}>
          <Card loading={loading} style={{ height: '100%', minHeight: 280 }}>
            <Typography.Text type="secondary" style={{ fontSize: 13, display: 'block', marginBottom: 8 }}>
              기간별 현황
            </Typography.Text>
            {donutData.length > 0 ? (
              <ResponsiveContainer width="100%" height={240}>
                <PieChart>
                  <Pie
                    data={donutData}
                    dataKey="value"
                    nameKey="name"
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={90}
                    paddingAngle={2}
                    label={({ name, value }) => `${name} ${nf.format(value)}`}
                  >
                    {donutData.map((_, i) => (
                      <Cell key={i} fill={DONUT_COLORS[i]} />
                    ))}
                  </Pie>
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div style={{ height: 240, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <Typography.Text type="secondary">표시할 데이터가 없습니다.</Typography.Text>
              </div>
            )}
          </Card>
        </Col>
      </Row>

      <Card style={{ marginTop: 24 }}>
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} md={8} lg={6}>
            <Card loading={loading}>
              <Typography.Text type="secondary" style={{ fontSize: 13, display: 'block', marginBottom: 4 }}>
                총 회원수
              </Typography.Text>
              <Typography.Title level={3} style={{ margin: 0 }}>
                {nf.format(data?.totalMembers ?? 0)}
              </Typography.Title>
              <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                계정 (정상+휴면+탈퇴요청, 탈퇴 제외)
              </Typography.Text>
            </Card>
          </Col>
        </Row>
      </Card>

      <Card style={{ marginTop: 24 }}>
        <Space wrap align="center" style={{ marginBottom: 16 }}>
          <Typography.Text>연도</Typography.Text>
          <DatePicker
            picker="year"
            value={dayjs().year(chartYear)}
            onChange={(v) => setChartYear(v ? v.year() : dayjs().year())}
            allowClear={false}
          />
        </Space>
        <Typography.Text type="secondary" style={{ fontSize: 13, display: 'block', marginBottom: 16 }}>
          월별 총 회원수 (해당 월 말일 기준, 탈퇴 제외)
        </Typography.Text>
        <ResponsiveContainer width="100%" height={320}>
          <BarChart data={barData} margin={{ top: 16, right: 16, left: 16, bottom: 16 }}>
            <XAxis dataKey="month" tick={{ fontSize: 12 }} />
            <YAxis tick={{ fontSize: 12 }} tickFormatter={(v) => nf.format(v)} />
            <Bar dataKey="total" name="총 회원수" fill="#8bd8c2" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </Card>
    </PageShell>
  )
}
