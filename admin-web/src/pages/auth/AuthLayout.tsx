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
 * - 좌측 브랜드 영역(브랜드명/태그라인/기능 소개 카드)
 * - 우측 흰색 카드(폼 슬롯)
 * - 좁은 화면에서는 브랜드 영역을 숨기고 카드만 중앙 정렬
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
          <h1 className={styles.brand}>INNOPLE LOYALTY PLATFORM</h1>
          <p className={styles.tagline}>테넌트 기반 멀티테넌시로, 더 안전하고 유연한 운영을 지원합니다.</p>
          <div className={styles.featureCards}>
            <div className={styles.featureCard}>
              <div className={styles.featureCardImg}>
                <svg viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="8" y="12" width="20" height="20" rx="4" fill="rgba(255,139,122,0.9)" />
                  <rect x="36" y="12" width="20" height="20" rx="4" fill="rgba(139,216,194,0.9)" />
                  <rect x="8" y="36" width="20" height="20" rx="4" fill="rgba(155,225,255,0.9)" />
                  <rect x="36" y="36" width="20" height="20" rx="4" fill="rgba(255,180,210,0.9)" />
                </svg>
              </div>
              <span className={styles.featureCardLabel}>테넌트 기반 멀티테넌시</span>
            </div>
            <div className={styles.featureCard}>
              <div className={styles.featureCardImg}>
                <svg viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="32" cy="20" r="10" fill="rgba(139,216,194,0.9)" />
                  <path d="M12 52c0-11 8.95-20 20-20s20 9 20 20" stroke="rgba(139,216,194,0.9)" strokeWidth="6" fill="none" strokeLinecap="round" />
                  <circle cx="48" cy="28" r="6" fill="rgba(255,139,122,0.9)" />
                  <path d="M36 48c0-6.6 5.4-12 12-12" stroke="rgba(255,139,122,0.9)" strokeWidth="4" fill="none" strokeLinecap="round" />
                </svg>
              </div>
              <span className={styles.featureCardLabel}>회원 조회 · 관리</span>
            </div>
            <div className={styles.featureCard}>
              <div className={styles.featureCardImg}>
                <svg viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="32" cy="28" r="14" fill="rgba(155,225,255,0.9)" />
                  <path d="M32 18v20M24 28h16" stroke="white" strokeWidth="3" strokeLinecap="round" />
                  <rect x="14" y="44" width="36" height="8" rx="2" fill="rgba(255,180,210,0.9)" />
                </svg>
              </div>
              <span className={styles.featureCardLabel}>포인트/거래 이력</span>
            </div>
          </div>
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
