import { Breadcrumb, Typography } from 'antd'
import React from 'react'
import { useMatches } from 'react-router-dom'

type Crumbs = string[] | ((params: Record<string, string | undefined>) => string[])

type MatchHandle = {
  crumbs?: Crumbs
}

export function AdminBreadcrumbs() {
  const matches = useMatches()

  const crumbs = React.useMemo(() => {
    for (let i = matches.length - 1; i >= 0; i -= 1) {
      const m: any = matches[i]
      const h: MatchHandle | undefined = m?.handle
      if (!h?.crumbs) continue
      if (typeof h.crumbs === 'function') return h.crumbs(m.params ?? {})
      return h.crumbs
    }
    return []
  }, [matches])

  if (!crumbs.length) return null

  return (
    <div style={{ marginBottom: 12 }}>
      <Breadcrumb
        separator=">"
        items={crumbs.map((c, idx) => ({
          key: `${idx}-${c}`,
          title: (
            <Typography.Text style={{ fontSize: 12 }} type={idx === crumbs.length - 1 ? undefined : 'secondary'}>
              {c}
            </Typography.Text>
          ),
        }))}
      />
    </div>
  )
}

