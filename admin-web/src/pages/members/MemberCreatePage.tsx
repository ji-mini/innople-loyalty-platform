import { Button, Card, DatePicker, Form, Input, Modal, Select, Space, Typography, message } from 'antd'
import dayjs from 'dayjs'
import React from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../../shared/api'
import { useCommonCodes } from '../../shared/queries'
import { PageShell } from '../common/PageShell'

type JusoSearchItem = {
  roadAddr: string
  roadAddrPart1: string
  roadAddrPart2?: string
  jibunAddr: string
  zipNo: string
  bdNm?: string
}

type AddressState = {
  zipCode: string
  roadAddress: string
  jibunAddress: string
  buildingName: string
  detailAddress: string
}

const INITIAL_ADDRESS: AddressState = {
  zipCode: '',
  roadAddress: '',
  jibunAddress: '',
  buildingName: '',
  detailAddress: '',
}

type AddressForm = AddressState & {
  siDo?: string
  siGunGu?: string
  eupMyeonDong?: string
  legalDongCode?: string
}

type FormValues = {
  memberNo: string
  name: string
  birthDate?: any
  calendarType?: 'SOLAR' | 'LUNAR'
  gender?: 'MALE' | 'FEMALE' | 'UNKNOWN'
  anniversaries?: any
  phoneNumber?: string
  ci?: string
  email?: string
  webId?: string
  joinedAt?: any
  statusCode?: string
  address?: AddressForm
}

const DEFAULT_STATUS = 'ACTIVE'

export function MemberCreatePage() {
  const nav = useNavigate()
  const [loading, setLoading] = React.useState(false)
  const [memberNoLoading, setMemberNoLoading] = React.useState(false)
  const [form] = Form.useForm<FormValues>()
  const statusCodes = useCommonCodes('MEMBER_STATUS')
  const phone = Form.useWatch('phoneNumber', form)
  const memberNo = Form.useWatch('memberNo', form)
  const webId = Form.useWatch('webId', form)

  const applyAddressData = React.useCallback(
    (data: AddressState) => {
      form.setFieldsValue({
        address: {
          ...data,
          detailAddress: form.getFieldValue(['address', 'detailAddress']) ?? '',
        },
      })
    },
    [form]
  )

  React.useEffect(() => {
    const digits = String(phone ?? '').replace(/\D/g, '')
    if (digits.length < 4) {
      form.setFieldsValue({ memberNo: '' })
      return
    }

    let cancelled = false
    const id = window.setTimeout(async () => {
      setMemberNoLoading(true)
      try {
        const res = await api.get<{ memberNo: string }>('/api/v1/members/member-no/suggest', { params: { phoneNumber: digits } })
        if (cancelled) return
        form.setFieldsValue({ memberNo: res.data.memberNo })
      } catch (e: any) {
        if (cancelled) return
        message.error(e?.response?.data?.message ?? e?.message ?? '회원번호 자동 생성 실패')
      } finally {
        if (cancelled) return
        setMemberNoLoading(false)
      }
    }, 300)

    return () => {
      cancelled = true
      window.clearTimeout(id)
    }
  }, [phone, form])

  React.useEffect(() => {
    const p = String(phone ?? '').replace(/\D/g, '')
    const m = String(memberNo ?? '').trim()
    const w = String(webId ?? '').trim()

    // 최소 입력값 조건: phone은 4자리 이상(또는 비어있음), memberNo는 비어있을 수 있음, webId는 비어있을 수 있음
    if (!m && p.length < 4 && !w) {
      form.setFields([
        { name: 'memberNo', errors: [] },
        { name: 'phoneNumber', errors: [] },
        { name: 'webId', errors: [] },
      ])
      return
    }

    let cancelled = false
    const id = window.setTimeout(async () => {
      try {
        const res = await api.get<{ memberNoDuplicated: boolean; phoneNumberDuplicated: boolean; webIdDuplicated: boolean }>(
          '/api/v1/members/duplicate-check',
          { params: { memberNo: m || undefined, phoneNumber: p || undefined, webId: w || undefined } }
        )
        if (cancelled) return
        form.setFields([
          { name: 'memberNo', errors: res.data.memberNoDuplicated ? ['이미 사용 중인 회원번호입니다.'] : [] },
          { name: 'phoneNumber', errors: res.data.phoneNumberDuplicated ? ['이미 등록된 휴대폰 번호입니다.'] : [] },
          { name: 'webId', errors: res.data.webIdDuplicated ? ['이미 사용 중인 WEB ID입니다.'] : [] },
        ])
      } catch (e: any) {
        if (cancelled) return
        // 중복체크 실패는 폼 오류로 막지 않고, 토스트만 노출
        message.error(e?.response?.data?.message ?? e?.message ?? '중복 체크 실패')
      }
    }, 350)

    return () => {
      cancelled = true
      window.clearTimeout(id)
    }
  }, [phone, memberNo, webId, form])

  const [addressSearchOpen, setAddressSearchOpen] = React.useState(false)
  const [addressKeyword, setAddressKeyword] = React.useState('')
  const [addressSearchLoading, setAddressSearchLoading] = React.useState(false)
  const [addressSearchResults, setAddressSearchResults] = React.useState<JusoSearchItem[]>([])
  const [addressSearchError, setAddressSearchError] = React.useState<string | null>(null)

  const openAddressSearch = () => {
    setAddressSearchOpen(true)
    setAddressKeyword('')
    setAddressSearchResults([])
    setAddressSearchError(null)
  }

  const searchAddress = async () => {
    const k = addressKeyword.trim()
    if (k.length < 2) {
      message.warning('검색어는 두 글자 이상 입력해주세요.')
      return
    }
    setAddressSearchLoading(true)
    setAddressSearchError(null)
    try {
      const res = await api.get<{
        results?: {
          common?: { errorCode?: string; errorMessage?: string }
          juso?: JusoSearchItem | JusoSearchItem[]
        }
      }>('/api/v1/public/juso/search', { params: { keyword: k, currentPage: 1, countPerPage: 20 } })
      const common = res.data?.results?.common
      const errCode = common?.errorCode
      if (errCode && errCode !== '0') {
        setAddressSearchError(common?.errorMessage ?? '검색 실패')
        setAddressSearchResults([])
        return
      }
      const juso = res.data?.results?.juso
      const list = Array.isArray(juso) ? juso : juso ? [juso] : []
      setAddressSearchResults(list)
      if (list.length === 0) setAddressSearchError('검색 결과가 없습니다.')
    } catch (e: any) {
      setAddressSearchError(e?.response?.data?.message ?? e?.message ?? '주소 검색 실패')
      setAddressSearchResults([])
    } finally {
      setAddressSearchLoading(false)
    }
  }

  const selectAddress = (item: JusoSearchItem) => {
    applyAddressData({
      zipCode: item.zipNo ?? '',
      roadAddress: item.roadAddrPart1 ?? item.roadAddr ?? '',
      jibunAddress: item.jibunAddr ?? '',
      buildingName: item.bdNm ?? '',
      detailAddress: form.getFieldValue(['address', 'detailAddress']) ?? '',
    })
    setAddressSearchOpen(false)
    message.success('주소가 적용되었습니다.')
  }

  const onFinish = async (v: FormValues) => {
    setLoading(true)
    try {
      const joinedAt = v.joinedAt?.format?.('YYYY-MM-DD')
      const birthDate = v.birthDate?.format?.('YYYY-MM-DD')
      const anniversaries = v.anniversaries?.format?.('YYYY-MM-DD')
      const addr = v.address
      const hasAddress = addr?.zipCode?.trim() && addr?.roadAddress?.trim()
      await api.post('/api/v1/members', {
        memberNo: v.memberNo.trim(),
        name: v.name.trim(),
        birthDate: birthDate ?? null,
        calendarType: v.calendarType ?? null,
        gender: v.gender ?? null,
        anniversaries: anniversaries ?? null,
        phoneNumber: v.phoneNumber?.trim() ? v.phoneNumber.trim() : null,
        ci: v.ci?.trim() ? v.ci.trim() : null,
        email: v.email?.trim() ? v.email.trim() : null,
        webId: v.webId?.trim() ? v.webId.trim() : null,
        joinedAt: joinedAt ?? null,
        statusCode: v.statusCode ?? DEFAULT_STATUS,
        address: hasAddress
          ? {
              zipCode: addr!.zipCode.trim(),
              roadAddress: addr!.roadAddress.trim(),
              jibunAddress: addr!.jibunAddress?.trim() || undefined,
              detailAddress: addr!.detailAddress?.trim() || undefined,
              buildingName: addr!.buildingName?.trim() || undefined,
              siDo: addr!.siDo?.trim() || undefined,
              siGunGu: addr!.siGunGu?.trim() || undefined,
              eupMyeonDong: addr!.eupMyeonDong?.trim() || undefined,
              legalDongCode: addr!.legalDongCode?.trim() || undefined,
            }
          : null,
      })
      message.success('회원이 등록되었습니다.')
      nav(`/members/${encodeURIComponent(v.memberNo.trim())}`, { replace: true })
    } catch (e: any) {
      const data = e?.response?.data
      const msg = data?.message ?? data?.detail ?? e?.message ?? '회원 등록 실패'
      message.error(msg)
      if (String(msg).toLowerCase().includes('memberno') && String(msg).toLowerCase().includes('exists')) {
        // 동시 등록으로 memberNo가 선점되면 자동으로 다시 제안 값을 받아서 갱신합니다.
        const digits = String(form.getFieldValue('phoneNumber') ?? '').replace(/\D/g, '')
        if (digits.length >= 4) {
          try {
            const res = await api.get<{ memberNo: string }>('/api/v1/members/member-no/suggest', { params: { phoneNumber: digits } })
            form.setFieldsValue({ memberNo: res.data.memberNo })
          } catch {
            // ignore
          }
        }
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <PageShell
      title="회원등록"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          수기로 회원을 등록합니다.
        </Typography.Text>
      }
    >
      <Card>
        <Form<FormValues>
          form={form}
          layout="vertical"
          onFinish={onFinish}
          initialValues={{
            memberNo: '',
            name: '',
            birthDate: undefined,
            calendarType: undefined,
            gender: undefined,
            phoneNumber: '',
            email: '',
            webId: '',
            joinedAt: dayjs(),
            statusCode: DEFAULT_STATUS,
            address: INITIAL_ADDRESS,
          }}
          requiredMark={false}
        >
          <Space direction="vertical" size={16} style={{ width: '100%' }}>
            <Space wrap size={16} align="start">
              <Form.Item label="회원번호" name="memberNo" rules={[{ required: true, message: '회원번호를 입력하세요' }]}>
                <Input placeholder="휴대폰번호 입력 시 자동 생성" style={{ width: 240 }} disabled readOnly />
              </Form.Item>
            </Space>

            <Space wrap size={16} align="start">
              <Form.Item label="이름" name="name" rules={[{ required: true, message: '이름을 입력하세요' }]}>
                <Input placeholder="예: 홍길동" style={{ width: 220 }} />
              </Form.Item>

              <Form.Item label="생년월일" name="birthDate">
                <DatePicker style={{ width: 180 }} placeholder="선택" />
              </Form.Item>

              <Form.Item label="양/음력" name="calendarType">
                <Select
                  style={{ width: 120 }}
                  placeholder="선택"
                  allowClear
                  options={[
                    { value: 'SOLAR', label: '양력' },
                    { value: 'LUNAR', label: '음력' },
                  ]}
                />
              </Form.Item>

              <Form.Item label="기념일(선택)" name="anniversaries">
                <DatePicker style={{ width: 180 }} placeholder="선택" />
              </Form.Item>

              <Form.Item label="성별" name="gender">
                <Select
                  style={{ width: 120 }}
                  placeholder="선택"
                  allowClear
                  options={[
                    { value: 'MALE', label: '남성' },
                    { value: 'FEMALE', label: '여성' },
                    { value: 'UNKNOWN', label: '미선택' },
                  ]}
                />
              </Form.Item>
            </Space>

            <Space wrap size={16} align="start">
              <Form.Item
                label="휴대폰번호"
                name="phoneNumber"
                rules={[{ required: true, message: '휴대폰 번호를 입력하세요' }]}
                getValueFromEvent={(e) => String(e?.target?.value ?? '').replace(/\D/g, '')}
              >
                <Input placeholder="01000000000" style={{ width: 220 }} allowClear inputMode="numeric" suffix={memberNoLoading ? '...' : undefined} />
              </Form.Item>

              <Form.Item label="휴대폰번호 인증(선택)">
                <Space>
                  <Form.Item name="ci" noStyle>
                    <Input style={{ display: 'none' }} />
                  </Form.Item>
                  <Button type="default" onClick={() => message.info('휴대폰번호 인증 기능은 준비 중입니다.')}>
                    인증하기
                  </Button>
                </Space>
              </Form.Item>

              <Form.Item
                label="이메일"
                name="email"
                rules={[{ type: 'email', message: '올바른 이메일 형식을 입력하세요' }]}
              >
                <Input placeholder="예: user@example.com" style={{ width: 240 }} allowClear />
              </Form.Item>

              <Form.Item
                label="WEB ID"
                name="webId"
                rules={[
                  {
                    pattern: /^[A-Za-z0-9_-]+$/,
                    message: 'WEB ID는 영문/숫자와 -, _ 만 사용할 수 있습니다.',
                  },
                ]}
                validateTrigger={['onChange', 'onBlur']}
              >
                <Input placeholder="예: web_123" style={{ width: 220 }} allowClear />
              </Form.Item>

              <Form.Item label="가입일" name="joinedAt">
                <DatePicker style={{ width: 180 }} disabled inputReadOnly />
              </Form.Item>

              <Form.Item label="상태" name="statusCode">
                <Select
                  style={{ width: 200 }}
                  loading={statusCodes.isLoading}
                  disabled
                  options={(statusCodes.data ?? []).map((c) => ({ value: c.code, label: c.name }))}
                />
              </Form.Item>
            </Space>
          </Space>

          <Form.Item label="주소(선택)">
            <Space orientation="vertical" style={{ width: '100%' }} size={8}>
              <Space.Compact>
                <Form.Item name={['address', 'zipCode']} noStyle>
                  <Input placeholder="우편번호" readOnly style={{ width: 140 }} />
                </Form.Item>
                <Button type="default" onClick={openAddressSearch}>
                  주소 검색
                </Button>
              </Space.Compact>
              <Form.Item name={['address', 'roadAddress']} noStyle>
                <Input placeholder="도로명주소" readOnly style={{ width: 400 }} />
              </Form.Item>
              <Form.Item name={['address', 'jibunAddress']} noStyle>
                <Input placeholder="지번주소" readOnly style={{ width: 400 }} />
              </Form.Item>
              <Form.Item name={['address', 'detailAddress']} noStyle>
                <Input placeholder="상세주소 (직접 입력)" allowClear style={{ width: 400 }} />
              </Form.Item>
            </Space>
          </Form.Item>

          <Modal
            title="주소 검색"
            open={addressSearchOpen}
            onCancel={() => setAddressSearchOpen(false)}
            footer={null}
            width={560}
            destroyOnHidden
          >
            <Space orientation="vertical" style={{ width: '100%' }} size={12}>
              <Space.Compact style={{ width: '100%' }}>
                <Input
                  placeholder="도로명, 지번, 건물명 검색 (2글자 이상)"
                  value={addressKeyword}
                  onChange={(e) => setAddressKeyword(e.target.value)}
                  onPressEnter={searchAddress}
                  allowClear
                  style={{ flex: 1 }}
                />
                <Button type="primary" onClick={searchAddress} loading={addressSearchLoading}>
                  조회
                </Button>
              </Space.Compact>
              {addressSearchError && (
                <Typography.Text type="danger">{addressSearchError}</Typography.Text>
              )}
              <div style={{ maxHeight: 320, overflow: 'auto' }}>
                {addressSearchLoading ? (
                  <Typography.Text type="secondary">검색 중...</Typography.Text>
                ) : addressSearchResults.length === 0 ? (
                  <Typography.Text type="secondary">검색어를 입력하고 조회를 눌러주세요.</Typography.Text>
                ) : (
                  addressSearchResults.map((item, idx) => (
                    <div
                      key={idx}
                      role="button"
                      tabIndex={0}
                      style={{
                        padding: '10px 12px',
                        cursor: 'pointer',
                        borderBottom: '1px solid #f0f0f0',
                      }}
                      onClick={() => selectAddress(item)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter' || e.key === ' ') {
                          e.preventDefault()
                          selectAddress(item)
                        }
                      }}
                    >
                      <div>
                        <Typography.Text strong>[{item.zipNo}]</Typography.Text>{' '}
                        {item.roadAddrPart1}
                        {item.roadAddrPart2 ? ` ${item.roadAddrPart2}` : ''}
                      </div>
                      {item.jibunAddr && (
                        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                          지번: {item.jibunAddr}
                        </Typography.Text>
                      )}
                    </div>
                  ))
                )}
              </div>
            </Space>
          </Modal>

          <Space>
            <Button type="primary" htmlType="submit" loading={loading}>
              등록
            </Button>
            <Button
              onClick={() => {
                form.resetFields()
              }}
            >
              초기화
            </Button>
            <Button onClick={() => nav('/members')}>목록으로</Button>
          </Space>
        </Form>
      </Card>
    </PageShell>
  )
}

