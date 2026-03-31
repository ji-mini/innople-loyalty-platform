import {
  BarChartOutlined,
  DashboardOutlined,
  GiftOutlined,
  LogoutOutlined,
  SettingOutlined,
  ShopOutlined,
  StarOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons'
import { Button, Layout, Menu, message, Select, Space, Tooltip, Typography } from 'antd'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import React from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { extendSession, getSession, setSession } from '../../shared/storage'
import { useSessionActivity } from '../../shared/useSessionActivity'
import { useSessionRemaining } from '../../shared/useSessionRemaining'
import { logout } from '../../shared/auth'
import { atLeast } from '../../shared/roles'
import { getTenantById, listPublicTenants } from '../../shared/tenants'
import { AdminBreadcrumbs } from './AdminBreadcrumbs'
import { BrandHeader } from '../../app/BrandHeader'

type MenuKey =
  | 'dashboard'
  | 'members.list'
  | 'members.register'
  | 'members.grades'
  | 'members.clubs'
  | 'points.policies'
  | 'points.manualEarn'
  | 'points.manualDeduct'
  | 'points.history'
  | 'points.expiry'
  | 'stamps.policies'
  | 'stamps.manual'
  | 'stamps.history'
  | 'coupons.issue'
  | 'coupons.history'
  | 'employees'
  | 'reports.points'
  | 'reports.members'
  | 'tenants.list'
  | 'tenants.admins'
  | 'system.users'
  | 'system.commonCodes'
  | 'system.permissions'
  | 'system.logs'

const KEY_TO_PATH: Record<MenuKey, string> = {
  dashboard: '/dashboard',
  'members.list': '/members',
  'members.register': '/members/register',
  'members.grades': '/member-grades',
  'members.clubs': '/clubs',
  'points.policies': '/points/policies',
  'points.manualEarn': '/points/manual/earn',
  'points.manualDeduct': '/points/manual/deduct',
  'points.history': '/points/history',
  'points.expiry': '/points/expiry',
  'stamps.policies': '/stamps/policies',
  'stamps.manual': '/stamps/manual',
  'stamps.history': '/stamps/history',
  'coupons.issue': '/coupons/issue',
  'coupons.history': '/coupons/history',
  employees: '/employees',
  'reports.points': '/reports/points',
  'reports.members': '/reports/members',
  'tenants.list': '/tenants',
  'tenants.admins': '/tenants/admins',
  'system.users': '/system/users',
  'system.commonCodes': '/system/common-codes',
  'system.permissions': '/system/permissions',
  'system.logs': '/system/logs',
}

function topMenuLabel(text: string) {
  return <span style={{ fontWeight: 700 }}>{text}</span>
}

function pickSelectedKey(pathname: string): MenuKey {
  if (pathname === '/' || pathname.startsWith('/dashboard')) return 'dashboard'
  if (pathname.startsWith('/members/register')) return 'members.register'
  if (pathname === '/members' || pathname.startsWith('/members/')) return 'members.list'
  if (pathname.startsWith('/member-grades')) return 'members.grades'
  if (pathname.startsWith('/clubs')) return 'members.clubs'

  if (pathname.startsWith('/points/policies')) return 'points.policies'
  if (pathname.startsWith('/points/manual/earn')) return 'points.manualEarn'
  if (pathname.startsWith('/points/manual/deduct')) return 'points.manualDeduct'
  if (pathname.startsWith('/points/manual')) return 'points.manualEarn'
  if (pathname.startsWith('/points/history')) return 'points.history'
  if (pathname.startsWith('/points/expiry')) return 'points.expiry'

  if (pathname.startsWith('/stamps/policies')) return 'stamps.policies'
  if (pathname.startsWith('/stamps/manual')) return 'stamps.manual'
  if (pathname.startsWith('/stamps/history')) return 'stamps.history'

  if (pathname.startsWith('/employees')) return 'employees'

  if (pathname.startsWith('/coupons/issue')) return 'coupons.issue'
  if (pathname.startsWith('/coupons/history')) return 'coupons.history'

  if (pathname.startsWith('/reports/points')) return 'reports.points'
  if (pathname.startsWith('/reports/members')) return 'reports.members'

  if (pathname === '/tenants' || pathname.startsWith('/tenants/')) {
    if (pathname.startsWith('/tenants/admins')) return 'tenants.admins'
    return 'tenants.list'
  }

  if (pathname.startsWith('/system/users') || pathname.startsWith('/system/admins')) return 'system.users'
  if (pathname.startsWith('/system/common-codes')) return 'system.commonCodes'
  if (pathname.startsWith('/system/permissions')) return 'system.permissions'
  if (pathname.startsWith('/system/logs')) return 'system.logs'

  return 'dashboard'
}

export function AdminLayout() {
  const nav = useNavigate()
  const loc = useLocation()
  const qc = useQueryClient()
  const [session, setSessionState] = React.useState(() => getSession())
  const role = session?.role ?? 'OPERATOR'

  useSessionActivity(!!session)
  const sessionRemaining = useSessionRemaining(!!session)

  const tenantsQuery = useQuery({
    queryKey: ['public', 'tenants'],
    queryFn: listPublicTenants,
  })

  const tenantId = session?.tenantId
  const tenantInList = React.useMemo(
    () => tenantsQuery.data?.items?.find((t) => t.tenantId === tenantId),
    [tenantsQuery.data?.items, tenantId]
  )

  const tenantByIdQuery = useQuery({
    queryKey: ['public', 'tenants', tenantId],
    queryFn: () => getTenantById(tenantId!),
    enabled: !!tenantId && !tenantInList,
  })

  const tenantName = React.useMemo(() => {
    if (!tenantId) return '-'
    if (session?.tenantName?.trim()) return session.tenantName
    if (tenantInList) return tenantInList.name
    if (tenantByIdQuery.data?.name) return tenantByIdQuery.data.name
    if (tenantsQuery.isLoading || tenantByIdQuery.isLoading) return '로딩 중...'
    return tenantId
  }, [session?.tenantName, tenantId, tenantInList, tenantByIdQuery.data?.name, tenantByIdQuery.isLoading, tenantsQuery.isLoading])

  const tenantSelectOptions = React.useMemo(() => {
    const items = tenantsQuery.data?.items ?? []
    const base = items.map((t) => ({ value: t.tenantId, label: t.name }))
    const hasCurrent = session?.tenantId && base.some((o) => o.value === session.tenantId)
    if (hasCurrent || !session?.tenantId) return base
    return [...base, { value: session.tenantId, label: tenantName }]
  }, [tenantsQuery.data?.items, session?.tenantId, tenantName])

  const selectedKey = React.useMemo<MenuKey>(() => pickSelectedKey(loc.pathname), [loc.pathname])
  const groupKey = selectedKey.includes('.') ? selectedKey.split('.')[0] : undefined
  const showBreadcrumbs = selectedKey !== 'dashboard'
  const [openKeys, setOpenKeys] = React.useState<string[]>(groupKey ? [groupKey] : [])

  React.useEffect(() => {
    if (!groupKey) return
    setOpenKeys((prev) => (prev.includes(groupKey) ? prev : [...prev, groupKey]))
  }, [groupKey])

  const onLogout = () => {
    logout()
    nav('/login', { replace: true })
  }

  const onChangeTenant = (tenantId: string) => {
    if (!session) return
    if (!atLeast(role, 'SUPER_ADMIN')) return
    if (!tenantId || tenantId === session.tenantId) return

    const selectedOption = tenantSelectOptions.find((option) => option.value === tenantId)
    const next = { ...session, tenantId, tenantName: typeof selectedOption?.label === 'string' ? selectedOption.label : session.tenantName }
    setSession(next)
    setSessionState(next)
    qc.clear()
    nav('/dashboard', { replace: true })
  }

  return (
    <Layout style={{ height: '100vh', overflow: 'hidden' }}>
      <Layout.Sider width={240} theme="light" style={{ height: '100vh' }}>
        <BrandHeader variant="sider" />
        <div style={{ height: 'calc(100vh - 58px)', overflow: 'auto' }}>
          <Menu
            mode="inline"
            selectedKeys={[selectedKey]}
            openKeys={openKeys}
            onOpenChange={(keys) => setOpenKeys(keys as string[])}
            style={{ paddingTop: 8 }}
            items={[
              {
                key: 'dashboard',
                icon: <DashboardOutlined />,
                label: topMenuLabel('대시보드'),
                onClick: () => nav(KEY_TO_PATH.dashboard),
              },
              {
                key: 'members',
                icon: <TeamOutlined />,
                label: topMenuLabel('회원관리'),
                children: [
                  {
                    key: 'members.list',
                    label: '회원조회',
                    onClick: () => nav(KEY_TO_PATH['members.list']),
                  },
                  ...(atLeast(role, 'SUPER_ADMIN')
                    ? [
                        {
                          key: 'members.register',
                          label: '회원등록',
                          onClick: () => nav(KEY_TO_PATH['members.register']),
                        },
                      ]
                    : []),
                  ...(atLeast(role, 'ADMIN')
                    ? [
                        {
                          key: 'members.grades',
                          label: '등급 관리',
                          onClick: () => nav(KEY_TO_PATH['members.grades']),
                        },
                        {
                          key: 'members.clubs',
                          label: '클럽 관리',
                          onClick: () => nav(KEY_TO_PATH['members.clubs']),
                        },
                      ]
                    : []),
                ],
              },
              {
                key: 'points',
                icon: <ShopOutlined />,
                label: topMenuLabel('포인트관리'),
                children: [
                  ...(atLeast(role, 'ADMIN') ? [{ key: 'points.policies', label: '포인트 정책 관리', onClick: () => nav(KEY_TO_PATH['points.policies']) }] : []),
                  ...(atLeast(role, 'SUPER_ADMIN')
                    ? [
                        { key: 'points.manualEarn', label: '포인트 수기 등록', onClick: () => nav(KEY_TO_PATH['points.manualEarn']) },
                        { key: 'points.manualDeduct', label: '포인트 수기 차감', onClick: () => nav(KEY_TO_PATH['points.manualDeduct']) },
                      ]
                    : []),
                  { key: 'points.history', label: '포인트 이력조회', onClick: () => nav(KEY_TO_PATH['points.history']) },
                ],
              },
              ...(atLeast(role, 'ADMIN')
                ? [
                    {
                      key: 'stamps',
                      icon: <StarOutlined />,
                      label: topMenuLabel('스탬프관리'),
                      children: [
                        { key: 'stamps.policies', label: '스탬프 정책', onClick: () => nav(KEY_TO_PATH['stamps.policies']) },
                        { key: 'stamps.manual', label: '스탬프 수기 지급', onClick: () => nav(KEY_TO_PATH['stamps.manual']) },
                        { key: 'stamps.history', label: '스탬프 이력', onClick: () => nav(KEY_TO_PATH['stamps.history']) },
                      ],
                    },
                  ]
                : []),
              {
                key: 'coupons',
                icon: <GiftOutlined />,
                label: topMenuLabel('쿠폰관리'),
                children: [
                  ...(atLeast(role, 'SUPER_ADMIN')
                    ? [{ key: 'coupons.issue', label: '쿠폰 발행', onClick: () => nav(KEY_TO_PATH['coupons.issue']) }]
                    : []),
                  { key: 'coupons.history', label: '쿠폰 이력', onClick: () => nav(KEY_TO_PATH['coupons.history']) },
                ],
              },
              ...(atLeast(role, 'ADMIN')
                ? [
                    {
                      key: 'employees',
                      icon: <UserOutlined />,
                      label: topMenuLabel('직원관리'),
                      onClick: () => nav(KEY_TO_PATH['employees']),
                    },
                  ]
                : []),
              {
                key: 'reports',
                icon: <BarChartOutlined />,
                label: topMenuLabel('리포트'),
                children: [
                  { key: 'reports.members', label: '회원 리포트', onClick: () => nav(KEY_TO_PATH['reports.members']) },
                  { key: 'reports.points', label: '포인트 리포트', onClick: () => nav(KEY_TO_PATH['reports.points']) },
                ],
              },
              ...(atLeast(role, 'ADMIN')
                ? [
                    {
                      key: 'system',
                      icon: <SettingOutlined />,
                      label: topMenuLabel('시스템'),
                      children: [
                        { key: 'system.users', label: '사용자 관리', onClick: () => nav(KEY_TO_PATH['system.users']) },
                        { key: 'system.commonCodes', label: '공통코드 관리', onClick: () => nav(KEY_TO_PATH['system.commonCodes']) },
                        { key: 'system.permissions', label: '권한관리', onClick: () => nav(KEY_TO_PATH['system.permissions']) },
                        { key: 'system.logs', label: '로그조회', onClick: () => nav(KEY_TO_PATH['system.logs']) },
                      ],
                    },
                  ]
                : []),
              ...(atLeast(role, 'SUPER_ADMIN')
                ? [
                    {
                      key: 'tenants',
                      icon: <ShopOutlined />,
                      label: topMenuLabel('테넌트관리'),
                      children: [
                        { key: 'tenants.list', label: '테넌트 목록', onClick: () => nav(KEY_TO_PATH['tenants.list']) },
                        { key: 'tenants.admins', label: '테넌트 관리자', onClick: () => nav(KEY_TO_PATH['tenants.admins']) },
                      ],
                    },
                  ]
                : []),
            ]}
          />
        </div>
      </Layout.Sider>

      <Layout style={{ height: '100vh', overflow: 'hidden' }}>
        <Layout.Header style={{ background: '#fff', borderBottom: '1px solid #f0f0f0', paddingInline: 16, flex: '0 0 auto' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Space size={12} wrap>
              {atLeast(role, 'SUPER_ADMIN') ? (
                <Select
                  value={session?.tenantId}
                  onChange={onChangeTenant}
                  loading={tenantsQuery.isLoading}
                  options={tenantSelectOptions}
                  placeholder="테넌트 선택"
                  style={{ width: 220 }}
                  size="small"
                  showSearch
                  optionFilterProp="label"
                />
              ) : (
                <Typography.Text style={{ fontSize: 12, fontWeight: 700 }}>{tenantName}</Typography.Text>
              )}
              <Typography.Text type="secondary" style={{ fontSize: 12, whiteSpace: 'nowrap' }}>
                {session?.name ?? '-'} / {session?.role ?? 'OPERATOR'}
              </Typography.Text>
            </Space>

            <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap', justifyContent: 'flex-end' }}>
              {sessionRemaining != null && (
                <Space size={8}>
                  <Tooltip title="활동 시 자동 연장됩니다">
                    <Typography.Text type="secondary" style={{ fontSize: 12, whiteSpace: 'nowrap' }}>
                      세션 {sessionRemaining}
                    </Typography.Text>
                  </Tooltip>
                  <Button
                    size="small"
                    type="link"
                    style={{ padding: 0, height: 'auto', fontSize: 12 }}
                    onClick={() => {
                      if (session) {
                        const next = extendSession(session)
                        setSessionState(next)
                        message.success('세션이 30분 연장되었습니다.')
                      }
                    }}
                  >
                    세션 연장하기
                  </Button>
                </Space>
              )}
              <Button icon={<LogoutOutlined />} onClick={onLogout}>
                로그아웃
              </Button>
            </div>
          </div>
        </Layout.Header>
        <Layout.Content style={{ padding: 16, overflow: 'auto', flex: '1 1 auto' }}>
          {showBreadcrumbs && <AdminBreadcrumbs />}
          <Outlet />
        </Layout.Content>
      </Layout>
    </Layout>
  )
}

