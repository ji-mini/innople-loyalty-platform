import { Navigate, Outlet, createBrowserRouter } from 'react-router-dom'
import { getSession } from '../shared/storage'
import { AdminLayout } from '../pages/layout/AdminLayout'
import { LoginPage } from '../pages/login/LoginPage'
import { AdminSignUpPage } from '../pages/signup/AdminSignUpPage'
import { MembersPage } from '../pages/members/MembersPage'
import { MemberDetailPage } from '../pages/members/MemberDetailPage'
import { DashboardPage } from '../pages/dashboard/DashboardPage'
import { MemberGradesPage } from '../pages/members/MemberGradesPage'
import { PointPoliciesPage } from '../pages/points/PointPoliciesPage'
import { PointManualPage } from '../pages/points/PointManualPage'
import { PointHistoryPage } from '../pages/points/PointHistoryPage'
import { PointExpiryPage } from '../pages/points/PointExpiryPage'
import { CouponIssuePage } from '../pages/coupons/CouponIssuePage'
import { CouponHistoryPage } from '../pages/coupons/CouponHistoryPage'
import { PointReportPage } from '../pages/reports/PointReportPage'
import { MemberReportPage } from '../pages/reports/MemberReportPage'
import { TenantsPage } from '../pages/tenants/TenantsPage'
import { TenantAdminsPage } from '../pages/tenants/TenantAdminsPage'
import { AdminAccountsPage } from '../pages/system/AdminAccountsPage'
import { PermissionsPage } from '../pages/system/PermissionsPage'
import { LogsPage } from '../pages/system/LogsPage'
import { BrandHeader } from './BrandHeader'

function RequireAuth() {
  const session = getSession()
  if (!session) return <Navigate to="/login" replace />
  return <Outlet />
}

function RootLayout() {
  return (
    <>
      <BrandHeader />
      <div className="innople-app-shell">
        <Outlet />
      </div>
    </>
  )
}

export const router = createBrowserRouter([
  {
    element: <RootLayout />,
    children: [
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
              { path: '/dashboard', element: <DashboardPage /> },

              // Member management
              { path: '/members', element: <MembersPage /> },
              { path: '/members/:memberNo', element: <MemberDetailPage /> },
              { path: '/member-grades', element: <MemberGradesPage /> },

              // Point management
              { path: '/points/policies', element: <PointPoliciesPage /> },
              { path: '/points/manual', element: <PointManualPage /> },
              { path: '/points/history', element: <PointHistoryPage /> },
              { path: '/points/expiry', element: <PointExpiryPage /> },

              // Coupon management
              { path: '/coupons/issue', element: <CouponIssuePage /> },
              { path: '/coupons/history', element: <CouponHistoryPage /> },

              // Reports
              { path: '/reports/points', element: <PointReportPage /> },
              { path: '/reports/members', element: <MemberReportPage /> },

              // Tenant management
              { path: '/tenants', element: <TenantsPage /> },
              { path: '/tenants/admins', element: <TenantAdminsPage /> },

              // System management
              { path: '/system/admins', element: <AdminAccountsPage /> },
              { path: '/system/permissions', element: <PermissionsPage /> },
              { path: '/system/logs', element: <LogsPage /> },
            ],
          },
        ],
      },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
])

