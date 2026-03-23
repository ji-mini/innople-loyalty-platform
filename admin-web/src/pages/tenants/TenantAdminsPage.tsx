import { Button, Card, Form, Input, Modal, Select, Space, Table, Tag, Typography, message } from 'antd'
import { useQuery } from '@tanstack/react-query'
import dayjs from 'dayjs'
import React from 'react'
import { PageShell } from '../common/PageShell'
import { api } from '../../shared/api'
import type { AdminRole } from '../../shared/types'
import { atLeast } from '../../shared/roles'
import { getSession } from '../../shared/storage'
import { listPublicTenants } from '../../shared/tenants'
import { useCommonCodes } from '../../shared/queries'

type Row = {
  id: string
  tenantId: string
  name: string
  phoneNumber: string
  email: string | null
  role: AdminRole
  createdAt: string
  updatedAt: string
}

function buildRoleOptions(items: { code: string; name: string }[]) {
  return [{ value: undefined, label: '전체' } as any].concat(items.map((c) => ({ value: c.code, label: `${c.name} (${c.code})` })))
}

export function TenantAdminsPage() {
  const session = getSession()
  const currentRole = session?.role ?? 'OPERATOR'
  const canEdit = atLeast(currentRole, 'SUPER_ADMIN')

  const tenantsQuery = useQuery({
    queryKey: ['public', 'tenants'],
    queryFn: listPublicTenants,
  })

  const [tenantId, setTenantId] = React.useState<string>(() => session?.tenantId ?? '')
  const [keyword, setKeyword] = React.useState('')
  const [role, setRole] = React.useState<Row['role'] | undefined>(undefined)
  const roleCodes = useCommonCodes('ADMIN_ROLE')
  const roleName = React.useMemo(() => {
    const map = new Map<string, string>()
    for (const c of roleCodes.data ?? []) map.set(c.code, c.name)
    return map
  }, [roleCodes.data])

  const q = useQuery({
    queryKey: ['admin', 'admin-users', tenantId, keyword],
    enabled: !!tenantId,
    queryFn: async () => {
      const res = await api.get('/api/v1/admin/admin-users', {
        params: keyword ? { keyword } : {},
        headers: { 'X-Tenant-Id': tenantId },
      })
      return (res.data ?? []) as Row[]
    },
  })

  const rows = React.useMemo<Row[]>(() => {
    const items = q.data ?? []
    if (!role) return items
    return items.filter((r) => r.role === role)
  }, [q.data, role])

  const tenantNameById = React.useMemo(() => {
    const map = new Map<string, string>()
    for (const t of tenantsQuery.data?.items ?? []) {
      map.set(t.tenantId, t.name)
    }
    return map
  }, [tenantsQuery.data?.items])

  const [open, setOpen] = React.useState(false)
  const [editing, setEditing] = React.useState<Row | null>(null)
  const [saving, setSaving] = React.useState(false)
  const [form] = Form.useForm<{ phoneNumber: string; email?: string; name: string; role: AdminRole }>()

  const openEdit = (r: Row) => {
    if (!canEdit) return
    setEditing(r)
    form.setFieldsValue({
      phoneNumber: r.phoneNumber,
      email: r.email ?? '',
      name: r.name,
      role: r.role,
    })
    setOpen(true)
  }

  const onSubmit = async () => {
    if (!canEdit || !editing) return
    const v = await form.validateFields()
    setSaving(true)
    try {
      await api.put(
        `/api/v1/admin/admin-users/${encodeURIComponent(editing.id)}`,
        {
          phoneNumber: v.phoneNumber,
          email: v.email,
          name: v.name,
          role: v.role,
        },
        { headers: { 'X-Tenant-Id': tenantId } },
      )
      message.success('테넌트 관리자 정보가 수정되었습니다.')
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
    <PageShell title="테넌트 관리자">
      <Card>
        <Space wrap>
          <Select
            value={tenantId}
            onChange={(v) => setTenantId(v)}
            style={{ width: 240 }}
            loading={tenantsQuery.isLoading}
            placeholder="테넌트 선택"
            options={(tenantsQuery.data?.items ?? []).map((t) => ({ value: t.tenantId, label: t.name }))}
            showSearch
            optionFilterProp="label"
          />
          <Input
            placeholder="이름/휴대폰/이메일"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            allowClear
            style={{ width: 280 }}
          />
          <Select
            value={role}
            onChange={setRole}
            style={{ width: 220 }}
            loading={roleCodes.isLoading}
            options={buildRoleOptions((roleCodes.data ?? []).map((c) => ({ code: c.code, name: c.name })) as any) as any}
          />
          <Button onClick={() => q.refetch()} loading={q.isFetching} disabled={!tenantId}>
            새로고침
          </Button>
          {!canEdit ? (
            <Typography.Text type="secondary" style={{ fontSize: 12 }}>
              SUPER_ADMIN만 수정이 가능합니다.
            </Typography.Text>
          ) : null}
        </Space>
      </Card>

      <Card>
        <Table<Row>
          rowKey={(r) => r.id}
          dataSource={rows}
          loading={q.isLoading}
          pagination={{ pageSize: 20 }}
          columns={[
            {
              title: '소속 테넌트',
              dataIndex: 'tenantId',
              width: 200,
              render: (v: string) => tenantNameById.get(v) ?? v,
            },
            { title: '이름', dataIndex: 'name', width: 180 },
            { title: '휴대폰', dataIndex: 'phoneNumber', width: 180 },
            { title: '이메일', dataIndex: 'email', width: 120, render: (v: string | null) => v ?? '-' },
            { title: '권한', dataIndex: 'role', width: 180, render: (v: Row['role']) => <Tag>{roleName.get(v) ?? v}</Tag> },
            {
              title: '수정일시',
              dataIndex: 'updatedAt',
              width: 260,
              render: (v: string) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '-'),
            },
            ...(canEdit
              ? [
                  {
                    title: '관리',
                    key: 'actions',
                    width: 110,
                    render: (_: any, r: Row) => (
                      <Button size="small" onClick={() => openEdit(r)}>
                        수정
                      </Button>
                    ),
                  },
                ]
              : []),
          ]}
          locale={{ emptyText: q.isError ? '관리자 목록 조회에 실패했습니다.' : '관리자 데이터가 없습니다.' }}
        />
      </Card>

      <Modal
        open={open}
        title="테넌트 관리자 수정"
        okText="수정"
        onOk={onSubmit}
        confirmLoading={saving}
        onCancel={() => {
          setOpen(false)
          setEditing(null)
        }}
        destroyOnClose
      >
        <Form form={form} layout="vertical" requiredMark={false}>
          <Form.Item
            label="휴대폰번호"
            name="phoneNumber"
            rules={[{ required: true, message: '휴대폰번호를 입력하세요' }]}
            getValueFromEvent={(e) => String(e?.target?.value ?? '').replace(/\D/g, '')}
          >
            <Input placeholder="예: 01012345678" />
          </Form.Item>
          <Form.Item label="이메일(선택)" name="email">
            <Input placeholder="예: admin@innople.com" />
          </Form.Item>
          <Form.Item label="이름" name="name" rules={[{ required: true, message: '이름을 입력하세요' }]}>
            <Input placeholder="예: 홍길동" />
          </Form.Item>
          <Form.Item label="권한" name="role" rules={[{ required: true, message: '권한을 선택하세요' }]}>
            <Select
              options={[
                { value: 'OPERATOR', label: 'OPERATOR(조회 전용)' },
                { value: 'ADMIN', label: 'ADMIN(관리 메뉴 접근)' },
                { value: 'SUPER_ADMIN', label: 'SUPER_ADMIN(수정/등록 가능)' },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </PageShell>
  )
}

