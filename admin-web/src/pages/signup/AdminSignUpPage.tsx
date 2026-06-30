import { Alert, Button, Form, Input, Select, message } from 'antd'
import React from 'react'
import { useNavigate } from 'react-router-dom'
import { LockOutlined, MailOutlined, PhoneOutlined, ShopOutlined, UserAddOutlined, UserOutlined } from '@ant-design/icons'
import { registerAdmin } from '../../shared/auth'
import { listPublicTenants } from '../../shared/tenants'
import type { TenantPublicItem } from '../../shared/types'
import styles from './AdminSignUpPage.module.css'

type FormValues = {
  tenantId: string
  name: string
  phoneNumber: string
  email: string
  password: string
  passwordConfirm: string
}

export function AdminSignUpPage() {
  const nav = useNavigate()
  const [error, setError] = React.useState<string | null>(null)
  const [loading, setLoading] = React.useState(false)
  const [tenantsLoading, setTenantsLoading] = React.useState(false)
  const [tenants, setTenants] = React.useState<TenantPublicItem[]>([])
  const [tenantsNotice, setTenantsNotice] = React.useState<string | null>(null)

  const onFinishFailed = (info: any) => {
    const first = info?.errorFields?.[0]
    const msg = first?.errors?.[0] ?? '입력값을 확인해 주세요.'
    setError(msg)
    message.error(msg)
  }

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
      await registerAdmin(v.tenantId, {
        phoneNumber: v.phoneNumber,
        email: v.email,
        name: v.name,
        password: v.password,
      })
      message.success('직원 계정이 생성되었습니다. 로그인 해주세요.')
      nav('/login', { replace: true })
    } catch (e: any) {
      setError(e?.response?.data?.message ?? e?.message ?? '계정 생성 실패')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={`brand-gradient-bg ${styles.page}`}>
      <div className={styles.card}>
        <div className={styles.header}>
          <div className={styles.badge}>
            <UserAddOutlined />
          </div>
          <h1 className={styles.title}>직원 계정 등록</h1>
          <p className={styles.subtitle}>테넌트별 어드민(직원) 계정을 생성합니다.</p>
        </div>

        <hr className={styles.headerDivider} />

        {tenantsNotice && <Alert type="info" message={tenantsNotice} showIcon style={{ marginBottom: 14 }} />}
        {error && <Alert type="error" message={error} showIcon style={{ marginBottom: 14 }} />}

        <Form<FormValues>
          layout="vertical"
          onFinish={onFinish}
          onFinishFailed={onFinishFailed}
          initialValues={{ tenantId: '', name: '', phoneNumber: '', email: '', password: '', passwordConfirm: '' }}
          requiredMark={false}
          size="large"
          scrollToFirstError
        >
          <div className={styles.sectionRow}>
            <span className={styles.sectionChip}>기본 정보</span>
            <span className={styles.sectionLine} />
          </div>

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
                suffixIcon={<ShopOutlined style={{ opacity: 0.55 }} />}
              />
            ) : (
              <Input placeholder="예: 11111111-1111-1111-1111-111111111111" prefix={<ShopOutlined style={{ opacity: 0.55 }} />} />
            )}
          </Form.Item>

          <Form.Item label="이름" name="name" rules={[{ required: true, message: '이름을 입력하세요' }]}>
            <Input placeholder="홍길동" prefix={<UserOutlined style={{ opacity: 0.55 }} />} />
          </Form.Item>

          <Form.Item
            label="휴대폰 번호"
            name="phoneNumber"
            rules={[{ required: true, message: '휴대폰 번호를 입력하세요' }]}
            getValueFromEvent={(e) => String(e?.target?.value ?? '').replace(/\D/g, '')}
          >
            <Input placeholder="예: 01000000000" prefix={<PhoneOutlined style={{ opacity: 0.55 }} />} inputMode="numeric" />
          </Form.Item>

          <div className={styles.sectionRow}>
            <span className={styles.sectionChip}>계정 정보</span>
            <span className={styles.sectionLine} />
          </div>

          <Form.Item label="이메일(선택)" name="email" rules={[{ type: 'email' }]}>
            <Input placeholder="admin@company.com" prefix={<MailOutlined style={{ opacity: 0.55 }} />} />
          </Form.Item>

          <Form.Item label="비밀번호" name="password" rules={[{ required: true, message: '비밀번호를 입력하세요' }]}>
            <Input.Password placeholder="비밀번호" prefix={<LockOutlined style={{ opacity: 0.55 }} />} />
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
            <Input.Password placeholder="비밀번호 확인" prefix={<LockOutlined style={{ opacity: 0.55 }} />} />
          </Form.Item>

          <Button type="primary" htmlType="submit" loading={loading} block className={styles.primaryBtn}>
            계정 생성
          </Button>

          <Button block className={styles.secondaryBtn} onClick={() => nav('/login')}>
            로그인으로 돌아가기
          </Button>
        </Form>
      </div>
    </div>
  )
}

