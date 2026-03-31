import { Card, Typography } from 'antd'
import { PageShell } from '../common/PageShell'

/**
 * 클럽(조건 충족 시 가입·혜택) 운영 화면 — API 연동 전 안내용 플레이스홀더.
 */
export function ClubManagementPage() {
  return (
    <PageShell
      title="클럽 관리"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          조건을 만족한 회원이 클럽에 가입하고, 클럽별 혜택을 부여할 수 있습니다.
        </Typography.Text>
      }
    >
      <Card>
        <Typography.Paragraph style={{ marginBottom: 8 }}>
          <Typography.Text strong>운영 방향</Typography.Text>
        </Typography.Paragraph>
        <ul style={{ margin: '0 0 16px', paddingLeft: 20, color: 'rgba(0,0,0,0.65)' }}>
          <li>가입 조건(예: 등급, 누적 구매, 활동 지표 등)을 정의합니다.</li>
          <li>조건에 해당하는 회원은 클럽에 가입할 수 있게 합니다.</li>
          <li>클럽별로 포인트·쿠폰·프로모션 등 혜택을 연결해 운영합니다.</li>
        </ul>
        <Typography.Text type="secondary">기능 구현 및 API 연동은 추후 단계에서 진행합니다.</Typography.Text>
      </Card>
    </PageShell>
  )
}
