import { useEffect, useState } from 'react'
import { clearSession, formatRemainingSession, getRemainingSessionMs } from './storage'

/**
 * 남은 세션 시간을 1초마다 갱신합니다.
 * 만료 시 clearSession 후 null 반환.
 */
export function useSessionRemaining(enabled: boolean): string | null {
  const [remaining, setRemaining] = useState<string | null>(() =>
    enabled ? formatRemainingSession(getRemainingSessionMs()) : null
  )

  useEffect(() => {
    if (!enabled) {
      setRemaining(null)
      return
    }

    const tick = () => {
      const ms = getRemainingSessionMs()
      if (ms <= 0) {
        clearSession()
        setRemaining(null)
        if (location.pathname !== '/login') {
          location.replace('/login')
        }
        return
      }
      setRemaining(formatRemainingSession(ms))
    }

    tick()
    const id = setInterval(tick, 1000)
    return () => clearInterval(id)
  }, [enabled])

  return remaining
}
