import { Button, Card, DatePicker, Form, Input, InputNumber, Radio, Select, Space, Typography, message } from 'antd'
import { useQueryClient } from '@tanstack/react-query'
import React from 'react'
import { api } from '../../shared/api'
import { useCommonCodes } from '../../shared/queries'
import { PageShell } from '../common/PageShell'

type FormValues = {
  memberNo: string
  brand: string
  type: 'EARN' | 'USE' | 'EXPIRE'
  amount: number
  expiresAt?: any
  referenceAt?: any
  reason: string
  referenceType?: string
  referenceId?: string
}

export function PointManualPage() {
  const [loading, setLoading] = React.useState(false)
  const [lookupLoading, setLookupLoading] = React.useState(false)
  const [member, setMember] = React.useState<{ id: string; memberNo: string; name: string } | null>(null)
  const [form] = Form.useForm<FormValues>()
  const queryClient = useQueryClient()
  const referenceTypes = useCommonCodes('POINT_REFERENCE_TYPE')

  const onFinish = async (v: FormValues) => {
    if (!member?.id) {
      message.error('먼저 회원번호를 조회하세요.')
      return
    }
    setLoading(true)
    try {
      const reason = `[${v.brand}] ${v.reason ?? ''}`.trim()

      if (v.type === 'EARN') {
        const expiresAtIso = v.expiresAt?.toISOString?.()
        if (!expiresAtIso) {
          message.error('적립 유효기간(만료일시)을 선택하세요.')
          return
        }
        await api.post('/api/v1/points/earn', {
          memberId: member.id,
          amount: v.amount,
          expiresAt: expiresAtIso,
          reason,
          referenceType: v.referenceType || null,
          referenceId: v.referenceId?.trim() || null,
        })
        message.success('포인트 적립이 완료되었습니다.')
      } else if (v.type === 'USE') {
        await api.post('/api/v1/points/use', {
          memberId: member.id,
          amount: v.amount,
          reason,
          referenceType: v.referenceType || null,
          referenceId: v.referenceId?.trim() || null,
        })
        message.success('포인트 사용이 완료되었습니다.')
      } else {
        const referenceAtIso = v.referenceAt?.toISOString?.()
        await api.post('/api/v1/points/expire/manual', {
          memberId: member.id,
          referenceAt: referenceAtIso,
          reason,
          referenceType: v.referenceType || null,
          referenceId: v.referenceId?.trim() || null,
        })
        message.success('포인트 소멸(만료분)이 처리되었습니다.')
      }
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['members', 'detail', member.memberNo] }),
        queryClient.invalidateQueries({ queryKey: ['points', 'ledgers', member.memberNo] }),
        queryClient.invalidateQueries({ queryKey: ['points', 'ledgers'] }),
        queryClient.invalidateQueries({ queryKey: ['members', 'list'] }),
        queryClient.invalidateQueries({ queryKey: ['dashboard'] }),
      ])
    } finally {
      setLoading(false)
    }
  }

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

  return (
    <PageShell
      title="포인트 수기등록"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          운영자가 직접 적립/사용/소멸 처리를 합니다.
        </Typography.Text>
      }
    >
      <Card>
        <Form<FormValues>
          layout="vertical"
          form={form}
          onFinish={onFinish}
          initialValues={{
            memberNo: '',
            brand: '',
            type: 'EARN',
            amount: 0,
            expiresAt: null,
            referenceAt: null,
            reason: '',
            referenceType: undefined,
            referenceId: '',
          }}
          requiredMark={false}
        >
          <Space size={16} wrap align="start">
            <Form.Item label="회원번호" name="memberNo" rules={[{ required: true, message: '회원번호를 입력하세요' }]}>
              <Input placeholder="예: 12345" style={{ width: 260 }} />
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

            <Form.Item label="브랜드" name="brand" rules={[{ required: true, message: '브랜드를 선택하세요' }]}>
              <Select
                placeholder="브랜드 선택"
                style={{ width: 220 }}
                options={[
                  { value: 'SPAO', label: 'SPAO' },
                  { value: 'MIXXO', label: 'MIXXO' },
                ]}
              />
            </Form.Item>

            <Form.Item label="처리구분" name="type" rules={[{ required: true }]}>
              <Radio.Group
                options={[
                  { value: 'EARN', label: '적립' },
                  { value: 'USE', label: '사용' },
                  { value: 'EXPIRE', label: '소멸' },
                ]}
              />
            </Form.Item>
          </Space>

          <Space size={16} wrap align="start">
            <Form.Item noStyle shouldUpdate={(prev, cur) => prev.type !== cur.type}>
              {({ getFieldValue }) => (
                <Form.Item
                  label="포인트 금액"
                  name="amount"
                  rules={[{ required: true, message: '포인트 금액을 입력하세요' }]}
                  extra={getFieldValue('type') === 'EXPIRE' ? '소멸은 기준일시 기준으로 만료분 전체가 처리됩니다.' : undefined}
                >
                  <InputNumber min={1} step={1} style={{ width: 220 }} addonAfter="P" disabled={getFieldValue('type') === 'EXPIRE'} />
                </Form.Item>
              )}
            </Form.Item>

            <Form.Item noStyle shouldUpdate={(prev, cur) => prev.type !== cur.type}>
              {({ getFieldValue }) => {
                const type = getFieldValue('type') as FormValues['type']
                if (type === 'EARN') {
                  return (
                    <Form.Item label="만료일시" name="expiresAt" rules={[{ required: true, message: '만료일시를 선택하세요' }]}>
                      <DatePicker showTime style={{ width: 240 }} />
                    </Form.Item>
                  )
                }
                if (type === 'EXPIRE') {
                  return (
                    <Form.Item label="기준일시(선택)" name="referenceAt">
                      <DatePicker showTime style={{ width: 240 }} />
                    </Form.Item>
                  )
                }
                return null
              }}
            </Form.Item>

            <Form.Item label="처리사유" name="reason" rules={[{ required: true, message: '처리사유를 입력하세요' }]}>
              <Input placeholder="예: 고객 CS 보상" style={{ width: 420, maxWidth: '100%' }} />
            </Form.Item>
          </Space>

          <Space size={16} wrap align="start">
            <Form.Item label="참조유형(선택)" name="referenceType">
              <Select
                placeholder="선택"
                allowClear
                loading={referenceTypes.isLoading}
                style={{ width: 220 }}
                options={(referenceTypes.data ?? []).map((c) => ({ value: c.code, label: `${c.code} (${c.name})` }))}
              />
            </Form.Item>
            <Form.Item label="참조ID(선택)" name="referenceId">
              <Input placeholder="예: ORDER-20260324-001" style={{ width: 280 }} />
            </Form.Item>
          </Space>

          <Button type="primary" htmlType="submit" loading={loading}>
            처리
          </Button>
        </Form>
      </Card>
    </PageShell>
  )
}

