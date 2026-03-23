import { Button, Card, Form, Input, Modal, Popconfirm, Space, Table, Typography, message } from 'antd'
import { useQuery } from '@tanstack/react-query'
import React from 'react'
import { useNavigate } from 'react-router-dom'
import { PageShell } from '../common/PageShell'
import { api } from '../../shared/api'
import { atLeast } from '../../shared/roles'
import { getSession } from '../../shared/storage'
import { listPublicTenants } from '../../shared/tenants'

type Row = {
  tenantId: string
  name: string
  representativeCode: string
}

export function TenantsPage() {
  const nav = useNavigate()
  const [keyword, setKeyword] = React.useState('')
  const role = getSession()?.role ?? 'OPERATOR'
  const canManage = atLeast(role, 'SUPER_ADMIN')
  const [open, setOpen] = React.useState(false)
  const [editing, setEditing] = React.useState<Row | null>(null)
  const [saving, setSaving] = React.useState(false)
  const [deletingTenantId, setDeletingTenantId] = React.useState<string | null>(null)
  const [form] = Form.useForm<{ name: string; representativeCode: string }>()

  const q = useQuery({
    queryKey: ['public', 'tenants'],
    queryFn: listPublicTenants,
  })

  const rows = React.useMemo<Row[]>(() => {
    const k = keyword.trim().toLowerCase()
    const items = (q.data?.items ?? []).map((t) => ({ tenantId: t.tenantId, name: t.name, representativeCode: t.representativeCode }))
    if (!k) return items
    return items.filter(
      (r) =>
        r.tenantId.toLowerCase().includes(k) ||
        r.name.toLowerCase().includes(k) ||
        r.representativeCode.toLowerCase().includes(k)
    )
  }, [keyword, q.data?.items])

  const openCreate = () => {
    if (!canManage) return
    setEditing(null)
    form.resetFields()
    form.setFieldsValue({ name: '', representativeCode: '' })
    setOpen(true)
  }

  const openEdit = (row: Row) => {
    if (!canManage) return
    setEditing(row)
    form.setFieldsValue({ name: row.name, representativeCode: row.representativeCode })
    setOpen(true)
  }

  const onSubmit = async () => {
    if (!canManage) return
    const values = await form.validateFields()
    setSaving(true)
    try {
      if (editing) {
        await api.put(`/api/v1/admin/tenants/${encodeURIComponent(editing.tenantId)}`, values, {
          headers: { 'X-Tenant-Id': editing.tenantId },
        })
        message.success('테넌트가 수정되었습니다.')
      } else {
        await api.post('/api/v1/admin/tenants', values, {
          headers: { 'X-Tenant-Id': '' },
        })
        message.success('테넌트가 생성되었습니다.')
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

  const onDelete = async (row: Row) => {
    if (!canManage) return
    setDeletingTenantId(row.tenantId)
    try {
      await api.delete(`/api/v1/admin/tenants/${encodeURIComponent(row.tenantId)}`)
      message.success('테넌트가 삭제되었습니다.')
      await q.refetch()
    } catch (e: any) {
      message.error(e?.response?.data?.message ?? e?.message ?? '삭제 실패')
    } finally {
      setDeletingTenantId(null)
    }
  }

  return (
    <PageShell
      title="테넌트 목록"
      extra={
        <Space size={12} wrap>
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            멀티테넌트 SaaS 핵심 관리 화면입니다.
          </Typography.Text>
          {canManage ? (
            <Button type="primary" onClick={openCreate}>
              테넌트 생성
            </Button>
          ) : null}
        </Space>
      }
    >
      <Card>
        <Space wrap>
          <Input
            placeholder="테넌트ID/회사명/도메인"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            allowClear
            style={{ width: 320 }}
          />
        </Space>
      </Card>

      <Card>
        <Table<Row>
          rowKey={(r) => r.tenantId}
          dataSource={rows}
          loading={q.isLoading}
          pagination={{ pageSize: 20 }}
          onRow={(r) => ({
            onClick: () => nav(`/tenants/${encodeURIComponent(r.tenantId)}`),
            style: { cursor: 'pointer' },
          })}
          columns={[
            { title: '테넌트ID', dataIndex: 'tenantId', width: 330 },
            { title: '대표코드', dataIndex: 'representativeCode', width: 110 },
            { title: '테넌트명', dataIndex: 'name' },
            ...(canManage
              ? [
                  {
                    title: '관리',
                    key: 'actions',
                    width: 170,
                    render: (_: unknown, row: Row) => (
                      <Space>
                        <Button
                          size="small"
                          onClick={(e) => {
                            e.stopPropagation()
                            openEdit(row)
                          }}
                        >
                          수정
                        </Button>
                        <Popconfirm
                          title="테넌트를 삭제할까요?"
                          description="연관 데이터가 있으면 삭제되지 않습니다."
                          okText="삭제"
                          cancelText="취소"
                          onConfirm={(e) => {
                            e?.stopPropagation()
                            return onDelete(row)
                          }}
                        >
                          <Button
                            size="small"
                            danger
                            loading={deletingTenantId === row.tenantId}
                            onClick={(e) => e.stopPropagation()}
                          >
                            삭제
                          </Button>
                        </Popconfirm>
                      </Space>
                    ),
                  },
                ]
              : []),
          ]}
          locale={{ emptyText: q.isError ? '테넌트 목록 조회에 실패했습니다.' : '테넌트 데이터가 없습니다.' }}
        />
      </Card>

      <Modal
        open={open}
        title={editing ? '테넌트 수정' : '테넌트 생성'}
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
          <Form.Item label="테넌트명" name="name" rules={[{ required: true, message: '테넌트명을 입력하세요' }]}>
            <Input placeholder="예: INNOPLE" />
          </Form.Item>
          <Form.Item
            label="대표코드"
            name="representativeCode"
            rules={[{ required: true, message: '대표코드를 입력하세요' }]}
            getValueFromEvent={(e) => String(e?.target?.value ?? '').toUpperCase().replace(/[^A-Z]/g, '').slice(0, 2)}
          >
            <Input placeholder="예: IN" maxLength={2} />
          </Form.Item>
        </Form>
      </Modal>
    </PageShell>
  )
}

