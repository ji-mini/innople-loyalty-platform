import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import { RouterProvider } from 'react-router-dom'
import { ConfigProvider } from 'antd'
import { QueryProvider } from './app/QueryProvider'
import { router } from './app/routes'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ConfigProvider>
      <QueryProvider>
        <RouterProvider router={router} />
      </QueryProvider>
    </ConfigProvider>
  </StrictMode>,
)
