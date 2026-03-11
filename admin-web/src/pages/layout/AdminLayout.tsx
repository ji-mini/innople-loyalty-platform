import { BarChartOutlined, DashboardOutlined, GiftOutlined, LogoutOutlined, SettingOutlined, ShopOutlined, TeamOutlined } from '@ant-design/icons'
import { Button, Layout, Menu, Select, Space, Typography } from 'antd'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import React from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { getSession, setSession } from '../../shared/storage'
import { logout } from '../../shared/auth'
import { atLeast } from '../../shared/roles'
import { listPublicTenants } from '../../shared/tenants'

type MenuKey =
  | 'dashboard'
  | 'members.list'
  | 'members.register'
  | 'members.grades'
  | 'points.policies'
  | 'points.manualEarn'
  | 'points.manualDeduct'
  | 'points.history'
  | 'points.expiry'
  | 'coupons.issue'
  | 'coupons.history'
  | 'reports.points'
  | 'reports.members'
  | 'tenants.list'
  | 'tenants.admins'
  | 'system.users'
  | 'system.permissions'
  | 'system.logs'

const KEY_TO_PATH: Record<MenuKey, string> = {
  dashboard: '/dashboard',
  'members.list': '/members',
  'members.register': '/members/register',
  'members.grades': '/member-grades',
  'points.policies': '/points/policies',
  'points.manualEarn': '/points/manual/earn',
  'points.manualDeduct': '/points/manual/deduct',
  'points.history': '/points/history',
  'points.expiry': '/points/expiry',
  'coupons.issue': '/coupons/issue',
  'coupons.history': '/coupons/history',
  'reports.points': '/reports/points',
  'reports.members': '/reports/members',
  'tenants.list': '/tenants',
  'tenants.admins': '/tenants/admins',
  'system.users': '/system/users',
  'system.permissions': '/system/permissions',
  'system.logs': '/system/logs',
}

function pickSelectedKey(pathname: string): MenuKey {
  if (pathname === '/' || pathname.startsWith('/dashboard')) return 'dashboard'
  if (pathname.startsWith('/members/register')) return 'members.register'
  if (pathname === '/members' || pathname.startsWith('/members/')) return 'members.list'
  if (pathname.startsWith('/member-grades')) return 'members.grades'

  if (pathname.startsWith('/points/policies')) return 'points.policies'
  if (pathname.startsWith('/points/manual/earn')) return 'points.manualEarn'
  if (pathname.startsWith('/points/manual/deduct')) return 'points.manualDeduct'
  if (pathname.startsWith('/points/manual')) return 'points.manualEarn'
  if (pathname.startsWith('/points/history')) return 'points.history'
  if (pathname.startsWith('/points/expiry')) return 'points.expiry'

  if (pathname.startsWith('/coupons/issue')) return 'coupons.issue'
  if (pathname.startsWith('/coupons/history')) return 'coupons.history'

  if (pathname.startsWith('/reports/points')) return 'reports.points'
  if (pathname.startsWith('/reports/members')) return 'reports.members'

  if (pathname === '/tenants' || pathname.startsWith('/tenants/')) {
    if (pathname.startsWith('/tenants/admins')) return 'tenants.admins'
    return 'tenants.list'
  }

  if (pathname.startsWith('/system/users') || pathname.startsWith('/system/admins')) return 'system.users'
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

  const tenantsQuery = useQuery({
    queryKey: ['public', 'tenants'],
    queryFn: listPublicTenants,
  })

  const tenantName = React.useMemo(() => {
    const id = session?.tenantId
    if (!id) return '-'
    const hit = tenantsQuery.data?.items?.find((t) => t.tenantId === id)
    return hit?.name ?? id
  }, [session?.tenantId, tenantsQuery.data?.items])

  const selectedKey = React.useMemo<MenuKey>(() => pickSelectedKey(loc.pathname), [loc.pathname])
  const groupKey = selectedKey.includes('.') ? selectedKey.split('.')[0] : undefined
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

    const next = { ...session, tenantId }
    setSession(next)
    setSessionState(next)
    qc.clear()
    nav('/dashboard', { replace: true })
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Layout.Sider width={240} theme="light">
        <div style={{ padding: 16 }}>
          <Typography.Title level={5} style={{ margin: 0 }}>
            INNO MEMBERSHIP ADMIN
          </Typography.Title>
        </div>
        <Menu
          mode="inline"
          selectedKeys={[selectedKey]}
          openKeys={openKeys}
          onOpenChange={(keys) => setOpenKeys(keys as string[])}
          style={{ paddingTop: 44 }}
          items={[
            {
              key: 'dashboard',
              icon: <DashboardOutlined />,
              label: '대시보드',
              onClick: () => nav(KEY_TO_PATH.dashboard),
            },
            {
              key: 'members',
              icon: <TeamOutlined />,
              label: '회원관리',
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
                        label: '회원등급관리',
                        onClick: () => nav(KEY_TO_PATH['members.grades']),
                      },
                    ]
                  : []),
              ],
            },
            {
              key: 'points',
              icon: <ShopOutlined />,
              label: '포인트관리',
              children: [
                ...(atLeast(role, 'ADMIN') ? [{ key: 'points.policies', label: '정책관리', onClick: () => nav(KEY_TO_PATH['points.policies']) }] : []),
                ...(atLeast(role, 'SUPER_ADMIN')
                  ? [
                      { key: 'points.manualEarn', label: '포인트 수기 등록', onClick: () => nav(KEY_TO_PATH['points.manualEarn']) },
                      { key: 'points.manualDeduct', label: '수기 차감', onClick: () => nav(KEY_TO_PATH['points.manualDeduct']) },
                    ]
                  : []),
                { key: 'points.history', label: '포인트 이력조회', onClick: () => nav(KEY_TO_PATH['points.history']) },
              ],
            },
            {
              key: 'coupons',
              icon: <GiftOutlined />,
              label: '쿠폰관리',
              children: [
                ...(atLeast(role, 'SUPER_ADMIN')
                  ? [{ key: 'coupons.issue', label: '쿠폰 발행', onClick: () => nav(KEY_TO_PATH['coupons.issue']) }]
                  : []),
                { key: 'coupons.history', label: '쿠폰 이력', onClick: () => nav(KEY_TO_PATH['coupons.history']) },
              ],
            },
            {
              key: 'reports',
              icon: <BarChartOutlined />,
              label: '리포트',
              children: [
                { key: 'reports.points', label: '포인트 리포트', onClick: () => nav(KEY_TO_PATH['reports.points']) },
                { key: 'reports.members', label: '회원 리포트', onClick: () => nav(KEY_TO_PATH['reports.members']) },
              ],
            },
            ...(atLeast(role, 'ADMIN')
              ? [
                  {
                    key: 'system',
                    icon: <SettingOutlined />,
                    label: '시스템',
                    children: [
                      { key: 'system.users', label: '사용자 관리', onClick: () => nav(KEY_TO_PATH['system.users']) },
                      { key: 'system.permissions', label: '권한관리', onClick: () => nav(KEY_TO_PATH['system.permissions']) },
                      { key: 'system.logs', label: '로그조회', onClick: () => nav(KEY_TO_PATH['system.logs']) },
                    ],
                  },
                ]
              : []),
            ...(atLeast(role, 'ADMIN')
              ? [
                  {
                    key: 'tenants',
                    icon: <ShopOutlined />,
                    label: '테넌트관리',
                    children: [
                      { key: 'tenants.list', label: '테넌트 목록', onClick: () => nav(KEY_TO_PATH['tenants.list']) },
                      { key: 'tenants.admins', label: '테넌트 관리자', onClick: () => nav(KEY_TO_PATH['tenants.admins']) },
                    ],
                  },
                ]
              : []),
          ]}
        />
      </Layout.Sider>

      <Layout>
        <Layout.Header style={{ background: '#fff', borderBottom: '1px solid #f0f0f0', paddingInline: 16 }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Space size={12} wrap>
              <Typography.Text strong>관리자</Typography.Text>
              <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                Tenant:
              </Typography.Text>
              {atLeast(role, 'SUPER_ADMIN') ? (
                <Select
                  value={session?.tenantId}
                  onChange={onChangeTenant}
                  loading={tenantsQuery.isLoading}
                  options={(tenantsQuery.data?.items ?? []).map((t) => ({ value: t.tenantId, label: t.name }))}
                  placeholder="테넌트 선택"
                  style={{ width: 220 }}
                  size="small"
                  showSearch
                  optionFilterProp="label"
                />
              ) : (
                <Typography.Text style={{ fontSize: 12, fontWeight: 700 }}>{tenantName}</Typography.Text>
              )}
            </Space>

            <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap', justifyContent: 'flex-end' }}>
              <Typography.Text type="secondary" style={{ fontSize: 12, whiteSpace: 'nowrap' }}>
                {session?.name ?? '-'} / {session?.role ?? 'OPERATOR'}
              </Typography.Text>
              <Button icon={<LogoutOutlined />} onClick={onLogout}>
                로그아웃
              </Button>
            </div>
          </div>
        </Layout.Header>
        <Layout.Content style={{ padding: 16 }}>
          <Outlet />
        </Layout.Content>
      </Layout>
    </Layout>
  )
}

