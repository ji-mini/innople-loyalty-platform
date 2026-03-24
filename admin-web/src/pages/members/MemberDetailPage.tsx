import { Alert, Button, Card, DatePicker, Descriptions, Form, Input, Modal, Select, Space, Table, Tabs, Tag, Typography, message } from 'antd'
import dayjs from 'dayjs'
import React from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { api } from '../../shared/api'
import { useCommonCodes, useMemberDetail, useMemberLedgers, usePointLedgers } from '../../shared/queries'
import type { MemberAddress, MemberDetail, MemberLedger, PointLedgerItem } from '../../shared/types'
import { getSession } from '../../shared/storage'
import { atLeast } from '../../shared/roles'

function formatAddress(addr: MemberAddress | null | undefined): string {
  if (!addr) return '-'
  const base = addr.roadAddress ?? addr.jibunAddress ?? ''
  const detail = addr.detailAddress?.trim()
  return detail ? `${base} ${detail}` : base || '-'
}

function formatCalendarType(v: string | null | undefined): string {
  if (!v) return '-'
  return v === 'SOLAR' ? '양력' : v === 'LUNAR' ? '음력' : v
}

function formatGender(v: string | null | undefined): string {
  if (!v) return '-'
  return v === 'MALE' ? '남성' : v === 'FEMALE' ? '여성' : v === 'UNKNOWN' ? '미정' : v
}

function formatPointSourceChannel(v: string | null | undefined): string {
  if (!v) return '-'
  return v === 'ADMIN_WEB_MANUAL_EARN'
    ? '관리자 웹 수기 적립'
    : v === 'ADMIN_WEB_MANUAL_USE'
      ? '관리자 웹 수기 차감'
      : v === 'ADMIN_WEB_MANUAL_EXPIRE'
        ? '관리자 웹 수기 소멸'
        : v === 'SYSTEM_AUTO_EXPIRE'
          ? '시스템 자동 소멸'
          : v
}

type JusoSearchItem = { roadAddr: string; roadAddrPart1: string; jibunAddr: string; zipNo: string; bdNm?: string }

function MemberEditModal({
  open,
  onClose,
  member,
  loading,
  form,
  onOpen,
  onSubmit,
}: {
  open: boolean
  onClose: () => void
  member: MemberDetail | null
  loading: boolean
  form: ReturnType<typeof Form.useForm>[0]
  onOpen: () => void
  onSubmit: (v: any) => Promise<void>
}) {
  const [addrOpen, setAddrOpen] = React.useState(false)
  const [addrKeyword, setAddrKeyword] = React.useState('')
  const [addrLoading, setAddrLoading] = React.useState(false)
  const [addrResults, setAddrResults] = React.useState<JusoSearchItem[]>([])
  const [addrError, setAddrError] = React.useState<string | null>(null)

  React.useEffect(() => {
    if (open && member) onOpen()
  }, [open, member, onOpen])

  const searchAddr = async () => {
    const k = addrKeyword.trim()
    if (k.length < 2) {
      message.warning('검색어는 두 글자 이상 입력해주세요.')
      return
    }
    setAddrLoading(true)
    setAddrError(null)
    try {
      const res = await api.get<{ results?: { common?: { errorCode?: string }; juso?: JusoSearchItem | JusoSearchItem[] } }>(
        '/api/v1/public/juso/search',
        { params: { keyword: k, currentPage: 1, countPerPage: 20 } }
      )
      const common = res.data?.results?.common
      if (common?.errorCode && common.errorCode !== '0') {
        setAddrError('검색 실패')
        setAddrResults([])
        return
      }
      const juso = res.data?.results?.juso
      const list = Array.isArray(juso) ? juso : juso ? [juso] : []
      setAddrResults(list)
      if (list.length === 0) setAddrError('검색 결과가 없습니다.')
    } catch {
      setAddrError('주소 검색 실패')
      setAddrResults([])
    } finally {
      setAddrLoading(false)
    }
  }

  const selectAddr = (item: JusoSearchItem) => {
    form.setFieldsValue({
      address: {
        zipCode: item.zipNo ?? '',
        roadAddress: item.roadAddrPart1 ?? item.roadAddr ?? '',
        jibunAddress: item.jibunAddr ?? '',
        detailAddress: form.getFieldValue(['address', 'detailAddress']) ?? '',
        buildingName: item.bdNm ?? '',
      },
    })
    setAddrOpen(false)
    message.success('주소가 적용되었습니다.')
  }

  return (
    <>
      <Modal
        title="회원정보 수정"
        open={open}
        onCancel={onClose}
        footer={null}
        width={560}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={(v) => onSubmit(v)}>
          <Form.Item label="이름" name="name" rules={[{ required: true, message: '이름을 입력하세요' }]}>
            <Input placeholder="이름" />
          </Form.Item>
          <Form.Item label="생년월일" name="birthDate">
            <DatePicker style={{ width: '100%' }} placeholder="선택" />
          </Form.Item>
          <Form.Item label="양/음력" name="calendarType">
            <Select placeholder="선택" allowClear options={[{ value: 'SOLAR', label: '양력' }, { value: 'LUNAR', label: '음력' }]} />
          </Form.Item>
          <Form.Item label="성별" name="gender">
            <Select
              placeholder="선택"
              allowClear
              options={[
                { value: 'MALE', label: '남성' },
                { value: 'FEMALE', label: '여성' },
                { value: 'UNKNOWN', label: '미정' },
              ]}
            />
          </Form.Item>
          <Form.Item
            label="휴대폰"
            name="phoneNumber"
            getValueFromEvent={(e) => String(e?.target?.value ?? '').replace(/\D/g, '')}
          >
            <Input placeholder="01000000000" inputMode="numeric" />
          </Form.Item>
          <Form.Item label="이메일" name="email">
            <Input placeholder="이메일" type="email" />
          </Form.Item>
          <Form.Item label="Web ID" name="webId">
            <Input placeholder="Web ID" />
          </Form.Item>
          <Form.Item label="CI" name="ci">
            <Input placeholder="CI" />
          </Form.Item>
          <Form.Item label="기념일" name="anniversaries">
            <Input placeholder="기념일 (예: 0101)" />
          </Form.Item>
          <Form.Item label="주소">
            <Space.Compact style={{ width: '100%' }}>
              <Form.Item name={['address', 'zipCode']} noStyle>
                <Input placeholder="우편번호" readOnly style={{ width: 100 }} />
              </Form.Item>
              <Button type="default" onClick={() => setAddrOpen(true)}>
                주소 검색
              </Button>
            </Space.Compact>
            <Form.Item name={['address', 'roadAddress']} noStyle>
              <Input placeholder="도로명주소" readOnly style={{ marginTop: 8, width: '100%' }} />
            </Form.Item>
            <Form.Item name={['address', 'detailAddress']} noStyle>
              <Input placeholder="상세주소" style={{ marginTop: 8, width: '100%' }} />
            </Form.Item>
          </Form.Item>
          <Form.Item style={{ marginBottom: 0, marginTop: 16 }}>
            <Space>
              <Button type="primary" htmlType="submit" loading={loading}>
                저장
              </Button>
              <Button onClick={onClose}>취소</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="주소 검색" open={addrOpen} onCancel={() => setAddrOpen(false)} footer={null} width={500}>
        <Space direction="vertical" style={{ width: '100%' }} size={12}>
          <Space.Compact style={{ width: '100%' }}>
            <Input
              placeholder="도로명, 지번, 건물명 검색"
              value={addrKeyword}
              onChange={(e) => setAddrKeyword(e.target.value)}
              onPressEnter={searchAddr}
              style={{ flex: 1 }}
            />
            <Button type="primary" onClick={searchAddr} loading={addrLoading}>
              조회
            </Button>
          </Space.Compact>
          {addrError && <Typography.Text type="danger">{addrError}</Typography.Text>}
          <div style={{ maxHeight: 280, overflow: 'auto' }}>
            {addrResults.map((item, i) => (
              <div
                key={i}
                style={{ padding: 8, cursor: 'pointer', borderBottom: '1px solid #f0f0f0' }}
                onClick={() => selectAddr(item)}
              >
                <Typography.Text>{item.roadAddrPart1 ?? item.roadAddr}</Typography.Text>
                {item.jibunAddr && <Typography.Text type="secondary"> ({item.jibunAddr})</Typography.Text>}
              </div>
            ))}
          </div>
        </Space>
      </Modal>
    </>
  )
}

export function MemberDetailPage() {
  const nav = useNavigate()
  const params = useParams()
  const memberNo = params.memberNo ?? ''
  const role = getSession()?.role ?? 'OPERATOR'

  const detail = useMemberDetail(memberNo)
  const ledgers = useMemberLedgers(memberNo, 100)
  const pointLedgers = usePointLedgers({ memberNo: memberNo || undefined, limit: 100 })
  const statusCodes = useCommonCodes('MEMBER_STATUS')

  const pointEventTypeLabel = (v: string) =>
    v === 'EARN' || v === 'ADJUST_EARN'
      ? '적립'
      : v === 'USE' || v === 'ADJUST_USE'
        ? '사용'
        : v === 'EXPIRE_AUTO'
          ? '자동 소멸'
          : v === 'EXPIRE_MANUAL'
            ? '수동 소멸'
            : v
  const [editOpen, setEditOpen] = React.useState(false)
  const [editLoading, setEditLoading] = React.useState(false)
  const [editForm] = Form.useForm()

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
        <Space>
          {atLeast(role, 'ADMIN') && (
            <Button onClick={() => setEditOpen(true)}>회원정보 수정</Button>
          )}
          {role === 'SUPER_ADMIN' ? (
            <>
              <Button onClick={() => nav(`/points/manual/earn?memberNo=${encodeURIComponent(memberNo)}`)}>포인트 적립</Button>
              <Button danger onClick={() => nav(`/points/manual/deduct?memberNo=${encodeURIComponent(memberNo)}`)}>
                포인트 차감
              </Button>
            </>
          ) : null}
        </Space>
      </div>

      <Tabs
        defaultActiveKey="info"
        items={[
          {
            key: 'info',
            label: '회원정보',
            children: (
              <Card loading={detail.isLoading}>
                {detail.isError && (
                  <Alert
                    type="error"
                    message="회원 정보를 불러오지 못했습니다."
                    description={String(detail.error ?? '')}
                    showIcon
                    style={{ marginBottom: 16 }}
                  />
                )}
                {!detail.isLoading && !detail.data && !detail.isError && (
                  <Typography.Text type="secondary">회원을 찾을 수 없습니다.</Typography.Text>
                )}
                {detail.data && (
                  <Descriptions bordered size="small" column={2}>
                    <Descriptions.Item label="회원번호">{detail.data.memberNo ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="이름">{detail.data.name ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="상태">{getStatusName(detail.data.statusCode)}</Descriptions.Item>
                    <Descriptions.Item label="등급">{detail.data.gradeName ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="가입일시">{detail.data.joinedAt ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="생년월일">{detail.data.birthDate ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="양/음력">{formatCalendarType(detail.data.calendarType)}</Descriptions.Item>
                    <Descriptions.Item label="성별">{formatGender(detail.data.gender)}</Descriptions.Item>
                    <Descriptions.Item label="휴대폰">{detail.data.phoneNumber ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="이메일">{detail.data.email ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="주소" span={2}>
                      {formatAddress(detail.data.address)}
                    </Descriptions.Item>
                    <Descriptions.Item label="Web ID">{detail.data.webId ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="CI">{detail.data.ci ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="휴면일자">{detail.data.dormantAt ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="탈퇴일자">{detail.data.withdrawnAt ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="기념일" span={2}>
                      {detail.data.anniversaries ?? '-'}
                    </Descriptions.Item>
                  </Descriptions>
                )}
              </Card>
            ),
          },
          {
            key: 'points',
            label: '포인트 이력',
            children: (
              <Card
                title="포인트 이력 (최근 100건)"
                extra={
                  <Typography.Text strong>
                    현재 잔액 {detail.data?.pointBalance?.toLocaleString('ko-KR') ?? '0'} P
                  </Typography.Text>
                }
                loading={pointLedgers.isLoading}
              >
                <Table<PointLedgerItem>
                  rowKey={(r) => r.id}
                  size="small"
                  pagination={false}
                  dataSource={pointLedgers.data ?? []}
                  columns={[
                    {
                      title: '구분',
                      dataIndex: 'eventType',
                      width: 100,
                      render: (v: string) => {
                        const color =
                          v === 'EARN' || v === 'ADJUST_EARN'
                            ? 'green'
                            : v === 'USE' || v === 'ADJUST_USE'
                              ? 'volcano'
                              : 'default'
                        return <Tag color={color}>{pointEventTypeLabel(v)}</Tag>
                      },
                    },
                    {
                      title: '포인트',
                      dataIndex: 'amount',
                      width: 110,
                      render: (v: number) => `${v >= 0 ? '+' : ''}${v.toLocaleString('ko-KR')} P`,
                    },
                    {
                      title: '경로',
                      dataIndex: 'sourceChannel',
                      width: 170,
                      render: (v: string) => formatPointSourceChannel(v),
                    },
                    { title: '사유', dataIndex: 'reason', ellipsis: true },
                    { title: '일시', dataIndex: 'createdAt', width: 170 },
                  ]}
                  locale={{
                    emptyText: <Typography.Text type="secondary">포인트 이력이 없습니다.</Typography.Text>,
                  }}
                />
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

      {atLeast(role, 'ADMIN') && (
        <MemberEditModal
          open={editOpen}
          onClose={() => setEditOpen(false)}
          member={detail.data ?? null}
          loading={editLoading}
          form={editForm}
          onOpen={() => {
            const d = detail.data
            if (!d) return
            editForm.setFieldsValue({
              name: d.name ?? '',
              birthDate: d.birthDate ? dayjs(d.birthDate) : null,
              calendarType: d.calendarType ?? undefined,
              gender: d.gender ?? undefined,
              phoneNumber: d.phoneNumber ?? '',
              email: d.email ?? '',
              webId: d.webId ?? '',
              ci: d.ci ?? '',
              anniversaries: d.anniversaries ?? '',
              address: d.address
                ? {
                    zipCode: d.address.zipCode ?? '',
                    roadAddress: d.address.roadAddress ?? '',
                    jibunAddress: d.address.jibunAddress ?? '',
                    detailAddress: d.address.detailAddress ?? '',
                    buildingName: d.address.buildingName ?? '',
                  }
                : undefined,
            })
          }}
          onSubmit={async (v) => {
            setEditLoading(true)
            try {
              const addr = v.address
              const hasAddress = addr?.zipCode?.trim() && addr?.roadAddress?.trim()
              await api.put(`/api/v1/members/${encodeURIComponent(memberNo)}`, {
                name: v.name.trim(),
                birthDate: v.birthDate?.format?.('YYYY-MM-DD') ?? null,
                calendarType: v.calendarType ?? null,
                gender: v.gender ?? null,
                phoneNumber: v.phoneNumber?.trim() || null,
                email: v.email?.trim() || null,
                webId: v.webId?.trim() || null,
                ci: v.ci?.trim() || null,
                anniversaries: v.anniversaries?.trim() || null,
                address: hasAddress
                  ? {
                      zipCode: addr!.zipCode.trim(),
                      roadAddress: addr!.roadAddress.trim(),
                      jibunAddress: addr!.jibunAddress?.trim() || undefined,
                      detailAddress: addr!.detailAddress?.trim() || undefined,
                      buildingName: addr!.buildingName?.trim() || undefined,
                      siDo: undefined,
                      siGunGu: undefined,
                      eupMyeonDong: undefined,
                      legalDongCode: undefined,
                    }
                  : null,
              })
              message.success('회원정보가 수정되었습니다.')
              setEditOpen(false)
              detail.refetch()
            } catch (e: any) {
              message.error(e?.response?.data?.message ?? e?.message ?? '수정 실패')
            } finally {
              setEditLoading(false)
            }
          }}
        />
      )}
    </Space>
  )
}

