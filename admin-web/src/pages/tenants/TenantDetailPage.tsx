import { Button, Card, Form, Input, Space, Typography, message } from 'antd'
import { useQuery } from '@tanstack/react-query'
import React from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { api } from '../../shared/api'
import { PageShell } from '../common/PageShell'

type TenantDetail = {
  tenantId: string
  name: string
  representativeCode: string
  createdAt: string
  updatedAt: string
}

export function TenantDetailPage() {
  const nav = useNavigate()
  const params = useParams()
  const tenantId = params.tenantId ?? ''
  const [saving, setSaving] = React.useState(false)
  const [form] = Form.useForm<{ name: string; representativeCode: string }>()

  const q = useQuery({
    queryKey: ['admin', 'tenants', tenantId],
    enabled: !!tenantId,
    queryFn: async () => {
      const res = await api.get(`/api/v1/admin/tenants/${encodeURIComponent(tenantId)}`, {
        headers: { 'X-Tenant-Id': tenantId },
      })
      return res.data as TenantDetail
    },
  })

  React.useEffect(() => {
    if (!q.data) return
    form.setFieldsValue({ name: q.data.name, representativeCode: q.data.representativeCode })
  }, [form, q.data])

  const onSave = async () => {
    const v = await form.validateFields()
    setSaving(true)
    try {
      await api.put(
        `/api/v1/admin/tenants/${encodeURIComponent(tenantId)}`,
        { name: v.name, representativeCode: v.representativeCode },
        { headers: { 'X-Tenant-Id': tenantId } },
      )
      message.success('테넌트 정보가 수정되었습니다.')
      await q.refetch()
    } catch (e: any) {
      message.error(e?.response?.data?.message ?? e?.message ?? '저장 실패')
    } finally {
      setSaving(false)
    }
  }

  return (
    <PageShell
      title="테넌트 상세"
      extra={
        <Space>
          <Button onClick={() => nav('/tenants')}>목록</Button>
          <Button type="primary" onClick={onSave} loading={saving}>
            저장
          </Button>
        </Space>
      }
    >
      <Card loading={q.isLoading}>
        <Space direction="vertical" style={{ width: '100%' }} size={12}>
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            상세/수정 요청은 해당 테넌트의 `X-Tenant-Id` 컨텍스트로 처리됩니다.
          </Typography.Text>

          <Form form={form} layout="vertical" requiredMark={false}>
            <Form.Item label="Tenant ID" colon={false}>
              <Input value={tenantId} disabled />
            </Form.Item>
            <Form.Item label="테넌트명" name="name" rules={[{ required: true, message: '테넌트명을 입력하세요' }]}>
              <Input placeholder="예: SPAO" />
            </Form.Item>
            <Form.Item
              label="대표코드"
              name="representativeCode"
              rules={[{ required: true, message: '대표코드를 입력하세요' }]}
              getValueFromEvent={(e) => String(e?.target?.value ?? '').toUpperCase().replace(/[^A-Z]/g, '').slice(0, 2)}
            >
              <Input placeholder="예: SP" maxLength={2} />
            </Form.Item>
            <Form.Item label="생성일시" colon={false}>
              <Input value={q.data?.createdAt ?? '-'} disabled />
            </Form.Item>
            <Form.Item label="수정일시" colon={false}>
              <Input value={q.data?.updatedAt ?? '-'} disabled />
            </Form.Item>
          </Form>
        </Space>
      </Card>
    </PageShell>
  )
}

