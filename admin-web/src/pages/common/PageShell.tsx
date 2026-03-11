import { Space, Typography } from 'antd'
import type { ReactNode } from 'react'

export function PageShell(props: { title: string; extra?: ReactNode; children: ReactNode }) {
  return (
    <Space direction="vertical" style={{ width: '100%' }} size={16}>
      <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', gap: 12, flexWrap: 'wrap' }}>
        <Typography.Title level={4} style={{ margin: 0 }}>
          {props.title}
        </Typography.Title>
        {props.extra}
      </div>
      {props.children}
    </Space>
  )
}

