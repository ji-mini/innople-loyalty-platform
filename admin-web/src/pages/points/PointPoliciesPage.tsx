import { Button, Card, Form, Input, InputNumber, Modal, Select, Space, Switch, Table, Tag, Typography, message } from 'antd'
import { useQuery } from '@tanstack/react-query'
import React from 'react'
import { PageShell } from '../common/PageShell'
import { api } from '../../shared/api'
import { atLeast } from '../../shared/roles'
import { getSession } from '../../shared/storage'

type PointPolicyRow = {
  id: string
  pointType: 'BASIC' | 'EVENT'
  name: string
  validityDays: number
  enabled: boolean
  description: string | null
  createdAt: string
  updatedAt: string
}

export function PointPoliciesPage() {
  const role = getSession()?.role ?? 'OPERATOR'
  const canEdit = atLeast(role, 'SUPER_ADMIN')
  const [open, setOpen] = React.useState(false)
  const [editing, setEditing] = React.useState<PointPolicyRow | null>(null)
  const [saving, setSaving] = React.useState(false)
  const [form] = Form.useForm<{
    pointType: PointPolicyRow['pointType']
    name: string
    validityDays: number
    enabled: boolean
    description?: string
  }>()

  const q = useQuery({
    queryKey: ['admin', 'point-policies'],
    queryFn: async () => {
      const res = await api.get('/api/v1/admin/point-policies')
      return (res.data ?? []) as PointPolicyRow[]
    },
  })

  const rows = q.data ?? []

  const openCreate = () => {
    setEditing(null)
    form.resetFields()
    form.setFieldsValue({ pointType: 'BASIC', name: '', validityDays: 365, enabled: true, description: '' })
    setOpen(true)
  }

  const openEdit = (r: PointPolicyRow) => {
    setEditing(r)
    form.setFieldsValue({
      pointType: r.pointType,
      name: r.name,
      validityDays: r.validityDays,
      enabled: r.enabled,
      description: r.description ?? '',
    })
    setOpen(true)
  }

  const onSubmit = async () => {
    if (!canEdit) return
    const v = await form.validateFields()
    setSaving(true)
    try {
      if (editing) {
        await api.put(`/api/v1/admin/point-policies/${encodeURIComponent(editing.id)}`, v)
        message.success('정책이 수정되었습니다.')
      } else {
        await api.post('/api/v1/admin/point-policies', v)
        message.success('정책이 생성되었습니다.')
      }
      setOpen(false)
      setEditing(null)
      await q.refetch()
    } catch (e: any) {
      message.error(e?.response?.data?.message ?? e?.message ?? '저장 실패')
    } finally {
      setSaving(false)
    }
  }

  return (
    <PageShell
      title="포인트 정책관리"
      extra={
        <Space size={12} wrap>
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            테넌트별 포인트 유형/유효기간 등을 관리합니다.
          </Typography.Text>
          {canEdit ? (
            <Button size="small" type="primary" onClick={openCreate}>
              정책 추가
            </Button>
          ) : null}
        </Space>
      }
    >
      <Card>
        <Table<PointPolicyRow>
          rowKey={(r) => r.id}
          dataSource={rows}
          loading={q.isLoading}
          pagination={{ pageSize: 20 }}
          columns={[
            {
              title: '포인트 유형',
              dataIndex: 'pointType',
              width: 140,
              render: (v: PointPolicyRow['pointType']) => <Tag>{v}</Tag>,
            },
            {
              title: '정책명',
              dataIndex: 'name',
              width: 260,
            },
            {
              title: '유효기간(일)',
              dataIndex: 'validityDays',
              width: 140,
              render: (v: number) => v,
            },
            {
              title: '사용여부',
              dataIndex: 'enabled',
              width: 140,
              render: (v: boolean) => <Switch checked={v} disabled />,
            },
            {
              title: '설명',
              dataIndex: 'description',
              render: (v: string | null) => v ?? '-',
            },
            {
              title: '수정일시',
              dataIndex: 'updatedAt',
              width: 190,
            },
            ...(canEdit
              ? [
                  {
                    title: '관리',
                    key: 'actions',
                    width: 110,
                    render: (_: any, r: PointPolicyRow) => (
                      <Button size="small" onClick={() => openEdit(r)}>
                        수정
                      </Button>
                    ),
                  },
                ]
              : []),
          ]}
          locale={{
            emptyText: (
              <Space direction="vertical" size={6}>
                <Typography.Text>정책 데이터가 없습니다.</Typography.Text>
                <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                  “정책 추가”로 테넌트별 포인트 유형/유효기간을 먼저 등록하세요.
                </Typography.Text>
              </Space>
            ),
          }}
        />
      </Card>

      <Modal
        open={open}
        title={editing ? '포인트 정책 수정' : '포인트 정책 추가'}
        okText={editing ? '수정' : '생성'}
        onOk={onSubmit}
        confirmLoading={saving}
        onCancel={() => {
          setOpen(false)
          setEditing(null)
        }}
        destroyOnClose
      >
        <Form form={form} layout="vertical" requiredMark={false}>
          <Form.Item label="포인트 유형" name="pointType" rules={[{ required: true, message: '포인트 유형을 선택하세요' }]}>
            <Select
              options={[
                { value: 'BASIC', label: 'BASIC(기본)' },
                { value: 'EVENT', label: 'EVENT(이벤트)' },
              ]}
            />
          </Form.Item>
          <Form.Item label="정책명" name="name" rules={[{ required: true, message: '정책명을 입력하세요' }]}>
            <Input placeholder="예: 기본 포인트" />
          </Form.Item>
          <Form.Item label="포인트 유효기간(일)" name="validityDays" rules={[{ required: true, message: '유효기간(일)을 입력하세요' }]}>
            <InputNumber min={1} step={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label="사용여부" name="enabled" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item label="설명(선택)" name="description">
            <Input.TextArea rows={3} placeholder="예: 가입 보상/정책 변경 이력 등" />
          </Form.Item>
        </Form>
      </Modal>
    </PageShell>
  )
}

