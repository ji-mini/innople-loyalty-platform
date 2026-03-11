import { Button, Card, Form, Input, Modal, Select, Space, Table, Tag, Typography, message } from 'antd'
import { useQuery } from '@tanstack/react-query'
import React from 'react'
import { PageShell } from '../common/PageShell'
import { api } from '../../shared/api'
import type { AdminRole } from '../../shared/types'
import { atLeast } from '../../shared/roles'
import { getSession } from '../../shared/storage'

type Row = {
  id: string
  name: string
  phoneNumber: string
  email: string | null
  role: AdminRole
  createdAt: string
  updatedAt: string
}

export function AdminAccountsPage() {
  const [keyword, setKeyword] = React.useState('')
  const role = getSession()?.role ?? 'OPERATOR'
  const canEdit = atLeast(role, 'SUPER_ADMIN')

  const q = useQuery({
    queryKey: ['admin', 'admin-users', keyword],
    queryFn: async () => {
      const res = await api.get('/api/v1/admin/admin-users', { params: keyword ? { keyword } : {} })
      return (res.data ?? []) as Row[]
    },
  })

  const rows = q.data ?? []

  const [open, setOpen] = React.useState(false)
  const [editing, setEditing] = React.useState<Row | null>(null)
  const [saving, setSaving] = React.useState(false)
  const [form] = Form.useForm<{
    phoneNumber: string
    email?: string
    name: string
    password?: string
    role: AdminRole
  }>()

  const openCreate = () => {
    if (!canEdit) return
    setEditing(null)
    form.resetFields()
    form.setFieldsValue({ phoneNumber: '', email: '', name: '', password: '', role: 'OPERATOR' })
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
        await api.post('/api/v1/admin/admin-users', {
          phoneNumber: v.phoneNumber,
          email: v.email,
          name: v.name,
          password: v.password,
          role: v.role,
        })
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
        <Table<Row>
          rowKey={(r) => r.id}
          dataSource={rows}
          loading={q.isLoading}
          pagination={{ pageSize: 20 }}
          columns={[
            { title: '이름', dataIndex: 'name', width: 180 },
            { title: '휴대폰', dataIndex: 'phoneNumber', width: 180 },
            { title: '이메일', dataIndex: 'email', render: (v: string | null) => v ?? '-' },
            {
              title: '권한',
              dataIndex: 'role',
              width: 140,
              render: (v: Row['role']) => <Tag>{v}</Tag>,
            },
            { title: '수정일시', dataIndex: 'updatedAt', width: 190 },
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
          {!editing ? (
            <Form.Item label="비밀번호" name="password" rules={[{ required: true, message: '비밀번호를 입력하세요' }]}>
              <Input.Password placeholder="초기 비밀번호" />
            </Form.Item>
          ) : null}
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

