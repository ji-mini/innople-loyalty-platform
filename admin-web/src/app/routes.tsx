import { Navigate, Outlet, createBrowserRouter } from 'react-router-dom'
import { getSession } from '../shared/storage'
import { AdminLayout } from '../pages/layout/AdminLayout'
import { LoginPage } from '../pages/login/LoginPage'
import { AdminSignUpPage } from '../pages/signup/AdminSignUpPage'
import { MembersPage } from '../pages/members/MembersPage'
import { MemberDetailPage } from '../pages/members/MemberDetailPage'
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
      <Outlet />
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
              { index: true, element: <Navigate to="/members" replace /> },
              { path: '/members', element: <MembersPage /> },
              { path: '/members/:memberNo', element: <MemberDetailPage /> },
            ],
          },
        ],
      },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
])

