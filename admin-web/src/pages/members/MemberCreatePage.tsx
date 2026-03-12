import { Button, Card, DatePicker, Form, Input, Select, Space, Typography, message } from 'antd'
import React from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../../shared/api'
import { useCommonCodes } from '../../shared/queries'
import { PageShell } from '../common/PageShell'

type FormValues = {
  memberNo: string
  name: string
  phoneNumber?: string
  webId?: string
  joinedAt?: any
  statusCode?: string
  address?: string
}

const DEFAULT_STATUS = 'ACTIVE'

export function MemberCreatePage() {
  const nav = useNavigate()
  const [loading, setLoading] = React.useState(false)
  const [form] = Form.useForm<FormValues>()
  const statusCodes = useCommonCodes('MEMBER_STATUS')

  const onFinish = async (v: FormValues) => {
    setLoading(true)
    try {
      const joinedAt = v.joinedAt?.format?.('YYYY-MM-DD')
      await api.post('/api/v1/members', {
        memberNo: v.memberNo.trim(),
        name: v.name.trim(),
        phoneNumber: v.phoneNumber?.trim() ? v.phoneNumber.trim() : null,
        webId: v.webId?.trim() ? v.webId.trim() : null,
        joinedAt: joinedAt ?? null,
        statusCode: v.statusCode ?? DEFAULT_STATUS,
        address: v.address?.trim() ? v.address.trim() : null,
      })
      message.success('회원이 등록되었습니다.')
      nav(`/members/${encodeURIComponent(v.memberNo.trim())}`, { replace: true })
    } catch (e: any) {
      message.error(e?.response?.data?.message ?? e?.message ?? '회원 등록 실패')
    } finally {
      setLoading(false)
    }
  }

  return (
    <PageShell
      title="회원등록"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          수기로 회원을 등록합니다.
        </Typography.Text>
      }
    >
      <Card>
        <Form<FormValues>
          form={form}
          layout="vertical"
          onFinish={onFinish}
          initialValues={{ memberNo: '', name: '', phoneNumber: '', webId: '', joinedAt: null, statusCode: DEFAULT_STATUS, address: '' }}
          requiredMark={false}
        >
          <Space wrap size={16} align="start">
            <Form.Item label="회원번호" name="memberNo" rules={[{ required: true, message: '회원번호를 입력하세요' }]}>
              <Input placeholder="예: 10000001" style={{ width: 240 }} />
            </Form.Item>

            <Form.Item label="이름" name="name" rules={[{ required: true, message: '이름을 입력하세요' }]}>
              <Input placeholder="예: 홍길동" style={{ width: 220 }} />
            </Form.Item>

            <Form.Item
              label="휴대폰번호"
              name="phoneNumber"
              getValueFromEvent={(e) => String(e?.target?.value ?? '').replace(/\D/g, '')}
            >
              <Input placeholder="01000000000" style={{ width: 220 }} allowClear inputMode="numeric" />
            </Form.Item>

            <Form.Item label="WEB ID" name="webId">
              <Input placeholder="예: web_123" style={{ width: 220 }} allowClear />
            </Form.Item>

            <Form.Item label="가입일" name="joinedAt">
              <DatePicker style={{ width: 180 }} />
            </Form.Item>

            <Form.Item label="상태" name="statusCode">
              <Select
                style={{ width: 200 }}
                loading={statusCodes.isLoading}
                options={(statusCodes.data ?? []).map((c) => ({ value: c.code, label: `${c.code} (${c.name})` }))}
              />
            </Form.Item>
          </Space>

          <Form.Item label="주소(선택)" name="address">
            <Input.TextArea placeholder="주소" autoSize={{ minRows: 2, maxRows: 4 }} />
          </Form.Item>

          <Space>
            <Button type="primary" htmlType="submit" loading={loading}>
              등록
            </Button>
            <Button
              onClick={() => {
                form.resetFields()
              }}
            >
              초기화
            </Button>
            <Button onClick={() => nav('/members')}>목록으로</Button>
          </Space>
        </Form>
      </Card>
    </PageShell>
  )
}

