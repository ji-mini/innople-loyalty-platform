import { Button, Card, Descriptions, Space, Table, Tabs, Tag, Typography } from 'antd'
import { useNavigate, useParams } from 'react-router-dom'
import { useCommonCodes, useMemberDetail, useMemberLedgers } from '../../shared/queries'
import type { MemberAddress, MemberLedger } from '../../shared/types'
import { getSession } from '../../shared/storage'

function formatAddress(addr: MemberAddress | null | undefined): string {
  if (!addr) return '-'
  const base = addr.roadAddress ?? addr.jibunAddress ?? ''
  const detail = addr.detailAddress?.trim()
  return detail ? `${base} ${detail}` : base || '-'
}

export function MemberDetailPage() {
  const nav = useNavigate()
  const params = useParams()
  const memberNo = params.memberNo ?? ''
  const role = getSession()?.role ?? 'OPERATOR'

  const detail = useMemberDetail(memberNo)
  const ledgers = useMemberLedgers(memberNo, 100)
  const statusCodes = useCommonCodes('MEMBER_STATUS')

  const getStatusName = (code: string | null | undefined) =>
    code ? (statusCodes.data?.find((c) => c.code === code)?.name ?? code) : '-'

  const ledgerCols = [
    { title: '일시', dataIndex: 'createdAt' },
    { title: '이벤트', dataIndex: 'eventType', render: (v: string) => <Tag>{v}</Tag> },
    { title: '상태(전)', dataIndex: 'statusCodeBefore', render: getStatusName },
    { title: '상태(후)', dataIndex: 'statusCodeAfter', render: getStatusName },
  ]

  return (
    <Space direction="vertical" style={{ width: '100%' }} size={16}>
      <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12, flexWrap: 'wrap' }}>
        <Typography.Title level={4} style={{ margin: 0 }}>
          회원 상세
        </Typography.Title>
        {role === 'SUPER_ADMIN' ? (
          <Space>
            <Button onClick={() => nav(`/points/manual/earn?memberNo=${encodeURIComponent(memberNo)}`)}>포인트 적립</Button>
            <Button danger onClick={() => nav(`/points/manual/deduct?memberNo=${encodeURIComponent(memberNo)}`)}>
              포인트 차감
            </Button>
          </Space>
        ) : null}
      </div>

      <Tabs
        items={[
          {
            key: 'info',
            label: '회원정보',
            children: (
              <Card loading={detail.isLoading}>
                <Descriptions bordered size="small" column={2}>
                  <Descriptions.Item label="회원번호">{detail.data?.memberNo}</Descriptions.Item>
                  <Descriptions.Item label="이름">{detail.data?.name}</Descriptions.Item>
                  <Descriptions.Item label="상태">{getStatusName(detail.data?.statusCode)}</Descriptions.Item>
                  <Descriptions.Item label="가입일">{detail.data?.joinedAt}</Descriptions.Item>
                  <Descriptions.Item label="생년월일">{detail.data?.birthDate ?? '-'}</Descriptions.Item>
                  <Descriptions.Item label="양/음력">{detail.data?.calendarType ?? '-'}</Descriptions.Item>
                  <Descriptions.Item label="성별">{detail.data?.gender ?? '-'}</Descriptions.Item>
                  <Descriptions.Item label="휴대폰">{detail.data?.phoneNumber ?? '-'}</Descriptions.Item>
                  <Descriptions.Item label="이메일">{detail.data?.email ?? '-'}</Descriptions.Item>
                  <Descriptions.Item label="주소" span={2}>
                    {formatAddress(detail.data?.address)}
                  </Descriptions.Item>
                  <Descriptions.Item label="Web ID">{detail.data?.webId ?? '-'}</Descriptions.Item>
                  <Descriptions.Item label="CI">{detail.data?.ci ?? '-'}</Descriptions.Item>
                  <Descriptions.Item label="휴면일자">{detail.data?.dormantAt ?? '-'}</Descriptions.Item>
                  <Descriptions.Item label="탈퇴일자">{detail.data?.withdrawnAt ?? '-'}</Descriptions.Item>
                  <Descriptions.Item label="기념일" span={2}>
                    {detail.data?.anniversaries ?? '-'}
                  </Descriptions.Item>
                </Descriptions>
              </Card>
            ),
          },
          {
            key: 'points',
            label: '포인트 이력',
            children: (
              <Card>
                <Typography.Text type="secondary">포인트 이력 탭은 준비 중입니다.</Typography.Text>
              </Card>
            ),
          },
          {
            key: 'coupons',
            label: '쿠폰',
            children: (
              <Card>
                <Typography.Text type="secondary">쿠폰 탭은 준비 중입니다.</Typography.Text>
              </Card>
            ),
          },
          {
            key: 'activity',
            label: '활동 로그',
            children: (
              <Card title="회원 원장(최근 100건)" loading={ledgers.isLoading}>
                <Table<MemberLedger>
                  rowKey={(r) => r.id}
                  columns={ledgerCols as any}
                  dataSource={ledgers.data ?? []}
                  pagination={false}
                  size="small"
                />
              </Card>
            ),
          },
        ]}
      />
    </Space>
  )
}

