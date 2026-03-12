import { Button, Card, Form, Input, InputNumber, Modal, Select, Space, Switch, Table, Tag, Typography, message } from 'antd'
import { useQuery } from '@tanstack/react-query'
import React from 'react'
import { api } from '../../shared/api'
import { atLeast } from '../../shared/roles'
import { getSession } from '../../shared/storage'
import { PageShell } from '../common/PageShell'

type Row = {
  id: string
  codeGroup: string
  code: string
  name: string
  active: boolean
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export function CommonCodesPage() {
  const role = getSession()?.role ?? 'OPERATOR'
  const canEdit = atLeast(role, 'SUPER_ADMIN')

  const [filters, setFilters] = React.useState<{ codeGroup: string; active: '' | 'true' | 'false'; keyword: string }>({
    codeGroup: '',
    active: '',
    keyword: '',
  })

  const q = useQuery({
    queryKey: ['admin', 'common-codes', filters],
    queryFn: async () => {
      const params: any = {}
      if (filters.codeGroup.trim()) params.codeGroup = filters.codeGroup.trim()
      if (filters.keyword.trim()) params.keyword = filters.keyword.trim()
      if (filters.active === 'true') params.active = true
      if (filters.active === 'false') params.active = false
      const res = await api.get('/api/v1/admin/common-codes', { params })
      return (res.data ?? []) as Row[]
    },
  })

  const rows = q.data ?? []

  const [open, setOpen] = React.useState(false)
  const [editing, setEditing] = React.useState<Row | null>(null)
  const [saving, setSaving] = React.useState(false)
  const [form] = Form.useForm<{ codeGroup: string; code: string; name: string; active: boolean; sortOrder: number }>()

  const openCreate = () => {
    if (!canEdit) return
    setEditing(null)
    form.resetFields()
    form.setFieldsValue({ codeGroup: '', code: '', name: '', active: true, sortOrder: 0 })
    setOpen(true)
  }

  const openEdit = (r: Row) => {
    if (!canEdit) return
    setEditing(r)
    form.setFieldsValue({
      codeGroup: r.codeGroup,
      code: r.code,
      name: r.name,
      active: r.active,
      sortOrder: r.sortOrder,
    })
    setOpen(true)
  }

  const onSubmit = async () => {
    if (!canEdit) return
    const v = await form.validateFields()
    setSaving(true)
    try {
      if (editing) {
        await api.put(`/api/v1/admin/common-codes/${encodeURIComponent(editing.id)}`, {
          name: v.name,
          active: v.active,
          sortOrder: v.sortOrder,
        })
        message.success('공통코드가 수정되었습니다.')
      } else {
        await api.post('/api/v1/admin/common-codes', v)
        message.success('공통코드가 등록되었습니다.')
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
      title="공통코드 관리"
      extra={
        <Space size={12} wrap>
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            테넌트별 공통코드를 조회/등록/수정합니다.
          </Typography.Text>
          {canEdit ? (
            <Button size="small" type="primary" onClick={openCreate}>
              공통코드 등록
            </Button>
          ) : null}
        </Space>
      }
    >
      <Card>
        <Space wrap>
          <Input
            placeholder="코드그룹 (예: MEMBER_STATUS)"
            value={filters.codeGroup}
            onChange={(e) => setFilters((p) => ({ ...p, codeGroup: e.target.value }))}
            allowClear
            style={{ width: 260 }}
          />
          <Select
            value={filters.active}
            onChange={(v) => setFilters((p) => ({ ...p, active: v }))}
            style={{ width: 140 }}
            options={[
              { value: '', label: '전체' },
              { value: 'true', label: '활성' },
              { value: 'false', label: '비활성' },
            ]}
          />
          <Input
            placeholder="코드/이름 검색"
            value={filters.keyword}
            onChange={(e) => setFilters((p) => ({ ...p, keyword: e.target.value }))}
            allowClear
            style={{ width: 260 }}
          />
          <Button onClick={() => q.refetch()} loading={q.isFetching}>
            새로고침
          </Button>
        </Space>
      </Card>

      <Card>
        <Table<Row>
          rowKey={(r) => r.id}
          dataSource={rows}
          loading={q.isLoading}
          pagination={{ pageSize: 30 }}
          columns={[
            { title: '코드그룹', dataIndex: 'codeGroup', width: 220, render: (v: string) => <Tag>{v}</Tag> },
            { title: '코드', dataIndex: 'code', width: 180 },
            { title: '이름', dataIndex: 'name' },
            { title: '활성', dataIndex: 'active', width: 110, render: (v: boolean) => <Switch checked={v} disabled /> },
            { title: '정렬', dataIndex: 'sortOrder', width: 90 },
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
          locale={{ emptyText: q.isError ? '공통코드 목록 조회에 실패했습니다.' : '공통코드 데이터가 없습니다.' }}
        />
      </Card>

      <Modal
        open={open}
        title={editing ? '공통코드 수정' : '공통코드 등록'}
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
          <Form.Item label="코드그룹" name="codeGroup" rules={[{ required: true, message: '코드그룹을 입력하세요' }]}>
            <Input disabled={!!editing} placeholder="예: MEMBER_STATUS" />
          </Form.Item>
          <Form.Item label="코드" name="code" rules={[{ required: true, message: '코드를 입력하세요' }]}>
            <Input disabled={!!editing} placeholder="예: ACTIVE" />
          </Form.Item>
          <Form.Item label="이름" name="name" rules={[{ required: true, message: '이름을 입력하세요' }]}>
            <Input placeholder="예: 정상" />
          </Form.Item>
          <Form.Item label="활성" name="active" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item label="정렬순서" name="sortOrder" rules={[{ required: true, message: '정렬순서를 입력하세요' }]}>
            <InputNumber style={{ width: '100%' }} step={1} />
          </Form.Item>
        </Form>
      </Modal>
    </PageShell>
  )
}

