import { Button, Card, DatePicker, Form, Input, Select, Space, Table, Tag, Typography } from 'antd'
import React from 'react'
import { useNavigate } from 'react-router-dom'
import type { MemberSummary } from '../../shared/types'
import { useMemberList } from '../../shared/queries'

const STATUS_OPTIONS = [
  { value: undefined, label: '전체' },
  { value: 'NORMAL', label: '정상' },
  { value: 'DORMANT', label: '휴면' },
  { value: 'WITHDRAW_REQUESTED', label: '탈퇴요청' },
  { value: 'WITHDRAWN', label: '탈퇴' },
]

export function MembersPage() {
  const nav = useNavigate()
  const [form] = Form.useForm()
  const [filters, setFilters] = React.useState<{
    memberNo?: string
    phoneNumber?: string
    name?: string
    webId?: string
    joinedFrom?: string
    joinedTo?: string
    statusCode?: string
  }>({})
  const [page, setPage] = React.useState(0)
  const size = 20

  const query = useMemberList({
    ...filters,
    page,
    size,
  })

  const columns = [
    {
      title: '회원번호',
      dataIndex: 'memberNo',
      render: (v: string, r: MemberSummary) => (
        <a
          onClick={(e) => {
            e.preventDefault()
            nav(`/members/${encodeURIComponent(r.memberNo)}`)
          }}
        >
          {v}
        </a>
      ),
    },
    { title: '이름', dataIndex: 'name' },
    {
      title: '포인트잔액',
      dataIndex: 'pointBalance',
      width: 140,
      align: 'right' as const,
      render: (v: number) => `${Number(v ?? 0).toLocaleString('ko-KR')} P`,
    },
    {
      title: '상태',
      dataIndex: 'statusCode',
      render: (v: string) => <Tag>{v}</Tag>,
    },
    { title: '휴대폰', dataIndex: 'phoneNumber' },
    { title: 'Web ID', dataIndex: 'webId' },
    { title: '가입일', dataIndex: 'joinedAt' },
  ]

  return (
    <Space direction="vertical" style={{ width: '100%' }} size={16}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        회원조회
      </Typography.Title>

      <Card>
        <Form
          form={form}
          layout="vertical"
          onFinish={(v: any) => {
            const range = v.joinedRange as any[] | undefined
            const joinedFrom = range?.[0]?.format?.('YYYY-MM-DD')
            const joinedTo = range?.[1]?.format?.('YYYY-MM-DD')
            setFilters({
              memberNo: v.memberNo?.trim() ? v.memberNo.trim() : undefined,
              phoneNumber: v.phoneNumber?.trim() ? String(v.phoneNumber).trim() : undefined,
              name: v.name?.trim() ? v.name.trim() : undefined,
              webId: v.webId?.trim() ? v.webId.trim() : undefined,
              statusCode: v.statusCode ?? undefined,
              joinedFrom,
              joinedTo,
            })
            setPage(0)
          }}
          initialValues={{ memberNo: '', phoneNumber: '', name: '', webId: '', joinedRange: undefined, statusCode: undefined }}
          requiredMark={false}
        >
          <Space wrap align="start" size={12}>
            <Form.Item label="회원번호" name="memberNo">
              <Input placeholder="회원번호" allowClear style={{ width: 200 }} />
            </Form.Item>
            <Form.Item label="휴대폰번호" name="phoneNumber" getValueFromEvent={(e) => String(e?.target?.value ?? '').replace(/\D/g, '')}>
              <Input placeholder="01000000000" allowClear style={{ width: 200 }} inputMode="numeric" />
            </Form.Item>
            <Form.Item label="이름" name="name">
              <Input placeholder="이름" allowClear style={{ width: 180 }} />
            </Form.Item>
            <Form.Item label="WEB ID" name="webId">
              <Input placeholder="WEB ID" allowClear style={{ width: 180 }} />
            </Form.Item>
            <Form.Item label="가입일" name="joinedRange">
              <DatePicker.RangePicker />
            </Form.Item>
            <Form.Item label="상태" name="statusCode">
              <Select style={{ width: 160 }} options={STATUS_OPTIONS as any} />
            </Form.Item>
            <Form.Item label=" " colon={false}>
              <Space>
                <Button type="primary" htmlType="submit">
                  조회
                </Button>
                <Button
                  onClick={() => {
                    form.resetFields()
                    setFilters({})
                    setPage(0)
                  }}
                >
                  초기화
                </Button>
              </Space>
            </Form.Item>
          </Space>
        </Form>
      </Card>

      <Card>
        <Table<MemberSummary>
          rowKey={(r) => r.id}
          columns={columns as any}
          dataSource={query.data?.items ?? []}
          loading={query.isLoading}
          pagination={{
            current: (query.data?.page ?? 0) + 1,
            pageSize: size,
            total: query.data?.totalElements ?? 0,
            onChange: (p) => setPage(p - 1),
            showSizeChanger: false,
          }}
        />
      </Card>
    </Space>
  )
}

