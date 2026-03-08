import { Alert, Button, Card, Form, Input, Typography } from 'antd'
import React from 'react'
import { useNavigate } from 'react-router-dom'
import { login } from '../../shared/auth'

type FormValues = {
  tenantId: string
  email: string
  password: string
}

export function LoginPage() {
  const nav = useNavigate()
  const [error, setError] = React.useState<string | null>(null)
  const [loading, setLoading] = React.useState(false)

  const onFinish = async (v: FormValues) => {
    setError(null)
    setLoading(true)
    try {
      await login(v.tenantId, { email: v.email, password: v.password })
      nav('/members', { replace: true })
    } catch (e: any) {
      setError(e?.response?.data?.message ?? e?.message ?? '로그인 실패')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ minHeight: '100vh', display: 'grid', placeItems: 'center', padding: 24, background: '#f5f5f5' }}>
      <Card style={{ width: 420 }} bordered>
        <Typography.Title level={3} style={{ marginTop: 0 }}>
          어드민 로그인
        </Typography.Title>
        <Typography.Paragraph type="secondary" style={{ marginTop: -8 }}>
          직원용 계정으로 로그인합니다.
        </Typography.Paragraph>

        {error && <Alert type="error" message={error} showIcon style={{ marginBottom: 16 }} />}

        <Form<FormValues>
          layout="vertical"
          onFinish={onFinish}
          initialValues={{ tenantId: '', email: '', password: '' }}
          requiredMark={false}
        >
          <Form.Item
            label="Tenant ID"
            name="tenantId"
            rules={[{ required: true, message: 'Tenant ID(UUID)를 입력하세요' }]}
          >
            <Input placeholder="예: 11111111-1111-1111-1111-111111111111" />
          </Form.Item>

          <Form.Item label="이메일" name="email" rules={[{ required: true }, { type: 'email' }]}>
            <Input placeholder="admin@company.com" />
          </Form.Item>

          <Form.Item label="비밀번호" name="password" rules={[{ required: true }]}>
            <Input.Password placeholder="비밀번호" />
          </Form.Item>

          <Button type="primary" htmlType="submit" loading={loading} block>
            로그인
          </Button>
        </Form>
      </Card>
    </div>
  )
}

