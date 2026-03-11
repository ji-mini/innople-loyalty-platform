import { Button, Card, Form, Input, InputNumber, Space, Typography, message } from 'antd'
import React from 'react'
import { PageShell } from '../common/PageShell'

type FormValues = {
  name: string
  discountAmount: number
  validDays: number
  quantity: number
  condition: string
}

export function CouponIssuePage() {
  const [loading, setLoading] = React.useState(false)

  const onFinish = async (v: FormValues) => {
    setLoading(true)
    try {
      // TODO: connect to backend coupon creation/issuance APIs
      message.info('쿠폰 발행 API는 준비 중입니다.')
      console.log('[coupon-issue]', v)
    } finally {
      setLoading(false)
    }
  }

  return (
    <PageShell
      title="쿠폰 발행"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          쿠폰을 생성/발행합니다.
        </Typography.Text>
      }
    >
      <Card>
        <Form<FormValues>
          layout="vertical"
          onFinish={onFinish}
          initialValues={{ name: '', discountAmount: 0, validDays: 30, quantity: 1, condition: '' }}
          requiredMark={false}
        >
          <Space wrap size={16} align="start">
            <Form.Item label="쿠폰명" name="name" rules={[{ required: true, message: '쿠폰명을 입력하세요' }]}>
              <Input placeholder="예: 신규가입 3,000원 할인" style={{ width: 360, maxWidth: '100%' }} />
            </Form.Item>
            <Form.Item
              label="할인금액"
              name="discountAmount"
              rules={[{ required: true, message: '할인금액을 입력하세요' }]}
            >
              <InputNumber min={0} step={100} style={{ width: 220 }} addonAfter="원" />
            </Form.Item>
          </Space>

          <Space wrap size={16} align="start">
            <Form.Item label="유효기간(일)" name="validDays" rules={[{ required: true }]}>
              <InputNumber min={1} step={1} style={{ width: 220 }} />
            </Form.Item>
            <Form.Item label="발급수량" name="quantity" rules={[{ required: true }]}>
              <InputNumber min={1} step={1} style={{ width: 220 }} />
            </Form.Item>
            <Form.Item label="사용조건" name="condition">
              <Input placeholder="예: 3만원 이상 구매 시" style={{ width: 420, maxWidth: '100%' }} />
            </Form.Item>
          </Space>

          <Button type="primary" htmlType="submit" loading={loading}>
            쿠폰 생성
          </Button>
        </Form>
      </Card>
    </PageShell>
  )
}

