import { useEffect, useRef } from 'react'
import { getRemainingSessionMs, getSession } from './storage'
import { refreshSession } from './auth'

/** 활동 감지 후 토큰 갱신 요청 최소 간격 (서버 부하 방지) */
const ACTIVITY_THROTTLE_MS = 60 * 1000
/** 남은 시간이 이 값 이하일 때만 활동 시 토큰을 갱신합니다 (10분) */
const REFRESH_WHEN_REMAINING_MS = 10 * 60 * 1000

/**
 * 사용자 활동(마우스, 키보드, 스크롤 등) 시 서버에 토큰 갱신을 요청해 세션을 슬라이딩 연장합니다.
 * AdminLayout에서 로그인된 상태일 때만 사용합니다.
 */
export function useSessionActivity(enabled: boolean) {
  const lastRefreshRef = useRef(0)

  useEffect(() => {
    if (!enabled) return

    const handleActivity = () => {
      const now = Date.now()
      if (now - lastRefreshRef.current < ACTIVITY_THROTTLE_MS) return

      const session = getSession()
      if (!session) return
      // 만료가 임박했을 때만 서버 토큰을 갱신합니다.
      if (getRemainingSessionMs() > REFRESH_WHEN_REMAINING_MS) return

      lastRefreshRef.current = now
      void refreshSession()
    }

    const events = ['mousedown', 'mousemove', 'keydown', 'scroll', 'touchstart', 'click']
    events.forEach((ev) => window.addEventListener(ev, handleActivity))
    return () => events.forEach((ev) => window.removeEventListener(ev, handleActivity))
  }, [enabled])
}
