import React from 'react'
import { Navigate, Outlet, createBrowserRouter, useLocation } from 'react-router-dom'
import { getSession } from '../shared/storage'
import { canAccessPath } from '../shared/roles'
import { AdminLayout } from '../pages/layout/AdminLayout'
import { BrandHeader } from './BrandHeader'

const LoginPage = React.lazy(async () => ({ default: (await import('../pages/login/LoginPage')).LoginPage }))
const AdminSignUpPage = React.lazy(async () => ({ default: (await import('../pages/signup/AdminSignUpPage')).AdminSignUpPage }))
const MembersPage = React.lazy(async () => ({ default: (await import('../pages/members/MembersPage')).MembersPage }))
const MemberDetailPage = React.lazy(async () => ({ default: (await import('../pages/members/MemberDetailPage')).MemberDetailPage }))
const MemberCreatePage = React.lazy(async () => ({ default: (await import('../pages/members/MemberCreatePage')).MemberCreatePage }))
const DashboardPage = React.lazy(async () => ({ default: (await import('../pages/dashboard/DashboardPage')).DashboardPage }))
const MemberGradesPage = React.lazy(async () => ({ default: (await import('../pages/members/MemberGradesPage')).MemberGradesPage }))
const PointPoliciesPage = React.lazy(async () => ({ default: (await import('../pages/points/PointPoliciesPage')).PointPoliciesPage }))
const PointManualEarnPage = React.lazy(async () => ({ default: (await import('../pages/points/PointManualEarnPage')).PointManualEarnPage }))
const PointManualDeductPage = React.lazy(async () => ({ default: (await import('../pages/points/PointManualDeductPage')).PointManualDeductPage }))
const PointHistoryPage = React.lazy(async () => ({ default: (await import('../pages/points/PointHistoryPage')).PointHistoryPage }))
const PointExpiryPage = React.lazy(async () => ({ default: (await import('../pages/points/PointExpiryPage')).PointExpiryPage }))
const CouponIssuePage = React.lazy(async () => ({ default: (await import('../pages/coupons/CouponIssuePage')).CouponIssuePage }))
const CouponHistoryPage = React.lazy(async () => ({ default: (await import('../pages/coupons/CouponHistoryPage')).CouponHistoryPage }))
const PointReportPage = React.lazy(async () => ({ default: (await import('../pages/reports/PointReportPage')).PointReportPage }))
const MemberReportPage = React.lazy(async () => ({ default: (await import('../pages/reports/MemberReportPage')).MemberReportPage }))
const TenantsPage = React.lazy(async () => ({ default: (await import('../pages/tenants/TenantsPage')).TenantsPage }))
const TenantAdminsPage = React.lazy(async () => ({ default: (await import('../pages/tenants/TenantAdminsPage')).TenantAdminsPage }))
const TenantDetailPage = React.lazy(async () => ({ default: (await import('../pages/tenants/TenantDetailPage')).TenantDetailPage }))
const AdminAccountsPage = React.lazy(async () => ({ default: (await import('../pages/system/AdminAccountsPage')).AdminAccountsPage }))
const CommonCodesPage = React.lazy(async () => ({ default: (await import('../pages/system/CommonCodesPage')).CommonCodesPage }))
const PermissionsPage = React.lazy(async () => ({ default: (await import('../pages/system/PermissionsPage')).PermissionsPage }))
const LogsPage = React.lazy(async () => ({ default: (await import('../pages/system/LogsPage')).LogsPage }))

function RequireAuth() {
  const loc = useLocation()
  const session = getSession()
  if (!session) return <Navigate to="/login" replace />
  if (!canAccessPath(session.role, loc.pathname)) return <Navigate to="/dashboard" replace />
  return <Outlet />
}

function RootLayout() {
  const loc = useLocation()
  const showFixedBrand = loc.pathname === '/login' || loc.pathname === '/signup'

  // 사용자가 아무 요청 없이 오래 머물러도 30분 뒤 자동 로그아웃되도록 주기적으로 세션 만료를 확인합니다.
  React.useEffect(() => {
    const id = window.setInterval(() => {
      const s = getSession()
      if (!s && location.pathname !== '/login' && location.pathname !== '/signup') {
        location.replace('/login')
      }
    }, 5000)
    return () => window.clearInterval(id)
  }, [])

  return (
    <>
      {showFixedBrand ? <BrandHeader variant="fixed" /> : null}
      <Outlet />
    </>
  )
}

function withSuspense(element: React.ReactNode) {
  return (
    <React.Suspense fallback={<div style={{ padding: 24 }}>불러오는 중...</div>}>
      {element}
    </React.Suspense>
  )
}

export const router = createBrowserRouter([
  {
    element: <RootLayout />,
    children: [
      {
        path: '/',
        element: <Navigate to="/dashboard" replace />,
      },
      {
        path: '/login',
        element: withSuspense(<LoginPage />),
      },
      {
        path: '/signup',
        element: withSuspense(<AdminSignUpPage />),
      },
      {
        element: <RequireAuth />,
        children: [
          {
            element: <AdminLayout />,
            children: [
              { index: true, element: <Navigate to="/dashboard" replace /> },
              { path: '/dashboard', element: withSuspense(<DashboardPage />), handle: { crumbs: ['대시보드'] } },

              // Member management
              { path: '/members', element: withSuspense(<MembersPage />), handle: { crumbs: ['회원관리', '회원조회'] } },
              { path: '/members/register', element: withSuspense(<MemberCreatePage />), handle: { crumbs: ['회원관리', '회원등록'] } },
              {
                path: '/members/:memberNo',
                element: withSuspense(<MemberDetailPage />),
                handle: { crumbs: (p: any) => ['회원관리', '회원상세', p?.memberNo ?? '-'] },
              },
              { path: '/member-grades', element: withSuspense(<MemberGradesPage />), handle: { crumbs: ['회원관리', '회원등급관리'] } },

              // Point management
              { path: '/points/policies', element: withSuspense(<PointPoliciesPage />), handle: { crumbs: ['포인트관리', '정책관리'] } },
              { path: '/points/manual', element: <Navigate to="/points/manual/earn" replace /> },
              { path: '/points/manual/earn', element: withSuspense(<PointManualEarnPage />), handle: { crumbs: ['포인트관리', '포인트 수기 등록'] } },
              { path: '/points/manual/deduct', element: withSuspense(<PointManualDeductPage />), handle: { crumbs: ['포인트관리', '포인트 수기 차감'] } },
              { path: '/points/history', element: withSuspense(<PointHistoryPage />), handle: { crumbs: ['포인트관리', '포인트 이력조회'] } },
              { path: '/points/expiry', element: withSuspense(<PointExpiryPage />), handle: { crumbs: ['포인트관리', '소멸관리'] } },

              // Coupon management
              { path: '/coupons/issue', element: withSuspense(<CouponIssuePage />), handle: { crumbs: ['쿠폰관리', '쿠폰 발행'] } },
              { path: '/coupons/history', element: withSuspense(<CouponHistoryPage />), handle: { crumbs: ['쿠폰관리', '쿠폰 이력'] } },

              // Reports
              { path: '/reports/points', element: withSuspense(<PointReportPage />), handle: { crumbs: ['리포트', '포인트 리포트'] } },
              { path: '/reports/members', element: withSuspense(<MemberReportPage />), handle: { crumbs: ['리포트', '회원 리포트'] } },

              // Tenant management
              { path: '/tenants', element: withSuspense(<TenantsPage />), handle: { crumbs: ['테넌트관리', '테넌트 목록'] } },
              { path: '/tenants/admins', element: withSuspense(<TenantAdminsPage />), handle: { crumbs: ['테넌트관리', '테넌트 관리자'] } },
              {
                path: '/tenants/:tenantId',
                element: withSuspense(<TenantDetailPage />),
                handle: { crumbs: (p: any) => ['테넌트관리', '테넌트 상세', p?.tenantId ?? '-'] },
              },

              // System management
              { path: '/system/users', element: withSuspense(<AdminAccountsPage />), handle: { crumbs: ['시스템', '사용자 관리'] } },
              { path: '/system/admins', element: <Navigate to="/system/users" replace /> },
              { path: '/system/common-codes', element: withSuspense(<CommonCodesPage />), handle: { crumbs: ['시스템', '공통코드 관리'] } },
              { path: '/system/permissions', element: withSuspense(<PermissionsPage />), handle: { crumbs: ['시스템', '권한관리'] } },
              { path: '/system/logs', element: withSuspense(<LogsPage />), handle: { crumbs: ['시스템', '로그조회'] } },
            ],
          },
        ],
      },
      { path: '*', element: <Navigate to="/dashboard" replace /> },
    ],
  },
])

