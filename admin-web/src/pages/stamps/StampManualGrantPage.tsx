import { Button, Card, Form, Input, InputNumber, Space, Typography, message } from 'antd'
import { useQueryClient } from '@tanstack/react-query'
import React from 'react'
import { useSearchParams } from 'react-router-dom'
import { api } from '../../shared/api'
import { PageShell } from '../common/PageShell'
import { atLeast } from '../../shared/roles'
import { getSession } from '../../shared/storage'

type FormValues = {
  memberNo: string
  stamps: number
  reason: string
}

export function StampManualGrantPage() {
  const role = getSession()?.role ?? 'OPERATOR'
  const canSubmit = atLeast(role, 'ADMIN')
  const [loading, setLoading] = React.useState(false)
  const [lookupLoading, setLookupLoading] = React.useState(false)
  const [member, setMember] = React.useState<{ id: string; memberNo: string; name: string } | null>(null)
  const [form] = Form.useForm<FormValues>()
  const [sp] = useSearchParams()
  const qc = useQueryClient()

  const onLookup = async () => {
    const memberNo = String(form.getFieldValue('memberNo') ?? '').trim()
    if (!memberNo) {
      message.error('회원번호를 입력하세요.')
      return
    }
    setLookupLoading(true)
    try {
      const res = await api.get(`/api/v1/members/${encodeURIComponent(memberNo)}`)
      const d = res.data as { id: string; memberNo: string; name: string }
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
    if (!canSubmit) return
    if (!member?.id) {
      message.error('먼저 회원번호를 조회하세요.')
      return
    }
    setLoading(true)
    try {
      const res = await api.post<{ ledgerId: string; currentBalance: number }>('/api/v1/admin/stamps/manual-grant', {
        memberId: member.id,
        stamps: v.stamps,
        reason: v.reason.trim(),
      })
      await qc.invalidateQueries({ queryKey: ['admin', 'stamp-ledgers'] })
      message.success(`스탬프 지급 완료. 현재 잔액 ${res.data.currentBalance}개`)
      form.setFieldsValue({ stamps: undefined, reason: '' })
    } catch (e: any) {
      message.error(e?.response?.data?.message ?? e?.message ?? '처리 실패')
    } finally {
      setLoading(false)
    }
  }

  return (
    <PageShell title="스탬프 수기 지급" extra={<Typography.Text type="secondary">관리자 사유가 원장에 남습니다.</Typography.Text>}>
      <Card style={{ maxWidth: 560 }}>
        <Form form={form} layout="vertical" onFinish={onFinish}>
          <Space.Compact style={{ width: '100%' }}>
            <Form.Item name="memberNo" label="회원번호" rules={[{ required: true }]} style={{ flex: 1, marginBottom: 8 }}>
              <Input placeholder="회원번호 입력 후 조회" />
            </Form.Item>
            <Button style={{ marginTop: 30 }} loading={lookupLoading} onClick={onLookup}>
              조회
            </Button>
          </Space.Compact>
          {member ? (
            <Typography.Paragraph style={{ marginBottom: 16 }}>
              <Typography.Text strong>{member.name}</Typography.Text> ({member.memberNo})
            </Typography.Paragraph>
          ) : null}
          <Form.Item name="stamps" label="지급 스탬프 수" rules={[{ required: true, message: '1 이상 입력' }]}>
            <InputNumber min={1} style={{ width: '100%' }} disabled={!canSubmit} />
          </Form.Item>
          <Form.Item name="reason" label="지급 사유" rules={[{ required: true, message: '사유를 입력하세요' }]}>
            <Input.TextArea rows={3} maxLength={500} showCount disabled={!canSubmit} />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={loading} disabled={!canSubmit || !member}>
            지급
          </Button>
        </Form>
      </Card>
    </PageShell>
  )
}
