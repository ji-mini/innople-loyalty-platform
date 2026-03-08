import { Card, Descriptions, Divider, Space, Table, Tag, Typography } from 'antd'
import React from 'react'
import { useParams } from 'react-router-dom'
import { useMemberDetail, useMemberLedgers } from '../../shared/queries'
import type { MemberLedger } from '../../shared/types'

export function MemberDetailPage() {
  const params = useParams()
  const memberNo = params.memberNo ?? ''

  const detail = useMemberDetail(memberNo)
  const ledgers = useMemberLedgers(memberNo, 100)

  const ledgerCols = [
    { title: '일시', dataIndex: 'createdAt' },
    { title: '이벤트', dataIndex: 'eventType', render: (v: string) => <Tag>{v}</Tag> },
    { title: '상태(전)', dataIndex: 'statusCodeBefore' },
    { title: '상태(후)', dataIndex: 'statusCodeAfter' },
  ]

  return (
    <Space direction="vertical" style={{ width: '100%' }} size={16}>
      <Typography.Title level={4} style={{ margin: 0 }}>
        회원 상세
      </Typography.Title>

      <Card loading={detail.isLoading}>
        <Descriptions bordered size="small" column={2}>
          <Descriptions.Item label="회원번호">{detail.data?.memberNo}</Descriptions.Item>
          <Descriptions.Item label="이름">{detail.data?.name}</Descriptions.Item>
          <Descriptions.Item label="상태">{detail.data?.statusCode}</Descriptions.Item>
          <Descriptions.Item label="가입일">{detail.data?.joinedAt}</Descriptions.Item>
          <Descriptions.Item label="생년월일">{detail.data?.birthDate ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="양/음력">{detail.data?.calendarType ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="성별">{detail.data?.gender ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="휴대폰">{detail.data?.phoneNumber ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="주소" span={2}>
            {detail.data?.address ?? '-'}
          </Descriptions.Item>
          <Descriptions.Item label="Web ID">{detail.data?.webId ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="CI">{detail.data?.ci ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="휴면일자">{detail.data?.dormantAt ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="탈퇴일자">{detail.data?.withdrawnAt ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="기념일" span={2}>
            {detail.data?.anniversaries ?? '-'}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      <Divider style={{ margin: 0 }} />

      <Card title="회원 원장(최근 100건)" loading={ledgers.isLoading}>
        <Table<MemberLedger>
          rowKey={(r) => r.id}
          columns={ledgerCols as any}
          dataSource={ledgers.data ?? []}
          pagination={false}
          size="small"
        />
      </Card>
    </Space>
  )
}

