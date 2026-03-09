import { Alert, Button, Card, Form, Input, Typography, message } from 'antd'
import React from 'react'
import { useNavigate } from 'react-router-dom'
import { KeyOutlined, LockOutlined, MailOutlined, UserOutlined } from '@ant-design/icons'
import { registerAdmin } from '../../shared/auth'

type FormValues = {
  tenantId: string
  name: string
  email: string
  password: string
  passwordConfirm: string
}

export function AdminSignUpPage() {
  const nav = useNavigate()
  const [error, setError] = React.useState<string | null>(null)
  const [loading, setLoading] = React.useState(false)

  const onFinish = async (v: FormValues) => {
    setError(null)
    setLoading(true)
    try {
      await registerAdmin(v.tenantId, { email: v.email, name: v.name, password: v.password })
      message.success('직원 계정이 생성되었습니다. 로그인 해주세요.')
      nav('/login', { replace: true })
    } catch (e: any) {
      setError(e?.response?.data?.message ?? e?.message ?? '계정 생성 실패')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ minHeight: '100vh', display: 'grid', placeItems: 'center', padding: 24 }}>
      <Card style={{ width: 460, maxWidth: '100%' }} bordered={false}>
        <Typography.Title level={3} style={{ marginTop: 0, marginBottom: 6 }}>
          직원 계정 등록
        </Typography.Title>
        <Typography.Paragraph type="secondary" style={{ marginTop: 0 }}>
          테넌트별 어드민(직원) 계정을 생성합니다.
        </Typography.Paragraph>

        {error && <Alert type="error" message={error} showIcon style={{ marginBottom: 14 }} />}

        <Form<FormValues>
          layout="vertical"
          onFinish={onFinish}
          initialValues={{ tenantId: '', name: '', email: '', password: '', passwordConfirm: '' }}
          requiredMark={false}
          size="large"
        >
          <Form.Item
            label="Tenant ID"
            name="tenantId"
            rules={[{ required: true, message: 'Tenant ID(UUID)를 입력하세요' }]}
          >
            <Input placeholder="예: 11111111-1111-1111-1111-111111111111" prefix={<KeyOutlined />} />
          </Form.Item>

          <Form.Item label="이름" name="name" rules={[{ required: true, message: '이름을 입력하세요' }]}>
            <Input placeholder="홍길동" prefix={<UserOutlined />} />
          </Form.Item>

          <Form.Item label="이메일" name="email" rules={[{ required: true }, { type: 'email' }]}>
            <Input placeholder="admin@company.com" prefix={<MailOutlined />} />
          </Form.Item>

          <Form.Item label="비밀번호" name="password" rules={[{ required: true, message: '비밀번호를 입력하세요' }]}>
            <Input.Password placeholder="비밀번호" prefix={<LockOutlined />} />
          </Form.Item>

          <Form.Item
            label="비밀번호 확인"
            name="passwordConfirm"
            dependencies={['password']}
            rules={[
              { required: true, message: '비밀번호 확인을 입력하세요' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) return Promise.resolve()
                  return Promise.reject(new Error('비밀번호가 일치하지 않습니다'))
                },
              }),
            ]}
          >
            <Input.Password placeholder="비밀번호 확인" prefix={<LockOutlined />} />
          </Form.Item>

          <Button type="primary" htmlType="submit" loading={loading} block style={{ marginTop: 6 }}>
            계정 생성
          </Button>

          <Button block style={{ marginTop: 10 }} onClick={() => nav('/login')}>
            로그인으로 돌아가기
          </Button>
        </Form>
      </Card>
    </div>
  )
}

