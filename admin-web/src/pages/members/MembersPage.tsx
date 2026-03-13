import { Alert, Button, Card, DatePicker, Form, Input, Select, Space, Table, Tag, Typography } from 'antd'
import React from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { MemberSummary } from '../../shared/types'
import { useCommonCodes, useMemberList } from '../../shared/queries'
import { getSession } from '../../shared/storage'

export function MembersPage() {
  const nav = useNavigate()
  const role = getSession()?.role ?? 'OPERATOR'
  const statusCodes = useCommonCodes('MEMBER_STATUS')
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

  const centerTitle = (text: string) => <div style={{ textAlign: 'center' }}>{text}</div>

  const col = (title: string, extra?: object) => ({ title: centerTitle(title), align: 'center' as const, ...extra })

  const columns = [
    {
      ...col('회원번호'),
      dataIndex: 'memberNo',
      render: (v: string, r: MemberSummary) => (
        <Link to={`/members/${encodeURIComponent(r.memberNo)}`}>{v}</Link>
      ),
    },
    { ...col('이름'), dataIndex: 'name' },
    {
      ...col('상태'),
      dataIndex: 'statusCode',
      render: (v: string) => {
        const name = statusCodes.data?.find((c) => c.code === v)?.name ?? v
        return <Tag>{name}</Tag>
      },
    },
    { ...col('휴대폰'), dataIndex: 'phoneNumber' },
    { ...col('Web ID'), dataIndex: 'webId' },
    { ...col('가입일'), dataIndex: 'joinedAt' },
    {
      ...col('포인트 잔액', { width: 140 }),
      dataIndex: 'pointBalance',
      render: (v: number) => `${Number(v ?? 0).toLocaleString('ko-KR')} P`,
    },
    ...(role === 'SUPER_ADMIN'
      ? [
          {
            ...col('포인트 작업', { width: 210 }),
            key: 'actions',
            render: (_: any, r: MemberSummary) => (
              <Space>
                <Button
                  size="small"
                  onClick={() => nav(`/points/manual/earn?memberNo=${encodeURIComponent(r.memberNo)}`)}
                >
                  적립
                </Button>
                <Button
                  size="small"
                  danger
                  onClick={() => nav(`/points/manual/deduct?memberNo=${encodeURIComponent(r.memberNo)}`)}
                >
                  차감
                </Button>
              </Space>
            ),
          },
        ]
      : []),
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
              <Select
                style={{ width: 200 }}
                allowClear
                placeholder="전체"
                loading={statusCodes.isLoading}
                options={(statusCodes.data ?? []).map((c) => ({ value: c.code, label: c.name }))}
              />
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
        {query.isError && (
          <Alert
            type="error"
            message="회원 목록을 불러오지 못했습니다."
            description={
              (() => {
                const err = query.error as any
                if (err?.response?.data?.message) return err.response.data.message
                if (err?.response?.status === 400) return '요청 형식이 올바르지 않습니다. (테넌트 정보 확인)'
                if (err?.response?.status === 401) return '로그인이 필요합니다.'
                if (err?.message) return err.message
                return String(query.error)
              })()
            }
            showIcon
            action={
              <Button size="small" onClick={() => query.refetch()}>
                다시 시도
              </Button>
            }
            style={{ marginBottom: 16 }}
          />
        )}
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

