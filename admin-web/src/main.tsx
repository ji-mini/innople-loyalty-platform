import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import { RouterProvider } from 'react-router-dom'
import { ConfigProvider } from 'antd'
import { QueryProvider } from './app/QueryProvider'
import { router } from './app/routes'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: '#ff8b7a',
          colorInfo: '#ff8b7a',
          borderRadius: 14,
          fontFamily: "'Nunito Sans', system-ui, -apple-system, Segoe UI, Roboto, sans-serif",
        },
        components: {
          Button: {
            controlHeight: 44,
            borderRadius: 999,
            fontWeight: 700,
          },
          Input: {
            controlHeight: 44,
            borderRadius: 12,
          },
          Card: {
            borderRadiusLG: 24,
          },
        },
      }}
    >
      <QueryProvider>
        <RouterProvider router={router} />
      </QueryProvider>
    </ConfigProvider>
  </StrictMode>,
)
