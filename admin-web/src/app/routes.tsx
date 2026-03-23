import React from 'react'
import { Navigate, Outlet, createBrowserRouter, useLocation } from 'react-router-dom'
import { getSession } from '../shared/storage'
import { canAccessPath } from '../shared/roles'
import { AdminLayout } from '../pages/layout/AdminLayout'
import { LoginPage } from '../pages/login/LoginPage'
import { AdminSignUpPage } from '../pages/signup/AdminSignUpPage'
import { MembersPage } from '../pages/members/MembersPage'
import { MemberDetailPage } from '../pages/members/MemberDetailPage'
import { MemberCreatePage } from '../pages/members/MemberCreatePage'
import { DashboardPage } from '../pages/dashboard/DashboardPage'
import { MemberGradesPage } from '../pages/members/MemberGradesPage'
import { PointPoliciesPage } from '../pages/points/PointPoliciesPage'
import { PointManualEarnPage } from '../pages/points/PointManualEarnPage'
import { PointManualDeductPage } from '../pages/points/PointManualDeductPage'
import { PointHistoryPage } from '../pages/points/PointHistoryPage'
import { PointExpiryPage } from '../pages/points/PointExpiryPage'
import { CouponIssuePage } from '../pages/coupons/CouponIssuePage'
import { CouponHistoryPage } from '../pages/coupons/CouponHistoryPage'
import { PointReportPage } from '../pages/reports/PointReportPage'
import { MemberReportPage } from '../pages/reports/MemberReportPage'
import { TenantsPage } from '../pages/tenants/TenantsPage'
import { TenantAdminsPage } from '../pages/tenants/TenantAdminsPage'
import { TenantDetailPage } from '../pages/tenants/TenantDetailPage'
import { AdminAccountsPage } from '../pages/system/AdminAccountsPage'
import { CommonCodesPage } from '../pages/system/CommonCodesPage'
import { PermissionsPage } from '../pages/system/PermissionsPage'
import { LogsPage } from '../pages/system/LogsPage'
import { BrandHeader } from './BrandHeader'

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
        element: <LoginPage />,
      },
      {
        path: '/signup',
        element: <AdminSignUpPage />,
      },
      {
        element: <RequireAuth />,
        children: [
          {
            element: <AdminLayout />,
            children: [
              { index: true, element: <Navigate to="/dashboard" replace /> },
              { path: '/dashboard', element: <DashboardPage />, handle: { crumbs: ['대시보드'] } },

              // Member management
              { path: '/members', element: <MembersPage />, handle: { crumbs: ['회원관리', '회원조회'] } },
              { path: '/members/register', element: <MemberCreatePage />, handle: { crumbs: ['회원관리', '회원등록'] } },
              {
                path: '/members/:memberNo',
                element: <MemberDetailPage />,
                handle: { crumbs: (p: any) => ['회원관리', '회원상세', p?.memberNo ?? '-'] },
              },
              { path: '/member-grades', element: <MemberGradesPage />, handle: { crumbs: ['회원관리', '회원등급관리'] } },

              // Point management
              { path: '/points/policies', element: <PointPoliciesPage />, handle: { crumbs: ['포인트관리', '정책관리'] } },
              { path: '/points/manual', element: <Navigate to="/points/manual/earn" replace /> },
              { path: '/points/manual/earn', element: <PointManualEarnPage />, handle: { crumbs: ['포인트관리', '포인트 수기 등록'] } },
              { path: '/points/manual/deduct', element: <PointManualDeductPage />, handle: { crumbs: ['포인트관리', '포인트 수기 차감'] } },
              { path: '/points/history', element: <PointHistoryPage />, handle: { crumbs: ['포인트관리', '포인트 이력조회'] } },
              { path: '/points/expiry', element: <PointExpiryPage />, handle: { crumbs: ['포인트관리', '소멸관리'] } },

              // Coupon management
              { path: '/coupons/issue', element: <CouponIssuePage />, handle: { crumbs: ['쿠폰관리', '쿠폰 발행'] } },
              { path: '/coupons/history', element: <CouponHistoryPage />, handle: { crumbs: ['쿠폰관리', '쿠폰 이력'] } },

              // Reports
              { path: '/reports/points', element: <PointReportPage />, handle: { crumbs: ['리포트', '포인트 리포트'] } },
              { path: '/reports/members', element: <MemberReportPage />, handle: { crumbs: ['리포트', '회원 리포트'] } },

              // Tenant management
              { path: '/tenants', element: <TenantsPage />, handle: { crumbs: ['테넌트관리', '테넌트 목록'] } },
              { path: '/tenants/admins', element: <TenantAdminsPage />, handle: { crumbs: ['테넌트관리', '테넌트 관리자'] } },
              {
                path: '/tenants/:tenantId',
                element: <TenantDetailPage />,
                handle: { crumbs: (p: any) => ['테넌트관리', '테넌트 상세', p?.tenantId ?? '-'] },
              },

              // System management
              { path: '/system/users', element: <AdminAccountsPage />, handle: { crumbs: ['시스템', '사용자 관리'] } },
              { path: '/system/admins', element: <Navigate to="/system/users" replace /> },
              { path: '/system/common-codes', element: <CommonCodesPage />, handle: { crumbs: ['시스템', '공통코드 관리'] } },
              { path: '/system/permissions', element: <PermissionsPage />, handle: { crumbs: ['시스템', '권한관리'] } },
              { path: '/system/logs', element: <LogsPage />, handle: { crumbs: ['시스템', '로그조회'] } },
            ],
          },
        ],
      },
      { path: '*', element: <Navigate to="/dashboard" replace /> },
    ],
  },
])

