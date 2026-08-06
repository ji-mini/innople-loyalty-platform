import { Alert, Card, Col, Empty, Row, Segmented, Space, Table, Tag, Tooltip, Typography } from 'antd'
import { ArrowDownOutlined, ArrowUpOutlined, MinusOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import React from 'react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip as ChartTooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { PageShell } from '../common/PageShell'
import { col } from '../../shared/tableColumns'
import { useCommonCodes } from '../../shared/queries'
import {
  DASHBOARD_PERIODS,
  useDashboardOverview,
  useDashboardTrends,
  type BatchStatusItem,
  type DashboardPeriod,
  type DistributionItem,
  type MetricValue,
} from '../../shared/dashboard'

const nf = new Intl.NumberFormat('ko-KR')

const CHART_COLORS = ['#ff8b7a', '#8bd8c2', '#9be1ff', '#ffb4d2', '#c3b5f5', '#ffd48b']

/** "전체 회원" 카드에 내역으로 노출할 상태 코드. 라벨은 공통코드(MEMBER_STATUS)에서 가져온다. */
const MEMBER_BREAKDOWN_STATUS_CODES = ['ACTIVE', 'DORMANT', 'WITHDRAW_REQUESTED']

const BATCH_LABELS: Record<string, string> = {
  AUTO_WITHDRAWAL: '회원 자동탈회',
  POINT_EXPIRATION: '포인트 자동소멸',
}

// 섹션 간 여백은 PageShell 의 Space gap 과 이 헤더 marginTop 의 합으로 결정된다.
// Typography.Title 의 기본 상단 마진에 의존하면 섹션마다 값이 달라지므로 명시적으로 고정한다.
const SECTION_HEADER_STYLE: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  gap: 12,
  flexWrap: 'wrap',
  marginTop: 16,
  marginBottom: 12,
}

const SECTION_TITLE_STYLE: React.CSSProperties = { margin: 0 }

// 같은 Row 안에서 내용 길이가 달라도(예: 상태 내역이 붙는 카드) 카드 높이를 맞춘다.
const CARD_STYLE: React.CSSProperties = { height: '100%' }
// 1·2단 카드 공통 내부 여백. 상하 20 유지(높이), 좌우 32로 테두리와 간격을 맞춤.
const CARD_BODY_STYLE: React.CSSProperties = { padding: '20px 32px' }

// 좌우 2분할 카드. 컨테이너 gap 을 쓰면 분할선이 밀리므로, 좌우 padding 으로만 간격을 준다.
const SPLIT_ROW_STYLE: React.CSSProperties = { display: 'flex', alignItems: 'stretch' }
// 1단 "전체 회원": 5:5
const SPLIT_LEFT_STYLE: React.CSSProperties = { flex: '1 1 50%', minWidth: 0, paddingRight: 16 }
const SPLIT_RIGHT_STYLE: React.CSSProperties = {
  flex: '1 1 50%',
  minWidth: 0,
  display: 'flex',
  borderLeft: '1px solid #f0f0f0',
  paddingLeft: 16,
}
// 2단 "적립/사용": 6:4 (누적 총액 : 이번 달)
const SPLIT_METRIC_LEFT_STYLE: React.CSSProperties = { flex: '1 1 60%', minWidth: 0, paddingRight: 16 }
const SPLIT_METRIC_RIGHT_STYLE: React.CSSProperties = {
  flex: '1 1 40%',
  minWidth: 0,
  display: 'flex',
  borderLeft: '1px solid #f0f0f0',
  paddingLeft: 16,
}

const BATCH_STATUS_META: Record<string, { color: string; label: string }> = {
  RUNNING: { color: 'blue', label: '실행중' },
  SUCCESS: { color: 'green', label: '성공' },
  PARTIAL: { color: 'orange', label: '부분성공' },
  FAILED: { color: 'red', label: '실패' },
}

function fmtDateTime(v: string | null | undefined): string {
  return v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '-'
}

/** 기간이 길수록 x축 라벨이 겹치므로 표시 간격을 늘린다. */
function axisInterval(pointCount: number): number {
  if (pointCount <= 10) return 0
  return Math.floor(pointCount / 8)
}

type KpiBreakdownItem = { key: string; label: string; count: number }

function DeltaLine(props: {
  delta: number
  unit: string
  /** day=전일 대비, month=전월 동기 대비 */
  compare?: 'day' | 'month'
  align?: 'flex-start' | 'flex-end'
}) {
  const { delta, unit, compare = 'day', align = 'flex-start' } = props
  const meta =
    delta > 0
      ? { color: '#cf1322', icon: <ArrowUpOutlined /> }
      : delta < 0
        ? { color: '#1677ff', icon: <ArrowDownOutlined /> }
        : { color: '#8c8c8c', icon: <MinusOutlined /> }
  const sameLabel = compare === 'month' ? '전월 동기와 동일' : '전일과 동일'
  const vsLabel = compare === 'month' ? '전월 동기 대비' : '전일 대비'

  return (
    <Space size={4} style={{ marginTop: 6, color: meta.color, fontSize: 12, justifyContent: align }}>
      {meta.icon}
      <span style={{ whiteSpace: 'nowrap' }}>
        {delta === 0 ? sameLabel : `${vsLabel} ${nf.format(Math.abs(delta))}${unit}`}
      </span>
    </Space>
  )
}

function KpiCard(props: {
  title: string
  metric?: MetricValue
  unit: string
  loading: boolean
  breakdown?: KpiBreakdownItem[]
  /** 증감 문구 기준. 기본 전일. */
  compare?: 'day' | 'month'
}) {
  const { title, metric, unit, loading, breakdown, compare = 'day' } = props

  const summary = (
    <>
      <Typography.Text type="secondary" style={{ fontSize: 13, display: 'block', marginBottom: 6 }}>
        {title}
      </Typography.Text>
      <Typography.Title level={3} style={{ margin: 0 }}>
        {nf.format(metric?.current ?? 0)}
        <Typography.Text type="secondary" style={{ fontSize: 13, marginLeft: 4 }}>
          {unit}
        </Typography.Text>
      </Typography.Title>
      <DeltaLine delta={metric?.delta ?? 0} unit={unit} compare={compare} />
    </>
  )

  return (
    <Card loading={loading} style={CARD_STYLE} styles={{ body: CARD_BODY_STYLE }}>
      {breakdown ? (
        <div style={SPLIT_ROW_STYLE}>
          <div style={SPLIT_LEFT_STYLE}>{summary}</div>
          <div
            style={{
              ...SPLIT_RIGHT_STYLE,
              alignItems: 'center',
              justifyContent: 'space-evenly',
            }}
          >
            {breakdown.map((item) => (
              <div key={item.key} style={{ textAlign: 'center', whiteSpace: 'nowrap' }}>
                <Typography.Text type="secondary" style={{ fontSize: 11, display: 'block' }}>
                  {item.label}
                </Typography.Text>
                <Typography.Text style={{ fontSize: 13, fontWeight: 600 }}>
                  {nf.format(item.count)}
                  {unit}
                </Typography.Text>
              </div>
            ))}
          </div>
        </div>
      ) : (
        <div>{summary}</div>
      )}
    </Card>
  )
}

/** 왼쪽에 누적 합계(강조), 오른쪽에 이번 달 값을 배치하는 카드 (6:4). */
function SplitMetricCard(props: {
  title: string
  total?: number
  unit: string
  sideTitle: string
  sideMetric?: MetricValue
  loading: boolean
}) {
  const { title, total, unit, sideTitle, sideMetric, loading } = props

  return (
    <Card loading={loading} style={CARD_STYLE} styles={{ body: CARD_BODY_STYLE }}>
      <div style={SPLIT_ROW_STYLE}>
        <div style={SPLIT_METRIC_LEFT_STYLE}>
          <Typography.Text type="secondary" style={{ fontSize: 13, display: 'block', marginBottom: 6 }}>
            {title}
          </Typography.Text>
          <Typography.Title level={3} style={{ margin: 0 }}>
            {nf.format(total ?? 0)}
            <Typography.Text type="secondary" style={{ fontSize: 13, marginLeft: 4 }}>
              {unit}
            </Typography.Text>
          </Typography.Title>
        </div>
        <div
          style={{
            ...SPLIT_METRIC_RIGHT_STYLE,
            flexDirection: 'column',
            alignItems: 'flex-end',
            justifyContent: 'center',
            whiteSpace: 'nowrap',
          }}
        >
          <Typography.Text type="secondary" style={{ fontSize: 11 }}>
            {sideTitle}
          </Typography.Text>
          <Typography.Text style={{ fontSize: 14, fontWeight: 600 }}>
            {nf.format(sideMetric?.current ?? 0)}
            {unit}
          </Typography.Text>
        </div>
      </div>
    </Card>
  )
}

function LiabilityCard(props: { title: string; value?: number; hint: string; loading: boolean }) {
  return (
    <Card loading={props.loading} style={CARD_STYLE} styles={{ body: CARD_BODY_STYLE }}>
      <Typography.Text type="secondary" style={{ fontSize: 13, display: 'block', marginBottom: 6 }}>
        {props.title}
      </Typography.Text>
      <Typography.Title level={3} style={{ margin: 0 }}>
        {nf.format(props.value ?? 0)}
        <Typography.Text type="secondary" style={{ fontSize: 13, marginLeft: 4 }}>
          P
        </Typography.Text>
      </Typography.Title>
      <Typography.Text type="secondary" style={{ fontSize: 12 }}>
        {props.hint}
      </Typography.Text>
    </Card>
  )
}

function DistributionChart(props: { items: DistributionItem[]; emptyText: string }) {
  const data = props.items.filter((i) => i.count > 0)
  if (data.length === 0) {
    return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={props.emptyText} style={{ margin: '48px 0' }} />
  }
  return (
    <ResponsiveContainer width="100%" height={240}>
      <PieChart>
        <Pie
          data={data}
          dataKey="count"
          nameKey="label"
          cx="50%"
          cy="50%"
          innerRadius={55}
          outerRadius={85}
          paddingAngle={2}
          label={({ name, value }) => `${name} ${nf.format(Number(value))}`}
        >
          {data.map((_, i) => (
            <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />
          ))}
        </Pie>
        <ChartTooltip formatter={(v: number) => `${nf.format(v)}명`} />
      </PieChart>
    </ResponsiveContainer>
  )
}

export function DashboardPage() {
  const [period, setPeriod] = React.useState<DashboardPeriod>('30D')

  const overviewQuery = useDashboardOverview()
  const trendsQuery = useDashboardTrends(period)
  const statusCodes = useCommonCodes('MEMBER_STATUS')

  const overview = overviewQuery.data
  const overviewLoading = overviewQuery.isLoading

  // 상태 코드 한글 표기는 공통코드(MEMBER_STATUS)에서 가져온다.
  const statusLabelMap = React.useMemo(() => {
    const map: Record<string, string> = {}
    for (const c of statusCodes.data ?? []) {
      map[c.code] = c.name
    }
    return map
  }, [statusCodes.data])

  const statusDistribution = React.useMemo(
    () =>
      (overview?.statusDistribution ?? []).map((item) => ({
        ...item,
        label: statusLabelMap[item.key] ?? item.label,
      })),
    [overview?.statusDistribution, statusLabelMap]
  )

  // 4단 상태 분포와 같은 집계를 재사용한다(추가 조회 없음).
  const memberBreakdown = React.useMemo(() => {
    const countByCode = new Map((overview?.statusDistribution ?? []).map((i) => [i.key, i.count]))
    return MEMBER_BREAKDOWN_STATUS_CODES.map((code) => ({
      key: code,
      label: statusLabelMap[code] ?? code,
      count: countByCode.get(code) ?? 0,
    }))
  }, [overview?.statusDistribution, statusLabelMap])

  const trendData = React.useMemo(
    () =>
      (trendsQuery.data?.points ?? []).map((p) => ({
        ...p,
        label: dayjs(p.date).format('MM-DD'),
      })),
    [trendsQuery.data?.points]
  )

  const tickInterval = axisInterval(trendData.length)
  const failedBatches = (overview?.batches ?? []).filter(
    (b) => b.status === 'FAILED' || b.status === 'PARTIAL'
  )

  return (
    <PageShell
      title="대시보드"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          기준일 {overview?.baseDate ?? '-'} (KST)
        </Typography.Text>
      }
    >
      {overviewQuery.isError ? (
        <Alert type="error" showIcon message="대시보드 데이터를 불러오지 못했습니다." />
      ) : null}

      {failedBatches.length > 0 ? (
        <Alert
          type="error"
          showIcon
          message={`최근 실행에서 실패한 배치가 ${failedBatches.length}건 있습니다.`}
          description={failedBatches
            .map((b) => `${BATCH_LABELS[b.batchName] ?? b.batchName}: 실패 ${nf.format(b.errorCount)}건`)
            .join(' · ')}
        />
      ) : null}

      {/* 1단 — 회원 현황 */}
      <div>
        <div style={SECTION_HEADER_STYLE}>
          <Typography.Title level={5} style={SECTION_TITLE_STYLE}>
            회원 현황
          </Typography.Title>
        </div>
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} lg={6}>
            <KpiCard
              title="이번 달 신규 가입"
              metric={overview?.today.newMembers}
              unit="명"
              loading={overviewLoading}
              compare="month"
            />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <KpiCard
              title="이번 달 회원 탈회"
              metric={overview?.today.withdrawals}
              unit="명"
              loading={overviewLoading}
              compare="month"
            />
          </Col>
          {/* 상태별 내역이 오른쪽에 붙으므로 다른 카드보다 넓게 잡는다. */}
          <Col xs={24} lg={12}>
            <KpiCard
              title="전체 회원"
              metric={overview?.today.activeMembers}
              unit="명"
              // 공통코드가 도착하기 전에 상태 라벨이 코드값으로 잠깐 보이지 않도록 함께 기다린다.
              loading={overviewLoading || statusCodes.isLoading}
              breakdown={memberBreakdown}
              compare="day"
            />
          </Col>
        </Row>
      </div>

      {/* 2단 — 포인트 현황. 카드 4개를 lg 이상에서 한 줄에 배치한다. */}
      <div>
        <div style={SECTION_HEADER_STYLE}>
          <Typography.Title level={5} style={SECTION_TITLE_STYLE}>
            포인트 현황
          </Typography.Title>
        </div>
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} lg={6}>
            <SplitMetricCard
              title="누적 총 적립 포인트"
              total={overview?.pointLiability.totalEarned}
              unit="P"
              sideTitle="이번 달 적립"
              sideMetric={overview?.today.earnedPoints}
              loading={overviewLoading}
            />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <SplitMetricCard
              title="누적 총 사용 포인트"
              total={overview?.pointLiability.totalUsed}
              unit="P"
              sideTitle="이번 달 사용"
              sideMetric={overview?.today.usedPoints}
              loading={overviewLoading}
            />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <LiabilityCard
              title="미사용 포인트 총 잔액"
              value={overview?.pointLiability.outstandingBalance}
              hint="소멸되지 않은 적립분의 잔여 합계"
              loading={overviewLoading}
            />
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <LiabilityCard
              title="이번 달 소멸 예정 금액"
              value={overview?.pointLiability.expiringThisMonth}
              hint="이번 달 안에 만료되는 잔여분"
              loading={overviewLoading}
            />
          </Col>
        </Row>
      </div>

      {/* 3단 — 추이 차트 */}
      <div>
        <div style={SECTION_HEADER_STYLE}>
          <Typography.Title level={5} style={SECTION_TITLE_STYLE}>
            추이
          </Typography.Title>
          <Segmented
            value={period}
            onChange={(v) => setPeriod(v as DashboardPeriod)}
            options={DASHBOARD_PERIODS.map((p) => ({ value: p.value, label: p.label }))}
          />
        </div>
        <Row gutter={[16, 16]}>
          <Col xs={24} xl={12}>
            <Card title="회원 증감 추이" loading={trendsQuery.isLoading}>
              <ResponsiveContainer width="100%" height={280}>
                <LineChart data={trendData} margin={{ top: 8, right: 16, left: 0, bottom: 8 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                  <XAxis dataKey="label" tick={{ fontSize: 11 }} interval={tickInterval} />
                  <YAxis tick={{ fontSize: 11 }} allowDecimals={false} tickFormatter={(v) => nf.format(v)} />
                  <ChartTooltip formatter={(v: number) => `${nf.format(v)}명`} />
                  <Legend />
                  <Line type="monotone" dataKey="signups" name="가입" stroke="#8bd8c2" strokeWidth={2} dot={false} />
                  <Line type="monotone" dataKey="withdrawals" name="탈퇴" stroke="#ff8b7a" strokeWidth={2} dot={false} />
                </LineChart>
              </ResponsiveContainer>
            </Card>
          </Col>
          <Col xs={24} xl={12}>
            <Card title="포인트 적립 vs 사용 추이" loading={trendsQuery.isLoading}>
              <ResponsiveContainer width="100%" height={280}>
                <BarChart data={trendData} margin={{ top: 8, right: 16, left: 0, bottom: 8 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                  <XAxis dataKey="label" tick={{ fontSize: 11 }} interval={tickInterval} />
                  <YAxis tick={{ fontSize: 11 }} tickFormatter={(v) => nf.format(v)} />
                  <ChartTooltip formatter={(v: number) => `${nf.format(v)}P`} />
                  <Legend />
                  <Bar dataKey="earnedPoints" name="적립" fill="#9be1ff" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="usedPoints" name="사용" fill="#ffb4d2" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </Card>
          </Col>
        </Row>
      </div>

      {/* 4단 — 분포 · 배치 상태 */}
      <div>
        <div style={SECTION_HEADER_STYLE}>
          <Typography.Title level={5} style={SECTION_TITLE_STYLE}>
            분포 · 상태
          </Typography.Title>
        </div>
        <Row gutter={[16, 16]}>
          <Col xs={24} xl={8}>
            <Card title="등급별 회원 분포" loading={overviewLoading} style={{ height: '100%' }}>
              <DistributionChart
                items={overview?.gradeDistribution ?? []}
                emptyText="등급별 회원 데이터가 없습니다."
              />
            </Card>
          </Col>
          <Col xs={24} xl={8}>
            <Card title="회원 상태 분포" loading={overviewLoading} style={{ height: '100%' }}>
              <DistributionChart items={statusDistribution} emptyText="회원 상태 데이터가 없습니다." />
            </Card>
          </Col>
          <Col xs={24} xl={8}>
            <Card title="배치 상태" loading={overviewLoading} style={{ height: '100%' }}>
              <Table<BatchStatusItem>
                rowKey={(r) => r.batchName}
                dataSource={overview?.batches ?? []}
                pagination={false}
                size="small"
                locale={{
                  emptyText: (
                    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="등록된 배치가 없습니다." />
                  ),
                }}
                columns={[
                  {
                    ...col('배치', 'left'),
                    dataIndex: 'batchName',
                    render: (v: string, row) => (
                      <Space direction="vertical" size={0}>
                        <Typography.Text strong>{BATCH_LABELS[v] ?? v}</Typography.Text>
                        {!row.enabled ? (
                          <Typography.Text type="secondary" style={{ fontSize: 11 }}>
                            비활성
                          </Typography.Text>
                        ) : null}
                      </Space>
                    ),
                  },
                  {
                    ...col('최근 실행'),
                    dataIndex: 'status',
                    width: 110,
                    render: (v: string | null, row) => {
                      if (!v) return <Tag>실행 이력 없음</Tag>
                      const meta = BATCH_STATUS_META[v] ?? { color: 'default', label: v }
                      const tag = <Tag color={meta.color}>{meta.label}</Tag>
                      return row.errorMessage ? <Tooltip title={row.errorMessage}>{tag}</Tooltip> : tag
                    },
                  },
                  {
                    ...col('시각'),
                    dataIndex: 'startedAt',
                    width: 130,
                    render: (v: string | null) => (
                      <Typography.Text style={{ fontSize: 12 }}>{fmtDateTime(v)}</Typography.Text>
                    ),
                  },
                  {
                    ...col('처리/실패', 'right'),
                    dataIndex: 'processedCount',
                    width: 90,
                    render: (v: number, row) => (
                      <Typography.Text style={{ fontSize: 12 }} type={row.errorCount > 0 ? 'danger' : undefined}>
                        {nf.format(v)} / {nf.format(row.errorCount)}
                      </Typography.Text>
                    ),
                  },
                ]}
              />
            </Card>
          </Col>
        </Row>
      </div>
    </PageShell>
  )
}
