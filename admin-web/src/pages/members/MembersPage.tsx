import { Card, Input, Select, Space, Table, Tag, Typography } from 'antd'
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
  const [keyword, setKeyword] = React.useState<string>('')
  const [statusCode, setStatusCode] = React.useState<string | undefined>(undefined)
  const [page, setPage] = React.useState(0)
  const size = 20

  const query = useMemberList({
    keyword: keyword.trim() ? keyword.trim() : undefined,
    statusCode,
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
        회원
      </Typography.Title>

      <Card>
        <Space wrap>
          <Input
            placeholder="회원번호/이름/휴대폰/WebId/CI"
            value={keyword}
            onChange={(e) => {
              setKeyword(e.target.value)
              setPage(0)
            }}
            style={{ width: 320 }}
            allowClear
          />
          <Select
            value={statusCode}
            onChange={(v) => {
              setStatusCode(v)
              setPage(0)
            }}
            style={{ width: 180 }}
            options={STATUS_OPTIONS as any}
          />
        </Space>
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

