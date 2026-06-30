import { Alert, Button, Checkbox, Form, Input, Select, message } from 'antd'
import React from 'react'
import { useNavigate } from 'react-router-dom'
import { LockOutlined, PhoneOutlined } from '@ant-design/icons'
import { login } from '../../shared/auth'
import { listPublicTenants } from '../../shared/tenants'
import type { TenantPublicItem } from '../../shared/types'
import { getLoginRemember, setLoginRemember } from '../../shared/storage'
import { AuthLayout } from '../auth/AuthLayout'
import styles from '../auth/AuthLayout.module.css'

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
      const selectedTenant = tenants.find((item) => item.tenantId === v.tenantId)
      await login(v.tenantId, { phoneNumber: v.phoneNumber, password: v.password }, selectedTenant?.name)
      setLoginRemember(v.remember ? { tenantId: v.tenantId, phoneNumber: v.phoneNumber, remember: true } : null)
      nav('/dashboard', { replace: true })
    } catch (e: any) {
      const status = e?.response?.status as number | undefined
      const serverMsg = e?.response?.data?.message as string | undefined
      const errMsg =
        status === 401
          ? serverMsg ?? '휴대폰 번호 또는 비밀번호가 올바르지 않습니다.'
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
    <AuthLayout cardTitle="Admin Login" cardSubtitle="직원 계정으로 로그인합니다.">
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

        <Form.Item
          label="휴대폰 번호"
          name="phoneNumber"
          rules={[{ required: true, message: '휴대폰 번호를 입력하세요' }]}
          getValueFromEvent={(e) => String(e?.target?.value ?? '').replace(/\D/g, '')}
        >
          <Input placeholder="예: 01000000000" prefix={<PhoneOutlined style={{ opacity: 0.55 }} />} inputMode="numeric" />
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
    </AuthLayout>
  )
}

