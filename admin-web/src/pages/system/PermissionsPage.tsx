import { Button, Card, Divider, Modal, Space, Table, Tag, Typography } from 'antd'
import React from 'react'
import { PageShell } from '../common/PageShell'
import type { AdminRole } from '../../shared/types'
import { atLeast } from '../../shared/roles'

type Row = {
  id: string
  role: string
  description: string
}

type MenuEntry = {
  section: string
  label: string
  path: string
  minRole: AdminRole
}

const MENU_ENTRIES: MenuEntry[] = [
  { section: '공통', label: '대시보드', path: '/dashboard', minRole: 'OPERATOR' },

  { section: '회원관리', label: '회원조회', path: '/members', minRole: 'OPERATOR' },
  { section: '회원관리', label: '회원 상세', path: '/members/:memberNo', minRole: 'OPERATOR' },
  { section: '회원관리', label: '회원등록', path: '/members/register', minRole: 'SUPER_ADMIN' },
  { section: '회원관리', label: '회원등급관리', path: '/member-grades', minRole: 'ADMIN' },

  { section: '포인트관리', label: '포인트 이력조회', path: '/points/history', minRole: 'OPERATOR' },
  { section: '포인트관리', label: '정책관리', path: '/points/policies', minRole: 'ADMIN' },
  { section: '포인트관리', label: '포인트 수기 등록', path: '/points/manual/earn', minRole: 'SUPER_ADMIN' },
  { section: '포인트관리', label: '포인트 수기 차감', path: '/points/manual/deduct', minRole: 'SUPER_ADMIN' },

  { section: '쿠폰관리', label: '쿠폰 이력', path: '/coupons/history', minRole: 'OPERATOR' },
  { section: '쿠폰관리', label: '쿠폰 발행', path: '/coupons/issue', minRole: 'SUPER_ADMIN' },

  { section: '리포트', label: '포인트 리포트', path: '/reports/points', minRole: 'OPERATOR' },
  { section: '리포트', label: '회원 리포트', path: '/reports/members', minRole: 'OPERATOR' },

  { section: '시스템', label: '사용자 관리', path: '/system/users', minRole: 'ADMIN' },
  { section: '시스템', label: '공통코드 관리', path: '/system/common-codes', minRole: 'ADMIN' },
  { section: '시스템', label: '권한관리', path: '/system/permissions', minRole: 'ADMIN' },
  { section: '시스템', label: '로그조회', path: '/system/logs', minRole: 'ADMIN' },

  { section: '테넌트관리', label: '테넌트 목록', path: '/tenants', minRole: 'SUPER_ADMIN' },
  { section: '테넌트관리', label: '테넌트 상세', path: '/tenants/:tenantId', minRole: 'SUPER_ADMIN' },
  { section: '테넌트관리', label: '테넌트 관리자', path: '/tenants/admins', minRole: 'SUPER_ADMIN' },
]

function getAccessibleMenus(role: AdminRole): MenuEntry[] {
  return MENU_ENTRIES.filter((m) => atLeast(role, m.minRole))
}

export function PermissionsPage() {
  const rows = React.useMemo<Row[]>(
    () => [
      { id: 'SUPER_ADMIN', role: 'SUPER ADMIN', description: '전체 테넌트/시스템 관리' },
      { id: 'ADMIN', role: 'ADMIN', description: '테넌트 운영 관리' },
      { id: 'OPERATOR', role: 'OPERATOR', description: '조회/일부 운영 처리' },
    ],
    [],
  )

  const [open, setOpen] = React.useState(false)
  const [selectedRole, setSelectedRole] = React.useState<AdminRole>('OPERATOR')

  const accessible = React.useMemo(() => getAccessibleMenus(selectedRole), [selectedRole])
  const grouped = React.useMemo(() => {
    const map = new Map<string, MenuEntry[]>()
    for (const m of accessible) {
      const list = map.get(m.section) ?? []
      list.push(m)
      map.set(m.section, list)
    }
    return [...map.entries()]
  }, [accessible])

  return (
    <PageShell
      title="권한관리"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          권한별 접근 가능 메뉴를 확인할 수 있습니다.
        </Typography.Text>
      }
    >
      <Card>
        <Table<Row>
          rowKey={(r) => r.id}
          dataSource={rows}
          pagination={false}
          columns={[
            { title: '권한', dataIndex: 'role', width: 220 },
            { title: '설명', dataIndex: 'description' },
            {
              title: '접근 가능 메뉴',
              key: 'menus',
              width: 180,
              render: (_: any, r: Row) => {
                const role = r.id as AdminRole
                const count = getAccessibleMenus(role).length
                return (
                  <Button
                    size="small"
                    onClick={() => {
                      setSelectedRole(role)
                      setOpen(true)
                    }}
                  >
                    보기 ({count})
                  </Button>
                )
              },
            },
          ]}
        />
      </Card>

      <Modal
        open={open}
        title={
          <Space>
            <Typography.Text strong>접근 가능 메뉴</Typography.Text>
            <Tag>{selectedRole}</Tag>
          </Space>
        }
        onCancel={() => setOpen(false)}
        footer={null}
        width={720}
        destroyOnClose
      >
        {grouped.map(([section, items], idx) => (
          <div key={section}>
            {idx > 0 ? <Divider style={{ marginBlock: 12 }} /> : null}
            <Typography.Text strong>{section}</Typography.Text>
            <div style={{ marginTop: 8, display: 'flex', flexDirection: 'column', gap: 6 }}>
              {items.map((m) => (
                <div key={`${m.section}:${m.path}`} style={{ display: 'flex', gap: 10, alignItems: 'baseline' }}>
                  <Typography.Text style={{ minWidth: 140 }}>{m.label}</Typography.Text>
                  <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                    {m.path}
                  </Typography.Text>
                </div>
              ))}
            </div>
          </div>
        ))}
      </Modal>
    </PageShell>
  )
}

