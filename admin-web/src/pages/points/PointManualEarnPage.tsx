import { Button, Card, DatePicker, Form, Input, InputNumber, Select, Space, Typography, message } from 'antd'
import React from 'react'
import { useSearchParams } from 'react-router-dom'
import { api } from '../../shared/api'
import { useCommonCodes } from '../../shared/queries'
import { PageShell } from '../common/PageShell'

type FormValues = {
  memberNo: string
  amount: number
  expiresAt: any
  reasonCode: string
  reasonDetail?: string
}

export function PointManualEarnPage() {
  const [loading, setLoading] = React.useState(false)
  const [lookupLoading, setLookupLoading] = React.useState(false)
  const [member, setMember] = React.useState<{ id: string; memberNo: string; name: string } | null>(null)
  const [form] = Form.useForm<FormValues>()
  const [sp] = useSearchParams()
  const reasons = useCommonCodes('POINT_REASON')

  const onLookup = async () => {
    const memberNo = String(form.getFieldValue('memberNo') ?? '').trim()
    if (!memberNo) {
      message.error('회원번호를 입력하세요.')
      return
    }
    setLookupLoading(true)
    try {
      const res = await api.get(`/api/v1/members/${encodeURIComponent(memberNo)}`)
      const d = res.data as any
      setMember({ id: d.id, memberNo: d.memberNo, name: d.name })
      message.success(`회원 조회 완료: ${d.name} (${d.memberNo})`)
    } catch (e: any) {
      setMember(null)
      message.error(e?.response?.data?.message ?? e?.message ?? '회원 조회 실패')
    } finally {
      setLookupLoading(false)
    }
  }

  React.useEffect(() => {
    const m = sp.get('memberNo')
    if (!m) return
    form.setFieldsValue({ memberNo: m })
    onLookup()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const onFinish = async (v: FormValues) => {
    if (!member?.id) {
      message.error('먼저 회원번호를 조회하세요.')
      return
    }
    const expiresAtIso = v.expiresAt?.toISOString?.()
    if (!expiresAtIso) {
      message.error('만료일시를 선택하세요.')
      return
    }
    setLoading(true)
    try {
      const hit = (reasons.data ?? []).find((r) => r.code === v.reasonCode)
      const reason = hit ? `[${hit.code}] ${hit.name}${v.reasonDetail?.trim() ? ` - ${v.reasonDetail.trim()}` : ''}` : v.reasonCode
      await api.post('/api/v1/points/earn', {
        memberId: member.id,
        amount: v.amount,
        expiresAt: expiresAtIso,
        reason,
      })
      message.success('포인트 수기 등록(적립)이 완료되었습니다.')
    } catch (e: any) {
      message.error(e?.response?.data?.message ?? e?.message ?? '처리 실패')
    } finally {
      setLoading(false)
    }
  }

  return (
    <PageShell
      title="포인트 수기 등록"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          회원 선택 후 포인트를 수기로 적립합니다.
        </Typography.Text>
      }
    >
      <Card>
        <Form<FormValues>
          layout="vertical"
          form={form}
          onFinish={onFinish}
          initialValues={{ memberNo: '', amount: 0, expiresAt: null, reasonCode: 'CS_COMP', reasonDetail: '' }}
          requiredMark={false}
        >
          <Space size={16} wrap align="start">
            <Form.Item label="회원번호" name="memberNo" rules={[{ required: true, message: '회원번호를 입력하세요' }]}>
              <Input placeholder="예: 10000001" style={{ width: 260 }} />
            </Form.Item>
            <Form.Item label=" " colon={false}>
              <Button onClick={onLookup} loading={lookupLoading}>
                조회
              </Button>
            </Form.Item>
            <Form.Item label="회원" colon={false}>
              <Typography.Text type={member ? undefined : 'secondary'} style={{ display: 'block', minWidth: 220 }}>
                {member ? `${member.name} (${member.memberNo})` : '조회 필요'}
              </Typography.Text>
            </Form.Item>
          </Space>

          <Space size={16} wrap align="start">
            <Form.Item label="포인트 금액" name="amount" rules={[{ required: true, message: '포인트 금액을 입력하세요' }]}>
              <InputNumber min={1} step={1} style={{ width: 220 }} addonAfter="P" />
            </Form.Item>

            <Form.Item label="만료일시" name="expiresAt" rules={[{ required: true, message: '만료일시를 선택하세요' }]}>
              <DatePicker showTime style={{ width: 240 }} />
            </Form.Item>

            <Form.Item label="사유 템플릿" name="reasonCode" rules={[{ required: true, message: '사유 템플릿을 선택하세요' }]}>
              <Select
                placeholder="선택"
                loading={reasons.isLoading}
                style={{ width: 260 }}
                options={(reasons.data ?? []).map((c) => ({ value: c.code, label: `${c.code} (${c.name})` }))}
              />
            </Form.Item>

            <Form.Item label="상세 사유(선택)" name="reasonDetail">
              <Input placeholder="예: 고객 CS 보상 적립" style={{ width: 360, maxWidth: '100%' }} />
            </Form.Item>
          </Space>

          <Button type="primary" htmlType="submit" loading={loading}>
            등록
          </Button>
        </Form>
      </Card>
    </PageShell>
  )
}

