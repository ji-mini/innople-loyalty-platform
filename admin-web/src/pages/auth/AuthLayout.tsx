import { CheckCircleFilled } from '@ant-design/icons'
import { Card } from 'antd'
import React from 'react'
import styles from './AuthLayout.module.css'

type AuthLayoutProps = {
  /** 카드 상단 제목 (예: "Admin Login") */
  cardTitle: string
  /** 카드 상단 부제목 */
  cardSubtitle?: string
  /** 카드 본문 (알림, 폼, 버튼 등) */
  children: React.ReactNode
}

/**
 * 인증 화면(로그인/회원가입/비밀번호 재설정 등) 공통 레이아웃.
 * - 연한 그라데이션 배경 + 떠다니는 구름 + 장식 일러스트
 * - 좌측 히어로 영역(2줄 브랜드 타이틀 + 코럴 강조 라인, 미니멀)
 * - 우측 흰색 카드(폼 슬롯)
 * - 좁은 화면에서는 히어로가 위, 카드가 아래로 스택
 */
export function AuthLayout({ cardTitle, cardSubtitle, children }: AuthLayoutProps) {
  return (
    <div className={`brand-gradient-bg ${styles.page}`}>
      <div className={styles.clouds} />

      <svg className={styles.decorPlane} viewBox="0 0 120 120" aria-hidden="true">
        <path
          d="M14 55.5c-.8-.3-1.2-1.1-.9-1.9.2-.5.6-.8 1.1-.9l88-22.5c1.2-.3 2.2.8 1.8 1.9L77 106.5c-.2.6-.8 1.1-1.5 1.1-.7 0-1.3-.4-1.5-1.1L60 72 14 55.5Z"
          fill="rgba(255,139,122,0.55)"
        />
        <path d="M103.2 31.1 60.2 72l13.6 33.4 29.4-74.3Z" fill="rgba(139,216,194,0.55)" />
      </svg>

      <svg className={styles.decorGift} viewBox="0 0 160 160" aria-hidden="true">
        <rect x="26" y="60" width="108" height="80" rx="18" fill="rgba(155,225,255,0.55)" />
        <rect x="22" y="50" width="116" height="22" rx="11" fill="rgba(255,180,210,0.55)" />
        <rect x="74" y="50" width="12" height="90" rx="6" fill="rgba(255,255,255,0.55)" />
        <path
          d="M80 50c-10-2-18-8-20-16-2-8 3-14 11-14 10 0 17 10 19 20 2-10 9-20 19-20 8 0 13 6 11 14-2 8-10 14-20 16h-20Z"
          fill="rgba(255,139,122,0.45)"
        />
      </svg>

      <div className={styles.wrap}>
        <section className={styles.hero}>
          <h1 className={styles.brand}>
            <span className={styles.brandPrimary}>Innople</span>
            <span className={styles.brandSecondary}>Loyalty Platform</span>
          </h1>
          <span className={styles.accent} aria-hidden="true" />
          <ul className={styles.features}>
            {['테넌트 기반 멀티테넌시', '회원 · 포인트 통합 관리', '안전하고 유연한 운영'].map((label) => (
              <li key={label} className={styles.featureItem}>
                <CheckCircleFilled className={styles.featureIcon} />
                <span>{label}</span>
              </li>
            ))}
          </ul>
        </section>

        <Card className={styles.card} bordered={false} bodyStyle={{ padding: 24 }}>
          <div className={styles.cardHeader}>
            <p className={styles.cardTitle}>{cardTitle}</p>
            {cardSubtitle && <p className={styles.cardSub}>{cardSubtitle}</p>}
          </div>

          {children}

          <div className={styles.footer}>© {new Date().getFullYear()} INNOPLE</div>
        </Card>
      </div>
    </div>
  )
}
