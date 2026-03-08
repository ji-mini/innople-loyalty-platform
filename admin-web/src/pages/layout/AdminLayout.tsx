import { LogoutOutlined, TeamOutlined } from '@ant-design/icons'
import { Button, Layout, Menu, Typography } from 'antd'
import React from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { getSession } from '../../shared/storage'
import { logout } from '../../shared/auth'

export function AdminLayout() {
  const nav = useNavigate()
  const loc = useLocation()
  const session = getSession()

  const selectedKey = React.useMemo(() => {
    if (loc.pathname.startsWith('/members')) return 'members'
    return 'members'
  }, [loc.pathname])

  const onLogout = () => {
    logout()
    nav('/login', { replace: true })
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Layout.Sider width={240} theme="light">
        <div style={{ padding: 16 }}>
          <Typography.Title level={5} style={{ margin: 0 }}>
            INNOPLE Admin
          </Typography.Title>
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            {session?.name ?? '-'} / {session?.tenantId ?? '-'}
          </Typography.Text>
        </div>
        <Menu
          mode="inline"
          selectedKeys={[selectedKey]}
          items={[
            {
              key: 'members',
              icon: <TeamOutlined />,
              label: '회원',
              onClick: () => nav('/members'),
            },
          ]}
        />
      </Layout.Sider>

      <Layout>
        <Layout.Header style={{ background: '#fff', borderBottom: '1px solid #f0f0f0', paddingInline: 16 }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Typography.Text strong>관리자</Typography.Text>
            <Button icon={<LogoutOutlined />} onClick={onLogout}>
              로그아웃
            </Button>
          </div>
        </Layout.Header>
        <Layout.Content style={{ padding: 16 }}>
          <Outlet />
        </Layout.Content>
      </Layout>
    </Layout>
  )
}

