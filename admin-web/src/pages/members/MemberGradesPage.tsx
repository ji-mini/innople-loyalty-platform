import { Button, Card, Form, Input, InputNumber, Modal, Popconfirm, Space, Table, Tag, Typography, message } from 'antd'
import { CrownOutlined, InfoCircleOutlined, PlusOutlined, StarFilled, TrophyOutlined } from '@ant-design/icons'
import { useQueryClient } from '@tanstack/react-query'
import React from 'react'
import { PageShell } from '../common/PageShell'
import { api } from '../../shared/api'
import { useMemberGrades } from '../../shared/queries'
import type { MemberGradeItem } from '../../shared/queries'
import { atLeast } from '../../shared/roles'
import { getSession } from '../../shared/storage'

const PAGE_BACKGROUND_STYLE: React.CSSProperties = {
  margin: '-8px -8px 0',
  padding: 24,
  borderRadius: 18,
  background: '#f8fafc',
}

const CARD_STYLE: React.CSSProperties = {
  borderRadius: 16,
  border: '1px solid #e5e7eb',
  boxShadow: '0 10px 30px rgba(15, 23, 42, 0.04)',
}

const SUMMARY_GRID_STYLE: React.CSSProperties = {
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
  gap: 16,
}

const FILTER_GRID_STYLE: React.CSSProperties = {
  display: 'grid',
  gridTemplateColumns: 'minmax(260px, 420px) auto',
  gap: 12,
  alignItems: 'end',
}

const GRADE_COLORS = ['#16a34a', '#2563eb', '#7c3aed', '#ea580c', '#0891b2', '#db2777']

function parseGradeLevel(grade: MemberGradeItem) {
  const parsed = Number.parseInt(grade.code, 10)
  return Number.isFinite(parsed) ? parsed : 0
}

function formatRate(rate?: number | null) {
  if (rate == null) return '-'
  return `${Number(rate).toLocaleString('ko-KR', { maximumFractionDigits: 2 })}%`
}

function getGradeColor(index: number) {
  return GRADE_COLORS[index % GRADE_COLORS.length]
}

function SummaryCard({
  title,
  value,
  description,
  icon,
}: {
  title: string
  value: React.ReactNode
  description: string
  icon: React.ReactNode
}) {
  return (
    <Card bordered={false} style={CARD_STYLE} styles={{ body: { padding: 18 } }}>
      <Space align="start" size={12} style={{ width: '100%', justifyContent: 'space-between' }}>
        <div>
          <Typography.Text type="secondary" style={{ fontSize: 12, fontWeight: 650 }}>
            {title}
          </Typography.Text>
          <div style={{ marginTop: 8, fontSize: 24, fontWeight: 800, color: '#0f172a' }}>{value}</div>
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            {description}
          </Typography.Text>
        </div>
        <div
          style={{
            width: 40,
            height: 40,
            borderRadius: 12,
            display: 'grid',
            placeItems: 'center',
            color: '#16a34a',
            background: '#dcfce7',
          }}
        >
          {icon}
        </div>
      </Space>
    </Card>
  )
}

export function MemberGradesPage() {
  const [draftKeyword, setDraftKeyword] = React.useState('')
  const [keyword, setKeyword] = React.useState('')
  const { data: grades = [], isLoading } = useMemberGrades()
  const queryClient = useQueryClient()
  const role = getSession()?.role ?? 'OPERATOR'
  const canEdit = atLeast(role, 'ADMIN')

  const sortedGrades = React.useMemo(
    () => [...grades].sort((a, b) => parseGradeLevel(a) - parseGradeLevel(b)),
    [grades]
  )

  const highestGrade = sortedGrades[sortedGrades.length - 1] ?? null
  const defaultGrade = grades.find((g) => g.name.includes('기본') || g.code === '1') ?? sortedGrades[0] ?? null
  const averageEarnRate =
    grades.length > 0
      ? grades.reduce((sum, grade) => sum + Number(grade.earnRatePercent ?? 0), 0) / grades.length
      : 0

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

  const resetFilter = () => {
    setDraftKeyword('')
    setKeyword('')
  }

  const [open, setOpen] = React.useState(false)
  const [editing, setEditing] = React.useState<MemberGradeItem | null>(null)
  const [saving, setSaving] = React.useState(false)
  const [form] = Form.useForm<{ name: string; level: number; description?: string; earnRatePercent: number }>()

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
      earnRatePercent: r.earnRatePercent ?? 0,
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
          earnRatePercent: v.earnRatePercent,
        })
        message.success('등급이 수정되었습니다.')
      } else {
        await api.post('/api/v1/member-grades', {
          name: v.name,
          level: v.level,
          description: v.description || null,
          earnRatePercent: v.earnRatePercent,
        })
        message.success('등급이 등록되었습니다.')
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
      message.success('등급이 삭제되었습니다.')
      await queryClient.invalidateQueries({ queryKey: ['member-grades'] })
    } catch (e: any) {
      message.error(e?.response?.data?.message ?? e?.message ?? '삭제 실패')
    }
  }

  return (
    <PageShell
      title="등급 관리"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          회원 등급과 POS 적립률 정책을 관리합니다.
        </Typography.Text>
      }
    >
      <div style={PAGE_BACKGROUND_STYLE}>
        <style>
          {`
            .member-grades-page .ant-form-item-label > label {
              font-weight: 650;
              color: #334155;
            }
            .member-grades-page .ant-input,
            .member-grades-page .ant-input-number,
            .member-grades-page .ant-input-number-input,
            .member-grades-page .ant-btn {
              border-radius: 10px;
            }
            .member-grades-page .ant-table-thead > tr > th {
              background: #f8fafc;
              color: #475569;
              font-weight: 700;
            }
            .member-grades-page .ant-table-tbody > tr:hover > td {
              background: #f0fdf4 !important;
            }
          `}
        </style>
        <Space direction="vertical" size={20} style={{ width: '100%' }} className="member-grades-page">
          <div style={SUMMARY_GRID_STYLE}>
            <SummaryCard
              title="전체 등급 수"
              value={`${grades.length.toLocaleString('ko-KR')}개`}
              description="등록된 회원 등급"
              icon={<TrophyOutlined />}
            />
            <SummaryCard
              title="최고 등급"
              value={highestGrade?.name ?? '-'}
              description={highestGrade ? `코드 ${highestGrade.code}` : '등록된 등급 없음'}
              icon={<CrownOutlined />}
            />
            <SummaryCard
              title="기본 등급"
              value={defaultGrade?.name ?? '-'}
              description={defaultGrade ? `코드 ${defaultGrade.code}` : '기본 등급 없음'}
              icon={<StarFilled />}
            />
            <SummaryCard
              title="평균 적립률"
              value={formatRate(averageEarnRate)}
              description="등급별 적립률 평균"
              icon={<InfoCircleOutlined />}
            />
            <SummaryCard
              title="사용 중인 등급 수"
              value={`${grades.length.toLocaleString('ko-KR')}개`}
              description="현재 정책에 노출되는 등급"
              icon={<TrophyOutlined />}
            />
          </div>

          <Card bordered={false} style={CARD_STYLE} styles={{ body: { padding: 22 } }}>
            <Space align="start" size={14}>
              <div
                style={{
                  width: 42,
                  height: 42,
                  borderRadius: 14,
                  display: 'grid',
                  placeItems: 'center',
                  color: '#2563eb',
                  background: '#dbeafe',
                  flex: '0 0 auto',
                }}
              >
                <InfoCircleOutlined />
              </div>
              <div>
                <Typography.Title level={5} style={{ margin: 0, color: '#0f172a' }}>
                  등급 정책 안내
                </Typography.Title>
                <Space direction="vertical" size={4} style={{ marginTop: 10 }}>
                  <Typography.Text type="secondary">등급별 적립률은 POS 적립 대상 금액 기준으로 계산됩니다.</Typography.Text>
                  <Typography.Text type="secondary">적립률 1% = 결제금액 10,000원 기준 100P 적립</Typography.Text>
                  <Typography.Text type="secondary">등급 순서는 코드 또는 level 기준으로 관리됩니다.</Typography.Text>
                </Space>
              </div>
            </Space>
          </Card>

          <Card bordered={false} style={CARD_STYLE} styles={{ body: { padding: 22 } }}>
            <Form
              layout="vertical"
              onFinish={() => setKeyword(draftKeyword.trim())}
            >
              <div style={FILTER_GRID_STYLE}>
                <Form.Item label="등급 검색" style={{ marginBottom: 0 }}>
                  <Input
                    placeholder="등급명 또는 코드로 검색"
                    value={draftKeyword}
                    onChange={(e) => setDraftKeyword(e.target.value)}
                    allowClear
                    style={{ height: 40 }}
                  />
                </Form.Item>
                <Form.Item style={{ marginBottom: 0 }}>
                  <Space>
                    <Button type="primary" htmlType="submit" style={{ height: 40 }}>
                      검색
                    </Button>
                    <Button style={{ height: 40 }} onClick={resetFilter}>
                      초기화
                    </Button>
                  </Space>
                </Form.Item>
              </div>
            </Form>
          </Card>

          <Card bordered={false} style={CARD_STYLE} styles={{ body: { padding: 0 } }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: 16,
                padding: '22px 22px 16px',
                borderBottom: '1px solid #e5e7eb',
              }}
            >
              <div>
                <Typography.Title level={5} style={{ margin: 0, color: '#0f172a' }}>
                  등급 목록
                </Typography.Title>
                <Typography.Text type="secondary" style={{ fontSize: 13 }}>
                  등급명, 코드, 적립률과 정책 설명을 한눈에 확인합니다.
                </Typography.Text>
              </div>
              {canEdit ? (
                <Button type="primary" icon={<PlusOutlined />} onClick={openCreate} style={{ height: 40 }}>
                  등급 등록
                </Button>
              ) : null}
            </div>
            <Table
              rowKey={(r) => r.id}
              dataSource={rows}
              loading={isLoading}
              pagination={false}
              rowClassName={() => 'member-grade-row'}
              columns={[
                {
                  title: '코드',
                  dataIndex: 'code',
                  width: 140,
                  render: (v: string) => <Tag style={{ borderRadius: 999, marginInlineEnd: 0 }}>{v}</Tag>,
                },
                {
                  title: '등급명',
                  dataIndex: 'name',
                  width: 220,
                  render: (v: string, r: MemberGradeItem, index: number) => (
                    <Space size={10}>
                      <span
                        style={{
                          width: 10,
                          height: 10,
                          borderRadius: 999,
                          background: getGradeColor(index),
                          boxShadow: `0 0 0 4px ${getGradeColor(index)}18`,
                        }}
                      />
                      <Typography.Text strong>{v}</Typography.Text>
                      {r.id === highestGrade?.id ? <Tag color="gold">최고</Tag> : null}
                    </Space>
                  ),
                },
                {
                  title: '적립률(%)',
                  dataIndex: 'earnRatePercent',
                  width: 140,
                  render: (v: number) => (
                    <Tag color="green" style={{ borderRadius: 999, fontWeight: 700, paddingInline: 10 }}>
                      {formatRate(v)}
                    </Tag>
                  ),
                },
                {
                  title: '설명',
                  dataIndex: 'description',
                  ellipsis: true,
                  render: (v: string | null) => (
                    <Typography.Text type="secondary">{v || '설명이 없습니다.'}</Typography.Text>
                  ),
                },
                ...(canEdit
                  ? [
                      {
                        title: '관리',
                        key: 'actions',
                        width: 150,
                        align: 'right' as const,
                        render: (_: unknown, r: MemberGradeItem) => (
                          <Space>
                            <Button size="small" onClick={() => openEdit(r)}>
                              수정
                            </Button>
                            <Popconfirm
                              title="이 등급을 삭제하시겠습니까?"
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
        </Space>
      </div>

      <Modal
        open={open}
        title={editing ? '등급 수정' : '등급 등록'}
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
          <Form.Item
            label="적립률(%)"
            name="earnRatePercent"
            rules={[{ required: true, message: '적립률을 입력하세요' }]}
            extra="적립 대상 금액(원) × 적립률 ÷ 100으로 포인트가 산정됩니다. (소수점 이하는 버림)"
          >
            <InputNumber style={{ width: '100%' }} min={0} max={100} step={0.1} precision={2} placeholder="예: 1.5" />
          </Form.Item>
          <Form.Item label="설명" name="description">
            <Input.TextArea placeholder="등급 설명 (선택)" rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </PageShell>
  )
}

