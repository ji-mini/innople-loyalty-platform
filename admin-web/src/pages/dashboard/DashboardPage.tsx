import { Alert, Button, Card, Col, Form, InputNumber, Modal, Row, Select, Space, Table, Tag, Typography, message } from 'antd'
import dayjs from 'dayjs'
import React from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../../shared/api'
import { atLeast } from '../../shared/roles'
import { getSession } from '../../shared/storage'
import { useDashboard } from '../../shared/queries'
import type { RecentAdminAction, RecentPointActivity, TodayStatus } from '../../shared/queries'
import { PageShell } from '../common/PageShell'

const nf = new Intl.NumberFormat('ko-KR')

function formatInstant(iso: string): string {
  if (!iso) return '-'
  const parsed = dayjs(iso)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : iso
}

type MetricKind = 'newMembers' | 'earn' | 'use'

type MetricInsight = {
  current: number
  previous: number
  unit: string
  deltaLabel: string
  tagColor: 'success' | 'processing' | 'error' | 'warning' | 'default'
  insight: string
  headline: string
}

type TargetInsight = {
  target: number
  achievementLabel: string
  tagColor: 'success' | 'processing' | 'error' | 'warning' | 'default'
  insight: string
}

type HealthInsight = {
  statusLabel: string
  tagColor: 'success' | 'processing' | 'error' | 'warning' | 'default'
  insight: string
}

type ActivityFilter = 'ALL' | 'EARN' | 'USE' | 'EVENT'

function formatMetricValue(value: number, unit = ''): string {
  return unit ? `${nf.format(value)} ${unit}` : nf.format(value)
}

function formatChangeRate(current: number, previous: number): string | null {
  if (previous === 0) return current === 0 ? '0%' : null
  const rate = Math.round(((current - previous) / previous) * 100)
  return `${rate > 0 ? '+' : ''}${rate}%`
}

function formatAchievementRate(current: number, target: number): number | null {
  if (target <= 0) return null
  return Math.round((current / target) * 100)
}

function buildTargetInsight(label: string, current: number, target: number, unit = ''): TargetInsight {
  const targetValue = formatMetricValue(target, unit)
  const currentValue = formatMetricValue(current, unit)
  const achievementRate = formatAchievementRate(current, target)

  if (target <= 0) {
    return {
      target,
      achievementLabel: '목표 미설정',
      tagColor: 'default',
      insight: `${label}의 이번 달 목표가 아직 설정되지 않았습니다.`,
    }
  }

  if ((achievementRate ?? 0) >= 100) {
    return {
      target,
      achievementLabel: `목표 달성 ${achievementRate}%`,
      tagColor: 'success',
      insight: `${label} 목표 ${targetValue}를 이미 달성했습니다. 현재 실적은 ${currentValue}입니다.`,
    }
  }

  if ((achievementRate ?? 0) >= 70) {
    return {
      target,
      achievementLabel: `목표 달성 ${achievementRate}%`,
      tagColor: 'processing',
      insight: `${label} 목표 ${targetValue} 대비 ${currentValue} 수준입니다. 현재 흐름이면 목표 근접이 가능합니다.`,
    }
  }

  if ((achievementRate ?? 0) > 0) {
    return {
      target,
      achievementLabel: `목표 달성 ${achievementRate}%`,
      tagColor: 'warning',
      insight: `${label} 목표 ${targetValue} 대비 아직 ${currentValue}입니다. 추가 점검이 필요합니다.`,
    }
  }

  return {
    target,
    achievementLabel: '목표 달성 0%',
    tagColor: 'error',
    insight: `${label} 목표 ${targetValue} 대비 아직 실적이 없습니다.`,
  }
}

function buildHealthInsight(label: string, current: number, average: number, unit = ''): HealthInsight {
  const currentValue = formatMetricValue(current, unit)
  const averageValue = formatMetricValue(average, unit)

  if (label === '포인트 사용' && current === 0) {
    return {
      statusLabel: '이상 징후',
      tagColor: 'warning',
      insight: `이번 달 ${label}이 ${currentValue}입니다. 최근 평균 ${averageValue} 대비 비정상적으로 낮습니다.`,
    }
  }

  if (average <= 0) {
    return {
      statusLabel: '기준 부족',
      tagColor: 'default',
      insight: `최근 평균 데이터가 충분하지 않아 ${label}의 이상 여부를 판단하기 어렵습니다.`,
    }
  }

  const ratio = current / average
  if (ratio < 0.6) {
    return {
      statusLabel: '평균 대비 낮음',
      tagColor: 'error',
      insight: `${label}이 최근 평균 ${averageValue} 대비 낮은 ${currentValue} 수준입니다. 원인 점검이 필요합니다.`,
    }
  }
  if (ratio <= 1.2) {
    return {
      statusLabel: '정상 범위',
      tagColor: 'success',
      insight: `${label}이 최근 평균 ${averageValue} 대비 정상 범위의 ${currentValue}입니다.`,
    }
  }
  return {
    statusLabel: '평균 대비 높음',
    tagColor: 'processing',
    insight: `${label}이 최근 평균 ${averageValue} 대비 높은 ${currentValue} 수준입니다.`,
  }
}

function buildTodayIssue(todayStatus: TodayStatus): string {
  if (todayStatus.todayUse === 0) return '사용 없음'
  if (todayStatus.todayNewMembers === 0) return '신규회원 유입 없음'
  if (todayStatus.todayEarn === 0) return '적립 발생 없음'
  return '특이사항 없음'
}

function buildMetricInsight(kind: MetricKind, current: number, previous: number, unit = ''): MetricInsight {
  const currentValue = formatMetricValue(current, unit)
  const previousValue = formatMetricValue(previous, unit)
  const changeRate = formatChangeRate(current, previous)

  if (kind === 'use' && current === 0) {
    return {
      current,
      previous,
      unit,
      deltaLabel: '비정상',
      tagColor: 'warning',
      insight: `이번 달 포인트 사용이 ${currentValue}입니다. 사용 전환이나 고객 활동 흐름 점검이 필요합니다.`,
      headline: `포인트 사용은 ${currentValue}로 비정상 징후입니다.`,
    }
  }

  if (previous === 0 && current > 0) {
    const message =
      kind === 'newMembers'
        ? '전월에는 신규 유입이 없었고 이번 달부터 유입이 다시 발생했습니다.'
        : kind === 'earn'
          ? '전월에는 적립이 없었고 이번 달 적립 활동이 새롭게 발생했습니다.'
          : '전월에는 사용이 없었고 이번 달 사용 활동이 다시 발생했습니다.'

    return {
      current,
      previous,
      unit,
      deltaLabel: '신규 발생',
      tagColor: kind === 'use' ? 'processing' : 'success',
      insight: message,
      headline: `${currentValue} 실적이 새롭게 발생했습니다.`,
    }
  }

  if (current === previous) {
    const sameMessage =
      kind === 'newMembers'
        ? '신규회원 유입 규모가 전월과 동일합니다.'
        : kind === 'earn'
          ? '포인트 적립 규모가 전월과 동일합니다.'
          : '포인트 사용 규모가 전월과 동일합니다.'

    return {
      current,
      previous,
      unit,
      deltaLabel: '변화 없음',
      tagColor: 'default',
      insight: sameMessage,
      headline: `전월과 동일한 ${currentValue} 수준입니다.`,
    }
  }

  const increased = current > previous
  if (kind === 'newMembers') {
    return {
      current,
      previous,
      unit,
      deltaLabel: `전월 대비 ${changeRate}`,
      tagColor: increased ? 'success' : 'error',
      insight: increased
        ? `신규회원 유입이 전월 ${previousValue}에서 ${currentValue}로 늘었습니다. 유입 흐름이 개선되고 있습니다.`
        : `신규회원 유입이 전월 ${previousValue}에서 ${currentValue}로 줄었습니다. 유입 채널 점검이 필요합니다.`,
      headline: `신규회원은 전월 대비 ${changeRate} ${increased ? '증가' : '감소'}했습니다.`,
    }
  }

  if (kind === 'earn') {
    return {
      current,
      previous,
      unit,
      deltaLabel: `전월 대비 ${changeRate}`,
      tagColor: increased ? 'success' : 'error',
      insight: increased
        ? `적립 규모가 전월 ${previousValue}에서 ${currentValue}로 확대됐습니다. 고객 활동이 늘어나는 흐름입니다.`
        : `적립 규모가 전월 ${previousValue}에서 ${currentValue}로 줄었습니다. 프로모션이나 방문 흐름 점검이 필요합니다.`,
      headline: `포인트 적립은 전월 대비 ${changeRate} ${increased ? '증가' : '감소'}했습니다.`,
    }
  }

  return {
    current,
    previous,
    unit,
    deltaLabel: `전월 대비 ${changeRate}`,
    tagColor: increased ? 'processing' : 'default',
    insight: increased
      ? `포인트 사용이 전월 ${previousValue}에서 ${currentValue}로 늘었습니다. 실사용 전환이 활발합니다.`
      : `포인트 사용이 전월 ${previousValue}에서 ${currentValue}로 줄었습니다. 사용 전환 흐름을 점검해보세요.`,
    headline: `포인트 사용은 전월 대비 ${changeRate} ${increased ? '증가' : '감소'}했습니다.`,
  }
}

export function DashboardPage() {
  const dashboard = useDashboard()
  const nav = useNavigate()
  const role = getSession()?.role ?? 'OPERATOR'
  const canEditGoal = atLeast(role, 'ADMIN')
  const canRunQuickAction = atLeast(role, 'SUPER_ADMIN')
  const [goalModalOpen, setGoalModalOpen] = React.useState(false)
  const [savingGoal, setSavingGoal] = React.useState(false)
  const [activityFilter, setActivityFilter] = React.useState<ActivityFilter>('ALL')
  const [goalForm] = Form.useForm<{ targetNewMembers: number; targetEarn: number; targetUse: number }>()
  const summary = dashboard.data?.summary
  const todayStatus = dashboard.data?.todayStatus ?? { todayEarn: 0, todayUse: 0, todayNewMembers: 0 }
  const thisMonthNewMembers = summary?.thisMonthNewMembers ?? 0
  const prevMonthNewMembers = summary?.prevMonthNewMembers ?? 0
  const avgNewMembers = summary?.avgNewMembers ?? 0
  const thisMonthEarn = summary?.thisMonthEarn ?? 0
  const prevMonthEarn = summary?.prevMonthEarn ?? 0
  const avgEarn = summary?.avgEarn ?? 0
  const thisMonthUse = summary?.thisMonthUse ?? 0
  const prevMonthUse = summary?.prevMonthUse ?? 0
  const avgUse = summary?.avgUse ?? 0
  const targetNewMembers = summary?.targetNewMembers ?? 0
  const targetEarn = summary?.targetEarn ?? 0
  const targetUse = summary?.targetUse ?? 0
  const totalMembers = summary?.totalMembers ?? 0
  const totalPointBalance = summary?.totalPointBalance ?? 0
  const expiring = dashboard.data?.expiringPoints ?? { pointsExpiringWithin7Days: 0, membersWithExpiringLots: 0 }

  const recentPoints = dashboard.data?.recentPoints ?? []
  const recentAdmins = dashboard.data?.recentAdmins ?? []
  const filteredRecentPoints = React.useMemo(() => {
    if (activityFilter === 'ALL') return recentPoints
    if (activityFilter === 'EARN') return recentPoints.filter((item) => item.type === 'EARN')
    if (activityFilter === 'USE') return recentPoints.filter((item) => item.type === 'USE')
    return recentPoints.filter((item) => item.type === 'EXPIRE')
  }, [activityFilter, recentPoints])
  const newMembersInsight = buildMetricInsight('newMembers', thisMonthNewMembers, prevMonthNewMembers)
  const earnInsight = buildMetricInsight('earn', thisMonthEarn, prevMonthEarn, 'P')
  const useInsight = buildMetricInsight('use', thisMonthUse, prevMonthUse, 'P')
  const newMembersHealth = buildHealthInsight('신규회원', thisMonthNewMembers, avgNewMembers)
  const earnHealth = buildHealthInsight('포인트 적립', thisMonthEarn, avgEarn, 'P')
  const useHealth = buildHealthInsight('포인트 사용', thisMonthUse, avgUse, 'P')
  const newMembersTarget = buildTargetInsight('신규회원', thisMonthNewMembers, targetNewMembers)
  const earnTarget = buildTargetInsight('포인트 적립', thisMonthEarn, targetEarn, 'P')
  const useTarget = buildTargetInsight('포인트 사용', thisMonthUse, targetUse, 'P')
  const summaryTone = useHealth.tagColor === 'warning' || newMembersHealth.tagColor === 'error' ? 'warning' : 'info'
  const todayIssue = buildTodayIssue(todayStatus)

  const openGoalModal = () => {
    goalForm.setFieldsValue({
      targetNewMembers,
      targetEarn,
      targetUse,
    })
    setGoalModalOpen(true)
  }

  const saveGoal = async () => {
    const values = await goalForm.validateFields()
    setSavingGoal(true)
    try {
      await api.put('/api/v1/dashboard/goals/current', values)
      message.success('이번 달 목표가 저장되었습니다.')
      setGoalModalOpen(false)
      await dashboard.refetch()
    } catch (e: any) {
      message.error(e?.response?.data?.message ?? e?.message ?? '목표 저장에 실패했습니다.')
    } finally {
      setSavingGoal(false)
    }
  }

  return (
    <PageShell
      title="대시보드"
      extra={
        <Space wrap size={12}>
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            이번 달 운영 현황을 평균/전월/목표와 비교해 보여줍니다.
          </Typography.Text>
          <Button size="small" type="primary" onClick={() => nav('/points/manual/earn')} disabled={!canRunQuickAction}>
            + 포인트 지급
          </Button>
          <Button size="small" onClick={() => nav('/points/manual/deduct')} disabled={!canRunQuickAction}>
            - 포인트 차감
          </Button>
          <Button size="small" onClick={() => nav('/members/register')} disabled={!canRunQuickAction}>
            회원 등록
          </Button>
          {canEditGoal ? (
            <Button size="small" onClick={openGoalModal}>
              이번 달 목표 설정
            </Button>
          ) : null}
        </Space>
      }
    >
      <Alert
        type={summaryTone}
        showIcon
        message="이번 달 핵심 인사이트"
        description={`${useHealth.insight} ${newMembersHealth.insight} ${earnHealth.insight} 신규회원 ${newMembersTarget.achievementLabel}, 적립 ${earnTarget.achievementLabel}, 사용 ${useTarget.achievementLabel}입니다.`}
      />

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={dashboard.isLoading}>
            <Typography.Text type="secondary">오늘 상태</Typography.Text>
            <Typography.Title level={4} style={{ margin: '8px 0 12px' }}>
              오늘 운영 요약
            </Typography.Title>
            <Space direction="vertical" size={6}>
              <Typography.Text>적립: {formatMetricValue(todayStatus.todayEarn, 'P')}</Typography.Text>
              <Typography.Text>
                사용: {formatMetricValue(todayStatus.todayUse, 'P')}
                {todayStatus.todayUse === 0 ? '  ⚠️' : ''}
              </Typography.Text>
              <Typography.Text>신규회원: {formatMetricValue(todayStatus.todayNewMembers)}명</Typography.Text>
              <Typography.Text type={todayIssue === '특이사항 없음' ? 'secondary' : 'warning'}>이슈: {todayIssue}</Typography.Text>
            </Space>
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={dashboard.isLoading}>
            <Typography.Text type="secondary">이번 달 신규 회원</Typography.Text>
            <Typography.Title level={3} style={{ margin: '8px 0 4px' }}>
              {formatMetricValue(thisMonthNewMembers)}
            </Typography.Title>
            <Space wrap size={8}>
              <Tag color={newMembersHealth.tagColor}>{newMembersHealth.statusLabel}</Tag>
              <Tag color={newMembersInsight.tagColor}>{newMembersInsight.deltaLabel}</Tag>
              <Tag color={newMembersTarget.tagColor}>{newMembersTarget.achievementLabel}</Tag>
              <Typography.Text type="secondary">최근 평균 {formatMetricValue(avgNewMembers)}</Typography.Text>
              <Typography.Text type="secondary">전월 {formatMetricValue(prevMonthNewMembers)}</Typography.Text>
              <Typography.Text type="secondary">목표 {formatMetricValue(targetNewMembers)}</Typography.Text>
            </Space>
            <Typography.Paragraph type="secondary" style={{ margin: '12px 0 0' }}>
              {newMembersHealth.insight} {newMembersInsight.insight} {newMembersTarget.insight}
            </Typography.Paragraph>
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={dashboard.isLoading}>
            <Typography.Text type="secondary">이번 달 포인트 적립</Typography.Text>
            <Typography.Title level={3} style={{ margin: '8px 0 4px' }}>
              {formatMetricValue(thisMonthEarn, 'P')}
            </Typography.Title>
            <Space wrap size={8}>
              <Tag color={earnHealth.tagColor}>{earnHealth.statusLabel}</Tag>
              <Tag color={earnInsight.tagColor}>{earnInsight.deltaLabel}</Tag>
              <Tag color={earnTarget.tagColor}>{earnTarget.achievementLabel}</Tag>
              <Typography.Text type="secondary">최근 평균 {formatMetricValue(avgEarn, 'P')}</Typography.Text>
              <Typography.Text type="secondary">전월 {formatMetricValue(prevMonthEarn, 'P')}</Typography.Text>
              <Typography.Text type="secondary">목표 {formatMetricValue(targetEarn, 'P')}</Typography.Text>
            </Space>
            <Typography.Paragraph type="secondary" style={{ margin: '12px 0 0' }}>
              {earnHealth.insight} {earnInsight.insight} {earnTarget.insight}
            </Typography.Paragraph>
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={dashboard.isLoading}>
            <Typography.Text type="secondary">이번 달 포인트 사용</Typography.Text>
            <Typography.Title level={3} style={{ margin: '8px 0 4px' }}>
              {formatMetricValue(thisMonthUse, 'P')}
            </Typography.Title>
            <Space wrap size={8}>
              <Tag color={useHealth.tagColor}>{useHealth.statusLabel}</Tag>
              <Tag color={useInsight.tagColor}>{useInsight.deltaLabel}</Tag>
              <Tag color={useTarget.tagColor}>{useTarget.achievementLabel}</Tag>
              <Typography.Text type="secondary">최근 평균 {formatMetricValue(avgUse, 'P')}</Typography.Text>
              <Typography.Text type="secondary">전월 {formatMetricValue(prevMonthUse, 'P')}</Typography.Text>
              <Typography.Text type="secondary">목표 {formatMetricValue(targetUse, 'P')}</Typography.Text>
            </Space>
            <Typography.Paragraph type="secondary" style={{ margin: '12px 0 0' }}>
              {useHealth.insight} {useInsight.insight} {useTarget.insight}
            </Typography.Paragraph>
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <Card loading={dashboard.isLoading}>
            <Typography.Text type="secondary">총 회원 수</Typography.Text>
            <Typography.Title level={3} style={{ margin: '8px 0 4px' }}>
              {formatMetricValue(totalMembers)}
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ margin: '12px 0 0' }}>
              현재 운영 중인 전체 회원 규모입니다.
            </Typography.Paragraph>
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <Card loading={dashboard.isLoading}>
            <Typography.Text type="secondary">총 포인트 잔액</Typography.Text>
            <Typography.Title level={3} style={{ margin: '8px 0 4px' }}>
              {formatMetricValue(totalPointBalance, 'P')}
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ margin: '12px 0 0' }}>
              현재 고객에게 남아 있는 전체 포인트 잔액입니다.
            </Typography.Paragraph>
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <Card
            loading={dashboard.isLoading}
            extra={
              <Button type="primary" size="small" onClick={() => nav('/members')}>
                캠페인 보내기
              </Button>
            }
          >
            <Typography.Text type="secondary">7일 내 소멸 예정</Typography.Text>
            <Typography.Title level={3} style={{ margin: '8px 0 4px' }}>
              {formatMetricValue(expiring.pointsExpiringWithin7Days, 'P')}
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ margin: '8px 0 0' }}>
              대상 회원: {formatMetricValue(expiring.membersWithExpiringLots)}명
            </Typography.Paragraph>
            <Typography.Paragraph type="secondary" style={{ margin: '12px 0 0' }}>
              곧 만료되는 잔여 포인트가 있는 회원에게 리마인드·프로모션을 보낼 수 있습니다.
            </Typography.Paragraph>
          </Card>
        </Col>
      </Row>

      <Modal
        open={goalModalOpen}
        title="이번 달 목표 설정"
        okText="저장"
        onOk={saveGoal}
        confirmLoading={savingGoal}
        onCancel={() => setGoalModalOpen(false)}
        destroyOnClose
      >
        <Form form={goalForm} layout="vertical" requiredMark={false}>
          <Form.Item label="신규회원 목표" name="targetNewMembers" rules={[{ required: true, message: '신규회원 목표를 입력하세요' }]}>
            <InputNumber min={0} style={{ width: '100%' }} step={1} />
          </Form.Item>
          <Form.Item label="포인트 적립 목표" name="targetEarn" rules={[{ required: true, message: '포인트 적립 목표를 입력하세요' }]}>
            <InputNumber min={0} style={{ width: '100%' }} step={100} />
          </Form.Item>
          <Form.Item label="포인트 사용 목표" name="targetUse" rules={[{ required: true, message: '포인트 사용 목표를 입력하세요' }]}>
            <InputNumber min={0} style={{ width: '100%' }} step={100} />
          </Form.Item>
        </Form>
      </Modal>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card
            title="최근 포인트 활동"
            extra={
              <Space size={8} wrap>
                <Select
                  value={activityFilter}
                  onChange={(value: ActivityFilter) => setActivityFilter(value)}
                  style={{ width: 130 }}
                  options={[
                    { value: 'ALL', label: '전체' },
                    { value: 'EARN', label: '적립' },
                    { value: 'USE', label: '사용' },
                    { value: 'EVENT', label: '이벤트' },
                  ]}
                />
                <Typography.Text type="secondary">최근 20건</Typography.Text>
              </Space>
            }
            loading={dashboard.isLoading}
          >
            <Table<RecentPointActivity>
              size="small"
              rowKey={(r) => r.id}
              dataSource={filteredRecentPoints}
              pagination={false}
              columns={[
                { title: '일시', dataIndex: 'createdAt', width: 160, render: (v: string) => formatInstant(v) },
                {
                  title: '회원번호',
                  dataIndex: 'memberNo',
                  width: 140,
                  render: (v: string) =>
                    v && v !== '-' ? (
                      <Button type="link" size="small" style={{ padding: 0 }} onClick={() => nav(`/members/${encodeURIComponent(v)}`)}>
                        {v}
                      </Button>
                    ) : (
                      '-'
                    ),
                },
                { title: '브랜드', dataIndex: 'brand', width: 120 },
                {
                  title: '구분',
                  dataIndex: 'type',
                  width: 90,
                  render: (v: RecentPointActivity['type']) => {
                    const color = v === 'EARN' ? 'green' : v === 'USE' ? 'volcano' : 'default'
                    const label = v === 'EARN' ? '적립' : v === 'USE' ? '사용' : '이벤트'
                    return <Tag color={color}>{label}</Tag>
                  },
                },
                {
                  title: '포인트',
                  dataIndex: 'amount',
                  width: 120,
                  render: (v: number, r: RecentPointActivity) => (
                    <Button
                      type="link"
                      size="small"
                      style={{ padding: 0 }}
                      onClick={() =>
                        nav(
                          `/points/history?memberNo=${encodeURIComponent(r.memberNo)}&typeGroup=${
                            r.type === 'EXPIRE' ? 'EVENT' : r.type
                          }`,
                        )
                      }
                    >
                      {nf.format(v)} P
                    </Button>
                  ),
                },
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
                { title: '관리자', dataIndex: 'adminName', width: 100 },
                {
                  title: '활동',
                  dataIndex: 'action',
                  render: (v: string) => (
                    <Typography.Text style={{ whiteSpace: 'normal', wordBreak: 'break-word' }}>{v}</Typography.Text>
                  ),
                },
              ]}
              locale={{ emptyText: '최근 작업이 없습니다.' }}
            />
          </Card>
        </Col>
      </Row>

    </PageShell>
  )
}

