import React from 'react'
import { Navigate, Outlet, createBrowserRouter } from 'react-router-dom'
import { getSession } from '../shared/storage'
import { AdminLayout } from '../pages/layout/AdminLayout'
import { LoginPage } from '../pages/login/LoginPage'
import { MembersPage } from '../pages/members/MembersPage'
import { MemberDetailPage } from '../pages/members/MemberDetailPage'

function RequireAuth() {
  const session = getSession()
  if (!session) return <Navigate to="/login" replace />
  return <Outlet />
}

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
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
])

