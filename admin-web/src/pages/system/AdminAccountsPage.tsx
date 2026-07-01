import { Button, Card, Form, Input, Modal, Popconfirm, Select, Space, Table, Tabs, Tag, Tooltip, Typography, message } from 'antd'
import { useQuery } from '@tanstack/react-query'
import dayjs from 'dayjs'
import React from 'react'
import { PageShell } from '../common/PageShell'
import { api } from '../../shared/api'
import type { AdminRole, AdminUserStatus } from '../../shared/types'
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
  status: AdminUserStatus
  createdAt: string
  updatedAt: string
}

const STATUS_META: Record<AdminUserStatus, { label: string; color: string }> = {
  PENDING: { label: '승인 대기', color: 'orange' },
  ACTIVE: { label: '활성', color: 'green' },
  INACTIVE: { label: '비활성', color: 'default' },
  REJECTED: { label: '거절', color: 'red' },
}

type StatusFilter = 'ALL' | AdminUserStatus

export function AdminAccountsPage() {
  const [keyword, setKeyword] = React.useState('')
  const [statusFilter, setStatusFilter] = React.useState<StatusFilter>('PENDING')
  const role = getSession()?.role ?? 'OPERATOR'
  const canEdit = atLeast(role, 'SUPER_ADMIN')
  const tenantsQuery = useQuery({
    queryKey: ['public', 'tenants'],
    queryFn: listPublicTenants,
  })
  const roleCodes = useCommonCodes('ADMIN_ROLE')
  const roleName = React.useMemo(() => {
    const map = new Map<string, string>()
    for (const c of roleCodes.data ?? []) map.set(c.code, c.name)
    return map
  }, [roleCodes.data])
  const tenantNameById = React.useMemo(() => {
    const map = new Map<string, string>()
    for (const t of tenantsQuery.data?.items ?? []) {
      map.set(t.tenantId, t.name)
    }
    return map
  }, [tenantsQuery.data?.items])

  const q = useQuery({
    queryKey: ['admin', 'admin-users', keyword],
    queryFn: async () => {
      const res = await api.get('/api/v1/admin/admin-users', { params: keyword ? { keyword } : {} })
      return (res.data ?? []) as Row[]
    },
  })

  const rows = q.data ?? []

  const statusCounts = React.useMemo(() => {
    const counts: Record<StatusFilter, number> = {
      ALL: rows.length,
      PENDING: 0,
      ACTIVE: 0,
      INACTIVE: 0,
      REJECTED: 0,
    }
    for (const r of rows) counts[r.status] = (counts[r.status] ?? 0) + 1
    return counts
  }, [rows])

  const filteredRows = React.useMemo(
    () => (statusFilter === 'ALL' ? rows : rows.filter((r) => r.status === statusFilter)),
    [rows, statusFilter],
  )

  // 최초 1회: 데이터 로드 후 승인대기 건수에 따라 기본 탭을 자동 결정한다.
  // (승인대기 1건 이상 → PENDING, 0건 → ALL). 이후 refetch/수동 변경 시엔 덮어쓰지 않는다.
  const autoTabAppliedRef = React.useRef(false)
  React.useEffect(() => {
    if (autoTabAppliedRef.current) return
    if (!q.isSuccess) return
    autoTabAppliedRef.current = true
    setStatusFilter(statusCounts.PENDING > 0 ? 'PENDING' : 'ALL')
  }, [q.isSuccess, statusCounts.PENDING])

  const onChangeStatusFilter = (next: StatusFilter) => {
    // 사용자가 직접 탭을 바꾸면 자동 결정 로직이 다시 개입하지 않도록 잠근다.
    autoTabAppliedRef.current = true
    setStatusFilter(next)
  }

  const [open, setOpen] = React.useState(false)
  const [editing, setEditing] = React.useState<Row | null>(null)
  const [saving, setSaving] = React.useState(false)
  const [form] = Form.useForm<{
    tenantId: string
    name: string
    phoneNumber: string
    email?: string
    password?: string
    role: AdminRole
  }>()

  const openCreate = () => {
    if (!canEdit) return
    setEditing(null)
    form.resetFields()
    form.setFieldsValue({
      tenantId: getSession()?.tenantId ?? '',
      name: '',
      phoneNumber: '',
      email: '',
      password: '',
      role: 'OPERATOR',
    })
    setOpen(true)
  }

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

  const [statusUpdatingId, setStatusUpdatingId] = React.useState<string | null>(null)
  const [rejectTarget, setRejectTarget] = React.useState<Row | null>(null)
  const [rejectReason, setRejectReason] = React.useState('')
  const [rejecting, setRejecting] = React.useState(false)

  const onChangeStatus = async (
    r: Row,
    status: Extract<AdminUserStatus, 'ACTIVE' | 'INACTIVE' | 'REJECTED'>,
    options?: { successMessage?: string; reason?: string },
  ): Promise<boolean> => {
    if (!canEdit) return false
    setStatusUpdatingId(r.id)
    try {
      const reason = options?.reason?.trim()
      await api.patch(`/api/v1/admin/users/${encodeURIComponent(r.id)}/status`, {
        status,
        ...(reason ? { reason } : {}),
      })
      message.success(
        options?.successMessage ?? (status === 'ACTIVE' ? '계정이 활성화되었습니다.' : '계정이 비활성화되었습니다.'),
      )
      await q.refetch()
      return true
    } catch (e: any) {
      message.error(e?.response?.data?.message ?? e?.message ?? '상태 변경 실패')
      return false
    } finally {
      setStatusUpdatingId(null)
    }
  }

  const openReject = (r: Row) => {
    if (!canEdit) return
    setRejectTarget(r)
    setRejectReason('')
  }

  const submitReject = async () => {
    if (!rejectTarget) return
    setRejecting(true)
    try {
      const ok = await onChangeStatus(rejectTarget, 'REJECTED', {
        successMessage: '계정이 거절되었습니다.',
        reason: rejectReason,
      })
      if (ok) {
        setRejectTarget(null)
        setRejectReason('')
      }
    } finally {
      setRejecting(false)
    }
  }

  const onSubmit = async () => {
    if (!canEdit) return
    const v = await form.validateFields()
    setSaving(true)
    try {
      if (editing) {
        await api.put(`/api/v1/admin/admin-users/${encodeURIComponent(editing.id)}`, {
          phoneNumber: v.phoneNumber,
          email: v.email,
          name: v.name,
          role: v.role,
        })
        message.success('사용자 정보가 수정되었습니다.')
      } else {
        await api.post(
          '/api/v1/admin/admin-users',
          {
            phoneNumber: v.phoneNumber,
            email: v.email,
            name: v.name,
            password: v.password,
            role: v.role,
          },
          { headers: { 'X-Tenant-Id': v.tenantId } },
        )
        message.success('사용자가 등록되었습니다.')
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
      title="사용자 관리"
      extra={
        canEdit ? (
          <Button type="primary" onClick={openCreate}>
            사용자 등록
          </Button>
        ) : (
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            SUPER_ADMIN만 사용자 등록/수정이 가능합니다.
          </Typography.Text>
        )
      }
    >
      <Card>
        <Space wrap>
          <Input
            placeholder="이름/휴대폰/이메일"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            allowClear
            style={{ width: 320 }}
          />
        </Space>
      </Card>

      <Card>
        <Tabs
          activeKey={statusFilter}
          onChange={(k) => onChangeStatusFilter(k as StatusFilter)}
          items={[
            { key: 'PENDING', label: `승인대기 (${statusCounts.PENDING})` },
            { key: 'ACTIVE', label: `승인완료 (${statusCounts.ACTIVE})` },
            { key: 'REJECTED', label: `거절 (${statusCounts.REJECTED})` },
            { key: 'INACTIVE', label: `비활성 (${statusCounts.INACTIVE})` },
            { key: 'ALL', label: `전체 (${statusCounts.ALL})` },
          ]}
        />
        <Table<Row>
          rowKey={(r) => r.id}
          dataSource={filteredRows}
          loading={q.isLoading}
          pagination={{ pageSize: 20 }}
          columns={[
            {
              title: '테넌트명',
              dataIndex: 'tenantId',
              width: 180,
              render: (v: string) => tenantNameById.get(v) ?? v,
            },
            { title: '이름', dataIndex: 'name', width: 180 },
            { title: '휴대폰', dataIndex: 'phoneNumber', width: 180 },
            { title: '이메일', dataIndex: 'email', width: 240, render: (v: string | null) => v ?? '-' },
            {
              title: '사용자ID',
              dataIndex: 'id',
              width: 120,
              render: (v: string) => (
                <Tooltip title={v}>
                  <Typography.Text copyable={{ text: v }} style={{ whiteSpace: 'nowrap' }}>
                    {v.slice(0, 8)}…
                  </Typography.Text>
                </Tooltip>
              ),
            },
            {
              title: '권한',
              dataIndex: 'role',
              width: 140,
              render: (v: Row['role']) => <Tag>{roleName.get(v) ?? v}</Tag>,
            },
            {
              title: '신청일시',
              dataIndex: 'createdAt',
              width: 190,
              render: (v: string) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '-'),
            },
            {
              title: '상태',
              dataIndex: 'status',
              width: 110,
              render: (v: Row['status']) => {
                const meta = STATUS_META[v] ?? { label: v, color: 'default' }
                return <Tag color={meta.color}>{meta.label}</Tag>
              },
            },
            {
              title: '수정일시',
              dataIndex: 'updatedAt',
              width: 190,
              render: (v: string) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '-'),
            },
            ...(canEdit
              ? [
                  {
                    title: '관리',
                    key: 'actions',
                    width: 220,
                    render: (_: any, r: Row) => (
                      <Space size={8} wrap>
                        <Button size="small" onClick={() => openEdit(r)}>
                          수정
                        </Button>
                        {r.status === 'PENDING' && (
                          <>
                            <Popconfirm
                              title={`${r.name} 계정을 승인하시겠습니까?`}
                              okText="승인"
                              cancelText="취소"
                              onConfirm={() => onChangeStatus(r, 'ACTIVE', { successMessage: '계정이 승인되었습니다.' })}
                            >
                              <Button size="small" type="primary" loading={statusUpdatingId === r.id}>
                                승인
                              </Button>
                            </Popconfirm>
                            <Button
                              size="small"
                              danger
                              loading={statusUpdatingId === r.id}
                              onClick={() => openReject(r)}
                            >
                              거절
                            </Button>
                          </>
                        )}
                        {r.status === 'ACTIVE' && (
                          <Popconfirm
                            title={`${r.name} 계정을 비활성화하시겠습니까?`}
                            okText="비활성화"
                            cancelText="취소"
                            okButtonProps={{ danger: true }}
                            onConfirm={() => onChangeStatus(r, 'INACTIVE')}
                          >
                            <Button size="small" danger loading={statusUpdatingId === r.id}>
                              비활성화
                            </Button>
                          </Popconfirm>
                        )}
                        {r.status === 'INACTIVE' && (
                          <Popconfirm
                            title={`${r.name} 계정을 다시 활성화하시겠습니까?`}
                            okText="활성화"
                            cancelText="취소"
                            onConfirm={() => onChangeStatus(r, 'ACTIVE')}
                          >
                            <Button size="small" type="primary" loading={statusUpdatingId === r.id}>
                              활성화
                            </Button>
                          </Popconfirm>
                        )}
                      </Space>
                    ),
                  },
                ]
              : []),
          ]}
          locale={{
            emptyText: (
              <Space direction="vertical" size={6}>
                <Typography.Text>사용자(어드민) 계정이 없습니다.</Typography.Text>
                <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                  우측 상단의 “사용자 등록”으로 추가할 수 있습니다.
                </Typography.Text>
              </Space>
            ),
          }}
        />
      </Card>

      <Modal
        open={open}
        title={editing ? '사용자 수정' : '사용자 등록'}
        okText={editing ? '수정' : '등록'}
        onOk={onSubmit}
        confirmLoading={saving}
        onCancel={() => {
          setOpen(false)
          setEditing(null)
        }}
        destroyOnClose
      >
        <Form form={form} layout="vertical" requiredMark={false}>
          {!editing ? (
            <Form.Item label="테넌트" name="tenantId" rules={[{ required: true, message: '테넌트를 선택하세요' }]}>
              <Select
                loading={tenantsQuery.isLoading}
                placeholder="테넌트를 선택하세요"
                options={(tenantsQuery.data?.items ?? []).map((t) => ({ value: t.tenantId, label: t.name }))}
                showSearch
                optionFilterProp="label"
              />
            </Form.Item>
          ) : null}
          <Form.Item label="이름" name="name" rules={[{ required: true, message: '이름을 입력하세요' }]}>
            <Input placeholder="예: 홍길동" />
          </Form.Item>
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
          {!editing ? (
            <Form.Item label="비밀번호" name="password" rules={[{ required: true, message: '비밀번호를 입력하세요' }]}>
              <Input.Password placeholder="초기 비밀번호" />
            </Form.Item>
          ) : null}
          <Form.Item label="권한" name="role" rules={[{ required: true, message: '권한을 선택하세요' }]}>
            <Select
              loading={roleCodes.isLoading}
              options={(roleCodes.data ?? []).map((c) => ({ value: c.code, label: `${c.name} (${c.code})` }))}
            />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        open={rejectTarget != null}
        title={rejectTarget ? `${rejectTarget.name} 계정을 거절하시겠습니까?` : '계정 거절'}
        okText="거절"
        okButtonProps={{ danger: true }}
        cancelText="취소"
        confirmLoading={rejecting}
        onOk={submitReject}
        onCancel={() => {
          setRejectTarget(null)
          setRejectReason('')
        }}
        destroyOnClose
      >
        <Space direction="vertical" size={12} style={{ width: '100%' }}>
          <Typography.Text type="secondary">
            거절 시 해당 계정은 로그인할 수 없는 거절 상태로 전환되며, 재승인할 수 없습니다.
          </Typography.Text>
          <Input.TextArea
            value={rejectReason}
            onChange={(e) => setRejectReason(e.target.value)}
            placeholder="거절 사유 (선택)"
            rows={3}
            maxLength={500}
            showCount
          />
        </Space>
      </Modal>
    </PageShell>
  )
}

