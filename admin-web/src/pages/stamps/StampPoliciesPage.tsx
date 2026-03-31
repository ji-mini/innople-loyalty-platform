import { Button, Card, Form, Input, InputNumber, Modal, Select, Space, Switch, Table, Tag, Typography, message } from 'antd'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import React from 'react'
import { api } from '../../shared/api'
import { PageShell } from '../common/PageShell'
import { atLeast } from '../../shared/roles'
import { getSession } from '../../shared/storage'

type IssuanceMode = 'AUTO' | 'MANUAL'

type CouponTemplate = {
  id: string
  name: string
  description: string | null
  active: boolean
  createdAt?: string
  updatedAt?: string
}

function formatCouponDt(v: string | null | undefined): string {
  if (!v) return '-'
  try {
    return new Date(v).toLocaleString('ko-KR')
  } catch {
    return v
  }
}

type StampPolicy = {
  id: string
  name: string
  amountWonPerStamp: number
  stampsRequiredForCoupon: number
  couponTemplateId: string
  couponTemplateName: string
  issuanceMode: IssuanceMode
  active: boolean
}

export function StampPoliciesPage() {
  const role = getSession()?.role ?? 'OPERATOR'
  const canEdit = atLeast(role, 'ADMIN')
  const qc = useQueryClient()
  const [open, setOpen] = React.useState(false)
  const [tplListOpen, setTplListOpen] = React.useState(false)
  const [tplOpen, setTplOpen] = React.useState(false)
  const [editing, setEditing] = React.useState<StampPolicy | null>(null)
  const [saving, setSaving] = React.useState(false)
  const [form] = Form.useForm<{
    name: string
    amountWonPerStamp: number
    stampsRequiredForCoupon: number
    couponTemplateId: string
    issuanceMode: IssuanceMode
    active: boolean
  }>()
  const [tplForm] = Form.useForm<{ name: string; description?: string; active: boolean }>()

  const policiesQ = useQuery({
    queryKey: ['admin', 'stamp-policies'],
    queryFn: async () => {
      const res = await api.get<StampPolicy[]>('/api/v1/admin/stamp-policies')
      return res.data ?? []
    },
  })

  const templatesQ = useQuery({
    queryKey: ['admin', 'coupon-templates'],
    queryFn: async () => {
      const res = await api.get<CouponTemplate[]>('/api/v1/admin/stamp-policies/coupon-templates')
      return res.data ?? []
    },
  })

  const openCreate = () => {
    setEditing(null)
    form.resetFields()
    form.setFieldsValue({
      name: '',
      amountWonPerStamp: 10000,
      stampsRequiredForCoupon: 10,
      couponTemplateId: templatesQ.data?.find((t) => t.active)?.id,
      issuanceMode: 'AUTO',
      active: true,
    })
    setOpen(true)
  }

  const openEdit = (r: StampPolicy) => {
    setEditing(r)
    form.setFieldsValue({
      name: r.name,
      amountWonPerStamp: r.amountWonPerStamp,
      stampsRequiredForCoupon: r.stampsRequiredForCoupon,
      couponTemplateId: r.couponTemplateId,
      issuanceMode: r.issuanceMode,
      active: r.active,
    })
    setOpen(true)
  }

  const errMsg = (e: unknown) => {
    const any = e as { response?: { data?: { message?: string } }; message?: string }
    return any?.response?.data?.message ?? any?.message ?? '요청 실패'
  }

  const onSubmit = async () => {
    if (!canEdit) {
      message.warning('수정 권한이 없습니다.')
      throw new Error('FORBIDDEN')
    }
    const v = await form.validateFields()
    setSaving(true)
    try {
      if (editing) {
        await api.put(`/api/v1/admin/stamp-policies/${encodeURIComponent(editing.id)}`, v)
        message.success('스탬프 정책이 수정되었습니다.')
      } else {
        await api.post('/api/v1/admin/stamp-policies', v)
        message.success('스탬프 정책이 등록되었습니다.')
      }
      setOpen(false)
      setEditing(null)
      await qc.invalidateQueries({ queryKey: ['admin', 'stamp-policies'] })
    } catch (e: any) {
      message.error(errMsg(e))
      throw e
    } finally {
      setSaving(false)
    }
  }

  const onCreateTemplate = async () => {
    if (!canEdit) {
      message.warning('등록 권한이 없습니다.')
      throw new Error('FORBIDDEN')
    }
    const v = await tplForm.validateFields()
    setSaving(true)
    try {
      await api.post('/api/v1/admin/stamp-policies/coupon-templates', {
        name: v.name.trim(),
        description: v.description?.trim() || null,
        active: v.active ?? true,
      })
      message.success('쿠폰 템플릿이 등록되었습니다.')
      setTplOpen(false)
      tplForm.resetFields()
      await qc.invalidateQueries({ queryKey: ['admin', 'coupon-templates'] })
    } catch (e: any) {
      message.error(errMsg(e))
      throw e
    } finally {
      setSaving(false)
    }
  }

  const rows = policiesQ.data ?? []
  const templateOptions = (templatesQ.data ?? []).filter((t) => t.active)

  return (
    <PageShell
      title="스탬프 정책 관리"
      extra={
        <Space>
          {canEdit ? (
            <Button type="primary" onClick={openCreate} disabled={templateOptions.length === 0}>
              정책 등록
            </Button>
          ) : null}
        </Space>
      }
    >
      <Card style={{ marginBottom: 16 }}>
        <Typography.Paragraph type="secondary" style={{ marginBottom: 8 }}>
          활성 정책은 테넌트당 1개입니다. 금액 기준 적립·보상 쿠폰 템플릿·발급 방식(AUTO/MANUAL)을 설정합니다. 정책 등록 전에{' '}
          <Typography.Text strong>쿠폰 템플릿</Typography.Text>이 필요합니다.
        </Typography.Paragraph>
        {canEdit ? (
          <Space wrap>
            <Button
              onClick={() => {
                setTplListOpen(true)
                void templatesQ.refetch()
              }}
            >
              쿠폰 템플릿 보기
            </Button>
            <Button onClick={() => setTplOpen(true)}>쿠폰 템플릿 추가</Button>
            {templateOptions.length === 0 ? (
              <Typography.Text type="warning">활성 쿠폰 템플릿이 없으면 정책을 등록할 수 없습니다.</Typography.Text>
            ) : null}
          </Space>
        ) : null}
      </Card>

      <Card loading={policiesQ.isLoading}>
        <Table<StampPolicy>
          rowKey={(r) => r.id}
          pagination={false}
          dataSource={rows}
          columns={[
            { title: '정책명', dataIndex: 'name', width: 160 },
            {
              title: 'N원당 1스탬프',
              dataIndex: 'amountWonPerStamp',
              width: 140,
              render: (v: number) => `${v.toLocaleString('ko-KR')}원`,
            },
            { title: '쿠폰까지 스탬프', dataIndex: 'stampsRequiredForCoupon', width: 130 },
            { title: '쿠폰 템플릿', dataIndex: 'couponTemplateName', ellipsis: true },
            {
              title: '발급',
              dataIndex: 'issuanceMode',
              width: 100,
              render: (m: IssuanceMode) =>
                m === 'AUTO' ? <Tag color="green">자동</Tag> : <Tag color="blue">고객수령</Tag>,
            },
            {
              title: '활성',
              dataIndex: 'active',
              width: 80,
              render: (a: boolean) => (a ? <Tag color="success">Y</Tag> : <Tag>No</Tag>),
            },
            ...(canEdit
              ? [
                  {
                    title: '',
                    key: 'actions',
                    width: 100,
                    render: (_: unknown, r: StampPolicy) => (
                      <Button type="link" size="small" onClick={() => openEdit(r)}>
                        수정
                      </Button>
                    ),
                  },
                ]
              : []),
          ]}
        />
      </Card>

      <Modal
        title={editing ? '스탬프 정책 수정' : '스탬프 정책 등록'}
        open={open}
        onCancel={() => setOpen(false)}
        onOk={onSubmit}
        confirmLoading={saving}
        okButtonProps={{ disabled: !canEdit }}
        width={560}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 12 }}>
          <Form.Item name="name" label="정책명" rules={[{ required: true, message: '정책명을 입력하세요' }]}>
            <Input placeholder="예: 카페 기본 스탬프" maxLength={200} />
          </Form.Item>
          <Form.Item
            name="amountWonPerStamp"
            label="구매 금액당 스탬프 1개 (원)"
            rules={[{ required: true, message: '금액을 입력하세요' }]}
          >
            <InputNumber min={1} style={{ width: '100%' }} placeholder="예: 10000" />
          </Form.Item>
          <Form.Item
            name="stampsRequiredForCoupon"
            label="쿠폰 1장에 필요한 스탬프 개수"
            rules={[{ required: true, message: '개수를 입력하세요' }]}
          >
            <InputNumber min={1} style={{ width: '100%' }} placeholder="예: 10" />
          </Form.Item>
          <Form.Item name="couponTemplateId" label="보상 쿠폰 템플릿" rules={[{ required: true, message: '템플릿을 선택하세요' }]}>
            <Select
              options={templateOptions.map((t) => ({ value: t.id, label: t.name }))}
              placeholder="쿠폰 템플릿"
              showSearch
              optionFilterProp="label"
            />
          </Form.Item>
          <Form.Item name="issuanceMode" label="쿠폰 발급 방식" rules={[{ required: true }]}>
            <Select
              options={[
                { value: 'AUTO', label: 'AUTO — 조건 충족 시 자동 발급(스탬프 차감)' },
                { value: 'MANUAL', label: 'MANUAL — 고객이 받기 전까지 스탬프 유지' },
              ]}
            />
          </Form.Item>
          <Form.Item name="active" label="활성" valuePropName="checked">
            <Switch checkedChildren="활성" unCheckedChildren="비활성" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="등록된 쿠폰 템플릿"
        open={tplListOpen}
        onCancel={() => setTplListOpen(false)}
        footer={
          <Button type="primary" onClick={() => setTplListOpen(false)}>
            닫기
          </Button>
        }
        width={920}
        styles={{ body: { maxHeight: '70vh', overflow: 'auto', paddingTop: 8 } }}
      >
        <Table<CouponTemplate>
          rowKey={(r) => r.id}
          size="small"
          pagination={false}
          loading={templatesQ.isLoading || templatesQ.isFetching}
          dataSource={templatesQ.data ?? []}
          locale={{
            emptyText: templatesQ.isError ? '목록을 불러오지 못했습니다.' : '등록된 쿠폰 템플릿이 없습니다.',
          }}
          columns={[
            { title: '이름', dataIndex: 'name', width: 180, ellipsis: true },
            {
              title: '설명',
              dataIndex: 'description',
              ellipsis: true,
              render: (v: string | null) => v?.trim() || '—',
            },
            {
              title: '상태',
              dataIndex: 'active',
              width: 88,
              render: (active: boolean) => (active ? <Tag color="green">활성</Tag> : <Tag>비활성</Tag>),
            },
            { title: '등록일시', dataIndex: 'createdAt', width: 168, render: (v: string | undefined) => formatCouponDt(v) },
            { title: '수정일시', dataIndex: 'updatedAt', width: 168, render: (v: string | undefined) => formatCouponDt(v) },
            {
              title: 'ID',
              dataIndex: 'id',
              width: 260,
              ellipsis: true,
              render: (id: string) => (
                <Typography.Paragraph copyable={{ text: id }} style={{ marginBottom: 0, fontSize: 12 }}>
                  {id}
                </Typography.Paragraph>
              ),
            },
          ]}
        />
      </Modal>

      <Modal title="쿠폰 템플릿 추가" open={tplOpen} onCancel={() => setTplOpen(false)} onOk={onCreateTemplate} confirmLoading={saving}>
        <Form form={tplForm} layout="vertical" style={{ marginTop: 12 }} initialValues={{ active: true }}>
          <Form.Item name="name" label="템플릿명" rules={[{ required: true }]}>
            <Input maxLength={200} placeholder="예: 아메리카노 무료" />
          </Form.Item>
          <Form.Item name="description" label="설명">
            <Input.TextArea rows={2} maxLength={2000} />
          </Form.Item>
          <Form.Item name="active" label="활성" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </PageShell>
  )
}
