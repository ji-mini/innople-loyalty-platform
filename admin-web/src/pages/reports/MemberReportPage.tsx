import { Card, DatePicker, Space, Statistic, Typography } from 'antd'
import { PageShell } from '../common/PageShell'

const nf = new Intl.NumberFormat('ko-KR')

export function MemberReportPage() {
  // TODO: connect to backend report APIs
  const joined = 0
  const withdrawn = 0
  const dormant = 0

  return (
    <PageShell title="회원 리포트">
      <Card>
        <Space wrap>
          <Typography.Text>기간</Typography.Text>
          <DatePicker.RangePicker />
        </Space>
      </Card>

      <Card>
        <Space wrap size={32}>
          <Statistic title="가입" value={joined} formatter={(v) => nf.format(Number(v))} />
          <Statistic title="탈퇴" value={withdrawn} formatter={(v) => nf.format(Number(v))} />
          <Statistic title="휴면" value={dormant} formatter={(v) => nf.format(Number(v))} />
        </Space>
      </Card>

      <Card>
        <Typography.Text type="secondary">추후 차트/세부 지표를 추가할 수 있습니다.</Typography.Text>
      </Card>
    </PageShell>
  )
}

