import { Card, Col, Row, Space, Statistic, Table, Tag, Typography } from 'antd'
import dayjs from 'dayjs'
import { useDashboard } from '../../shared/queries'
import type { RecentAdminAction, RecentPointActivity } from '../../shared/queries'
import { PageShell } from '../common/PageShell'

const nf = new Intl.NumberFormat('ko-KR')

function formatInstant(iso: string): string {
  if (!iso) return '-'
  const parsed = dayjs(iso)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : iso
}

export function DashboardPage() {
  const dashboard = useDashboard()
  const summary = dashboard.data?.summary
  const thisMonthNewMembers = summary?.thisMonthNewMembers ?? 0
  const thisMonthEarn = summary?.thisMonthEarn ?? 0
  const thisMonthUse = summary?.thisMonthUse ?? 0
  const totalMembers = summary?.totalMembers ?? 0
  const totalPointBalance = summary?.totalPointBalance ?? 0

  const recentPoints = dashboard.data?.recentPoints ?? []
  const recentAdmins = dashboard.data?.recentAdmins ?? []

  return (
    <PageShell
      title="대시보드"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          운영 현황을 한눈에 확인합니다.
        </Typography.Text>
      }
    >
      <Card loading={dashboard.isLoading}>
        <Space wrap size={24}>
          <div>
            <Typography.Text type="secondary" style={{ display: 'block', fontSize: 12 }}>
              총 회원 수
            </Typography.Text>
            <Typography.Title level={4} style={{ margin: 0 }}>
              {nf.format(totalMembers)}
            </Typography.Title>
          </div>
          <div>
            <Typography.Text type="secondary" style={{ display: 'block', fontSize: 12 }}>
              이번 달 현황
            </Typography.Text>
            <Typography.Text>
              이번 달 적립 <b>{nf.format(thisMonthEarn)}</b> P / 이번 달 사용 <b>{nf.format(thisMonthUse)}</b> P / 이번 달 신규 회원{' '}
              <b>{nf.format(thisMonthNewMembers)}</b>
            </Typography.Text>
          </div>
        </Space>
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={dashboard.isLoading}>
            <Statistic title="이번 달 신규 회원" value={thisMonthNewMembers} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={dashboard.isLoading}>
            <Statistic title="이번 달 포인트 적립" value={thisMonthEarn} formatter={(v) => `${nf.format(Number(v))} P`} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={dashboard.isLoading}>
            <Statistic title="이번 달 포인트 사용" value={thisMonthUse} formatter={(v) => `${nf.format(Number(v))} P`} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={dashboard.isLoading}>
            <Statistic title="총 포인트 잔액" value={totalPointBalance} formatter={(v) => `${nf.format(Number(v))} P`} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="최근 포인트 활동" extra={<Typography.Text type="secondary">최근 20건</Typography.Text>} loading={dashboard.isLoading}>
            <Table<RecentPointActivity>
              size="small"
              rowKey={(r) => r.id}
              dataSource={recentPoints}
              pagination={false}
              columns={[
                { title: '일시', dataIndex: 'createdAt', width: 160, render: (v: string) => formatInstant(v) },
                { title: '회원번호', dataIndex: 'memberNo', width: 140 },
                { title: '브랜드', dataIndex: 'brand', width: 120 },
                {
                  title: '구분',
                  dataIndex: 'type',
                  width: 90,
                  render: (v: RecentPointActivity['type']) => {
                    const color = v === 'EARN' ? 'green' : v === 'USE' ? 'volcano' : 'default'
                    const label = v === 'EARN' ? '적립' : v === 'USE' ? '사용' : '소멸'
                    return <Tag color={color}>{label}</Tag>
                  },
                },
                { title: '포인트', dataIndex: 'amount', width: 120, render: (v: number) => `${nf.format(v)} P` },
                { title: '사유', dataIndex: 'reason' },
              ]}
              locale={{ emptyText: '최근 활동이 없습니다.' }}
            />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="최근 관리자 작업" extra={<Typography.Text type="secondary">최근 20건</Typography.Text>} loading={dashboard.isLoading}>
            <Table<RecentAdminAction>
              size="small"
              rowKey={(r) => r.id}
              dataSource={recentAdmins}
              pagination={false}
              columns={[
                { title: '일시', dataIndex: 'createdAt', width: 160, render: (v: string) => formatInstant(v) },
                { title: '관리자', dataIndex: 'adminName', width: 140 },
                { title: '작업', dataIndex: 'action', width: 180 },
                { title: '대상', dataIndex: 'target' },
              ]}
              locale={{ emptyText: '최근 작업이 없습니다.' }}
            />
          </Card>
        </Col>
      </Row>

    </PageShell>
  )
}

