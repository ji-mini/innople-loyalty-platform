import { Alert, Button, Card, Checkbox, Form, Input, Select, message } from 'antd'
import React from 'react'
import { useNavigate } from 'react-router-dom'
import { LockOutlined, PhoneOutlined } from '@ant-design/icons'
import { login } from '../../shared/auth'
import { listPublicTenants } from '../../shared/tenants'
import type { TenantPublicItem } from '../../shared/types'
import styles from './LoginPage.module.css'

type FormValues = {
  tenantId: string
  phoneNumber: string
  password: string
  remember: boolean
}

export function LoginPage() {
  const nav = useNavigate()
  const [error, setError] = React.useState<string | null>(null)
  const [loading, setLoading] = React.useState(false)
  const [tenantsLoading, setTenantsLoading] = React.useState(false)
  const [tenants, setTenants] = React.useState<TenantPublicItem[]>([])

  React.useEffect(() => {
    let cancelled = false
    setTenantsLoading(true)
    listPublicTenants()
      .then((r) => {
        if (cancelled) return
        setTenants(r.items)
      })
      .catch((e: any) => {
        if (cancelled) return
        setError(e?.response?.data?.message ?? e?.message ?? '테넌트 목록 조회 실패')
      })
      .finally(() => {
        if (cancelled) return
        setTenantsLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  const onFinish = async (v: FormValues) => {
    setError(null)
    setLoading(true)
    try {
      await login(v.tenantId, { phoneNumber: v.phoneNumber, password: v.password })
      nav('/members', { replace: true })
    } catch (e: any) {
      setError(e?.response?.data?.message ?? e?.message ?? '로그인 실패')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.clouds} />

      <svg className={styles.decorPlane} viewBox="0 0 120 120" aria-hidden="true">
        <path
          d="M14 55.5c-.8-.3-1.2-1.1-.9-1.9.2-.5.6-.8 1.1-.9l88-22.5c1.2-.3 2.2.8 1.8 1.9L77 106.5c-.2.6-.8 1.1-1.5 1.1-.7 0-1.3-.4-1.5-1.1L60 72 14 55.5Z"
          fill="rgba(255,139,122,0.55)"
        />
        <path
          d="M103.2 31.1 60.2 72l13.6 33.4 29.4-74.3Z"
          fill="rgba(139,216,194,0.55)"
        />
      </svg>

      <svg className={styles.decorGift} viewBox="0 0 160 160" aria-hidden="true">
        <rect x="26" y="60" width="108" height="80" rx="18" fill="rgba(155,225,255,0.55)" />
        <rect x="22" y="50" width="116" height="22" rx="11" fill="rgba(255,180,210,0.55)" />
        <rect x="74" y="50" width="12" height="90" rx="6" fill="rgba(255,255,255,0.55)" />
        <path
          d="M80 50c-10-2-18-8-20-16-2-8 3-14 11-14 10 0 17 10 19 20 2-10 9-20 19-20 8 0 13 6 11 14-2 8-10 14-20 16h-20Z"
          fill="rgba(255,139,122,0.45)"
        />
      </svg>

      <div className={styles.wrap}>
        <section className={styles.hero}>
          <h1 className={styles.brand}>INNOPLE Membership &amp; Point</h1>
          <p className={styles.tagline}>테넌트 기반 멀티테넌시로, 더 안전하고 유연한 운영을 지원합니다.</p>
          <div className={styles.chips}>
            <div className={styles.chip}>테넌트 기반 멀티테넌시</div>
            <div className={styles.chip}>회원 조회 · 관리</div>
            <div className={styles.chip}>포인트/거래 이력</div>
          </div>
        </section>

        <Card className={styles.card} bordered={false} bodyStyle={{ padding: 24 }}>
          <div className={styles.cardHeader}>
            <p className={styles.cardTitle}>Admin Login</p>
            <p className={styles.cardSub}>직원 계정으로 로그인합니다.</p>
          </div>

          {error && <Alert type="error" message={error} showIcon style={{ marginBottom: 14 }} />}

          <Form<FormValues>
            layout="vertical"
            onFinish={onFinish}
            initialValues={{ tenantId: '', phoneNumber: '', password: '', remember: true }}
            requiredMark={false}
            size="large"
          >
            <Form.Item
              label="Tenant"
              name="tenantId"
              rules={[{ required: true, message: '테넌트를 선택하세요' }]}
            >
              <Select
                placeholder={tenantsLoading ? '테넌트 목록 불러오는 중...' : '테넌트를 선택하세요'}
                loading={tenantsLoading}
                options={tenants.map((t) => ({ value: t.tenantId, label: t.name }))}
                showSearch
                optionFilterProp="label"
              />
            </Form.Item>

            <Form.Item label="휴대폰 번호" name="phoneNumber" rules={[{ required: true, message: '휴대폰 번호를 입력하세요' }]}>
              <Input placeholder="예: 010-1234-5678" prefix={<PhoneOutlined style={{ opacity: 0.55 }} />} />
            </Form.Item>

            <Form.Item label="Password" name="password" rules={[{ required: true }]}>
              <Input.Password placeholder="비밀번호" prefix={<LockOutlined style={{ opacity: 0.55 }} />} />
            </Form.Item>

            <div className={styles.actionsRow}>
              <Form.Item name="remember" valuePropName="checked" style={{ marginBottom: 0 }}>
                <Checkbox>Remember me</Checkbox>
              </Form.Item>
              <Button
                type="link"
                className={styles.linkBtn}
                onClick={() => message.info('비밀번호 재설정은 준비 중입니다.')}
              >
                Forgot password?
              </Button>
            </div>

            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              className={styles.primaryBtn}
              style={{ marginTop: 10 }}
            >
              Login
            </Button>

            <Button
              block
              className={styles.secondaryBtn}
              style={{ marginTop: 10 }}
              onClick={() => nav('/signup')}
            >
              Sign up
            </Button>
          </Form>

          <div className={styles.footer}>© {new Date().getFullYear()} INNOPLE</div>
        </Card>
      </div>
    </div>
  )
}

