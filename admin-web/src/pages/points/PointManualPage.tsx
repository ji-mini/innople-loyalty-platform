import { Button, Card, Form, Input, InputNumber, Radio, Select, Space, Typography, message } from 'antd'
import React from 'react'
import { PageShell } from '../common/PageShell'

type FormValues = {
  memberNo: string
  brand: string
  type: 'EARN' | 'USE' | 'EXPIRE'
  amount: number
  reason: string
}

export function PointManualPage() {
  const [loading, setLoading] = React.useState(false)

  const onFinish = async (v: FormValues) => {
    setLoading(true)
    try {
      // TODO: connect to backend manual point operation API
      message.info('포인트 수기등록 API는 준비 중입니다.')
      console.log('[point-manual]', v)
    } finally {
      setLoading(false)
    }
  }

  return (
    <PageShell
      title="포인트 수기등록"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          운영자가 직접 적립/사용/소멸 처리를 합니다.
        </Typography.Text>
      }
    >
      <Card>
        <Form<FormValues>
          layout="vertical"
          onFinish={onFinish}
          initialValues={{ memberNo: '', brand: '', type: 'EARN', amount: 0, reason: '' }}
          requiredMark={false}
        >
          <Space size={16} wrap align="start">
            <Form.Item label="회원번호" name="memberNo" rules={[{ required: true, message: '회원번호를 입력하세요' }]}>
              <Input placeholder="예: 12345" style={{ width: 260 }} />
            </Form.Item>

            <Form.Item label="브랜드" name="brand" rules={[{ required: true, message: '브랜드를 선택하세요' }]}>
              <Select
                placeholder="브랜드 선택"
                style={{ width: 220 }}
                options={[
                  { value: 'SPAO', label: 'SPAO' },
                  { value: 'MIXXO', label: 'MIXXO' },
                ]}
              />
            </Form.Item>

            <Form.Item label="처리구분" name="type" rules={[{ required: true }]}>
              <Radio.Group
                options={[
                  { value: 'EARN', label: '적립' },
                  { value: 'USE', label: '사용' },
                  { value: 'EXPIRE', label: '소멸' },
                ]}
              />
            </Form.Item>
          </Space>

          <Space size={16} wrap align="start">
            <Form.Item label="포인트 금액" name="amount" rules={[{ required: true, message: '포인트 금액을 입력하세요' }]}>
              <InputNumber min={1} step={1} style={{ width: 220 }} addonAfter="P" />
            </Form.Item>

            <Form.Item label="처리사유" name="reason" rules={[{ required: true, message: '처리사유를 입력하세요' }]}>
              <Input placeholder="예: 고객 CS 보상" style={{ width: 420, maxWidth: '100%' }} />
            </Form.Item>
          </Space>

          <Button type="primary" htmlType="submit" loading={loading}>
            처리
          </Button>
        </Form>
      </Card>
    </PageShell>
  )
}

