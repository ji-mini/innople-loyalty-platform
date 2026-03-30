import { useEffect, useRef } from 'react'
import { getSession, touchSession } from './storage'

const ACTIVITY_THROTTLE_MS = 30 * 1000

/**
 * 사용자 활동(마우스, 키보드, 스크롤 등) 시 세션을 연장합니다.
 * AdminLayout에서 로그인된 상태일 때만 사용합니다.
 */
export function useSessionActivity(enabled: boolean) {
  const lastTouchRef = useRef(0)

  useEffect(() => {
    if (!enabled) return

    const handleActivity = () => {
      const now = Date.now()
      if (now - lastTouchRef.current < ACTIVITY_THROTTLE_MS) return

      const session = getSession()
      if (!session) return

      const touched = touchSession(session)
      if (touched.expiresAt !== session.expiresAt) {
        lastTouchRef.current = now
      }
    }

    const events = ['mousedown', 'mousemove', 'keydown', 'scroll', 'touchstart', 'click']
    events.forEach((ev) => window.addEventListener(ev, handleActivity))
    return () => events.forEach((ev) => window.removeEventListener(ev, handleActivity))
  }, [enabled])
}
