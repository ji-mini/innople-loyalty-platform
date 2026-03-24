import { Button, Card, Form, Input, InputNumber, Modal, Popconfirm, Space, Table, Typography, message } from 'antd'
import { useQueryClient } from '@tanstack/react-query'
import React from 'react'
import { PageShell } from '../common/PageShell'
import { api } from '../../shared/api'
import { useMemberGrades } from '../../shared/queries'
import type { MemberGradeItem } from '../../shared/queries'
import { atLeast } from '../../shared/roles'
import { getSession } from '../../shared/storage'

export function MemberGradesPage() {
  const [keyword, setKeyword] = React.useState('')
  const { data: grades = [], isLoading } = useMemberGrades()
  const queryClient = useQueryClient()
  const role = getSession()?.role ?? 'OPERATOR'
  const canEdit = atLeast(role, 'ADMIN')

  const rows = React.useMemo(() => {
    if (!keyword.trim()) return grades
    const k = keyword.toLowerCase().trim()
    return grades.filter(
      (g) =>
        g.code.toLowerCase().includes(k) ||
        (g.name?.toLowerCase().includes(k) ?? false) ||
        (g.description?.toLowerCase().includes(k) ?? false)
    )
  }, [grades, keyword])

  const [open, setOpen] = React.useState(false)
  const [editing, setEditing] = React.useState<MemberGradeItem | null>(null)
  const [saving, setSaving] = React.useState(false)
  const [form] = Form.useForm<{ name: string; level: number; description?: string }>()

  const openCreate = () => {
    if (!canEdit) return
    setEditing(null)
    form.resetFields()
    form.setFieldsValue({ name: '', level: 1, description: '' })
    setOpen(true)
  }

  const openEdit = (r: MemberGradeItem) => {
    if (!canEdit) return
    setEditing(r)
    form.setFieldsValue({
      name: r.name,
      level: parseInt(r.code, 10) || 1,
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
        await api.put(`/api/v1/member-grades/${encodeURIComponent(editing.id)}`, {
          name: v.name,
          level: v.level,
          description: v.description || null,
        })
        message.success('회원등급이 수정되었습니다.')
      } else {
        await api.post('/api/v1/member-grades', {
          name: v.name,
          level: v.level,
          description: v.description || null,
        })
        message.success('회원등급이 등록되었습니다.')
      }
      setOpen(false)
      setEditing(null)
      await queryClient.invalidateQueries({ queryKey: ['member-grades'] })
    } catch (e: any) {
      message.error(e?.response?.data?.message ?? e?.message ?? '저장 실패')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id: string) => {
    if (!canEdit) return
    try {
      await api.delete(`/api/v1/member-grades/${encodeURIComponent(id)}`)
      message.success('회원등급이 삭제되었습니다.')
      await queryClient.invalidateQueries({ queryKey: ['member-grades'] })
    } catch (e: any) {
      message.error(e?.response?.data?.message ?? e?.message ?? '삭제 실패')
    }
  }

  return (
    <PageShell
      title="회원등급관리"
      extra={
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            회원 등급별 혜택 정책을 관리합니다.
          </Typography.Text>
          {canEdit ? (
            <Button type="primary" onClick={openCreate}>
              등급 등록
            </Button>
          ) : null}
        </div>
      }
    >
      <Card>
        <Form layout="inline">
          <Form.Item>
            <Input
              placeholder="등급 코드/명"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              allowClear
              style={{ width: 260 }}
            />
          </Form.Item>
        </Form>
      </Card>

      <Card>
        <Table
          rowKey={(r) => r.id}
          dataSource={rows}
          loading={isLoading}
          pagination={false}
          columns={[
            { title: '코드', dataIndex: 'code', width: 160 },
            { title: '등급명', dataIndex: 'name', width: 200 },
            { title: '설명', dataIndex: 'description' },
            ...(canEdit
              ? [
                  {
                    title: '관리',
                    key: 'actions',
                    width: 140,
                    render: (_: unknown, r: MemberGradeItem) => (
                      <Space>
                        <Button size="small" onClick={() => openEdit(r)}>
                          수정
                        </Button>
                        <Popconfirm
                          title="이 회원등급을 삭제하시겠습니까?"
                          onConfirm={() => handleDelete(r.id)}
                        >
                          <Button size="small" danger>
                            삭제
                          </Button>
                        </Popconfirm>
                      </Space>
                    ),
                  },
                ]
              : []),
          ]}
          locale={{ emptyText: '등급 데이터가 없습니다.' }}
        />
      </Card>

      <Modal
        open={open}
        title={editing ? '회원등급 수정' : '회원등급 등록'}
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
          <Form.Item label="등급명" name="name" rules={[{ required: true, message: '등급명을 입력하세요' }]}>
            <Input placeholder="예: 일반" />
          </Form.Item>
          <Form.Item
            label="레벨"
            name="level"
            rules={[{ required: true, message: '레벨을 입력하세요' }]}
            extra="숫자가 클수록 높은 등급입니다."
          >
            <InputNumber style={{ width: '100%' }} min={1} />
          </Form.Item>
          <Form.Item label="설명" name="description">
            <Input.TextArea placeholder="등급 설명 (선택)" rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </PageShell>
  )
}

