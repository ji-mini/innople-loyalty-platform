import { Alert, Button, Card, Checkbox, Form, Input, Select, message } from 'antd'
import React from 'react'
import { useNavigate } from 'react-router-dom'
import { LockOutlined, PhoneOutlined } from '@ant-design/icons'
import { login } from '../../shared/auth'
import { listPublicTenants } from '../../shared/tenants'
import type { TenantPublicItem } from '../../shared/types'
import { getLoginRemember, setLoginRemember } from '../../shared/storage'
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
  const [tenantsNotice, setTenantsNotice] = React.useState<string | null>(null)
  const [form] = Form.useForm<FormValues>()

  const remembered = React.useMemo(() => getLoginRemember(), [])

  React.useEffect(() => {
    if (!remembered) return
    form.setFieldsValue({
      tenantId: remembered.tenantId,
      phoneNumber: remembered.phoneNumber,
      remember: true,
    })
  }, [form, remembered])

  React.useEffect(() => {
    let cancelled = false
    const run = async () => {
      setTenantsLoading(true)
      setTenantsNotice(null)

      const delaysMs = [0, 700, 1500]
      let lastErr: any = null
      for (let i = 0; i < delaysMs.length; i++) {
        const d = delaysMs[i]
        if (d > 0) {
          setTenantsNotice('서버 준비 중입니다. 잠시 후 자동으로 다시 시도합니다.')
          await new Promise((r) => setTimeout(r, d))
        }
        if (cancelled) return
        try {
          const r = await listPublicTenants()
          if (cancelled) return
          setTenants(r.items)
          setTenantsNotice(null)
          return
        } catch (e: any) {
          lastErr = e
        }
      }

      if (cancelled) return
      setTenants([])
      setError(lastErr?.response?.data?.message ?? lastErr?.message ?? '테넌트 목록 조회 실패')
      setTenantsNotice('서버 준비 중일 수 있습니다. 잠시 후 다시 시도하거나 Tenant ID를 직접 입력하세요.')
    }

    run().finally(() => {
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
      setLoginRemember(v.remember ? { tenantId: v.tenantId, phoneNumber: v.phoneNumber, remember: true } : null)
      nav('/dashboard', { replace: true })
    } catch (e: any) {
      const status = e?.response?.status as number | undefined
      const serverMsg = e?.response?.data?.message as string | undefined
      const errMsg =
        status === 401
          ? '휴대폰 번호 또는 비밀번호가 올바르지 않습니다.'
          : status === 400
            ? serverMsg ?? '요청 값이 올바르지 않습니다. (테넌트/입력값을 확인하세요)'
            : serverMsg ?? e?.message ?? '로그인 실패'

      setError(errMsg)
      message.error(errMsg)
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

          {tenantsNotice && <Alert type="info" message={tenantsNotice} showIcon style={{ marginBottom: 14 }} />}
          {error && <Alert type="error" message={error} showIcon style={{ marginBottom: 14 }} />}

          <Form<FormValues>
            layout="vertical"
            form={form}
            onFinish={onFinish}
            onValuesChange={(_, all) => {
              if (!all.remember) {
                setLoginRemember(null)
                return
              }
              if (all.tenantId?.trim() && all.phoneNumber?.trim()) {
                setLoginRemember({ tenantId: all.tenantId, phoneNumber: all.phoneNumber, remember: true })
              }
            }}
            initialValues={{
              tenantId: remembered?.tenantId ?? '',
              phoneNumber: remembered?.phoneNumber ?? '',
              password: '',
              remember: remembered?.remember ?? true,
            }}
            requiredMark={false}
            size="large"
          >
            <Form.Item
              label="Tenant"
              name="tenantId"
              rules={[{ required: true, message: tenants.length > 0 ? '테넌트를 선택하세요' : 'Tenant ID(UUID)를 입력하세요' }]}
            >
              {tenants.length > 0 ? (
                <Select
                  placeholder={tenantsLoading ? '테넌트 목록 불러오는 중...' : '테넌트를 선택하세요'}
                  loading={tenantsLoading}
                  options={tenants.map((t) => ({ value: t.tenantId, label: t.name }))}
                  showSearch
                  optionFilterProp="label"
                />
              ) : (
                <Input placeholder="예: 11111111-1111-1111-1111-111111111111" />
              )}
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

