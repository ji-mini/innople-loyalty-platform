import { Card, DatePicker, Space, Statistic, Typography } from 'antd'
import { PageShell } from '../common/PageShell'

const nf = new Intl.NumberFormat('ko-KR')

export function PointReportPage() {
  // TODO: connect to backend report APIs
  const earn = 0
  const use = 0
  const expire = 0
  const balance = 0

  return (
    <PageShell title="포인트 리포트">
      <Card>
        <Space wrap>
          <Typography.Text>기간</Typography.Text>
          <DatePicker.RangePicker />
        </Space>
      </Card>

      <Space direction="vertical" size={16} style={{ width: '100%' }}>
        <Card>
          <Space wrap size={32}>
            <Statistic title="적립 총액" value={earn} formatter={(v) => `${nf.format(Number(v))} P`} />
            <Statistic title="사용 총액" value={use} formatter={(v) => `${nf.format(Number(v))} P`} />
            <Statistic title="소멸" value={expire} formatter={(v) => `${nf.format(Number(v))} P`} />
            <Statistic title="잔액" value={balance} formatter={(v) => `${nf.format(Number(v))} P`} />
          </Space>
        </Card>

        <Card>
          <Typography.Text type="secondary">그래프는 추후 추가됩니다.</Typography.Text>
        </Card>
      </Space>
    </PageShell>
  )
}

