import { Card, Typography } from 'antd'
import { PageShell } from '../common/PageShell'

/**
 * 직원할인(직원 식별·할인 적용 여부·할인율) 운영 화면 — API 연동 전 안내용 플레이스홀더.
 */
export function EmployeeManagementPage() {
  return (
    <PageShell
      title="직원관리"
      extra={
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          매장 직원을 등록하고, 직원할인 적용 여부와 할인율을 관리합니다.
        </Typography.Text>
      }
    >
      <Card>
        <Typography.Paragraph style={{ marginBottom: 8 }}>
          <Typography.Text strong>예정 데이터</Typography.Text>
        </Typography.Paragraph>
        <ul style={{ margin: '0 0 16px', paddingLeft: 20, color: 'rgba(0,0,0,0.65)' }}>
          <li>직원 등록(식별 정보·소속 등)</li>
          <li>직원할인 적용 여부(할인을 줄지 말지)</li>
          <li>직원할인율(몇 % 할인할지)</li>
        </ul>
        <Typography.Text type="secondary">기능 구현 및 API 연동은 추후 단계에서 진행합니다.</Typography.Text>
      </Card>
    </PageShell>
  )
}
