import {
  Alert,
  Button,
  Card,
  Descriptions,
  Form,
  InputNumber,
  Modal,
  Space,
  Switch,
  Table,
  Tag,
  Tooltip,
  Typography,
  message,
} from 'antd'
import { ThunderboltOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import React from 'react'
import { useParams } from 'react-router-dom'
import { PageShell } from '../common/PageShell'
import { col } from '../../shared/tableColumns'
import {
  batchLabel,
  batchStatusMeta,
  useBatchConfig,
  useBatchExecutions,
  useRunBatch,
  useUpdateBatchConfig,
  type BatchConfig,
  type BatchExecution,
} from '../../shared/batch'

type ConfigForm = {
  enabled: boolean
  runHour: number
  thresholdDays: number
}

function fmt(v: string | null | undefined): string {
  return v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '-'
}

export function BatchDetailPage() {
  const { batchName = '' } = useParams()
  const configQuery = useBatchConfig(batchName)
  const updateMutation = useUpdateBatchConfig(batchName)
  const runMutation = useRunBatch(batchName)

  const [form] = Form.useForm<ConfigForm>()
  const [page, setPage] = React.useState(0)
  const size = 10
  const executionsQuery = useBatchExecutions(batchName, page, size)

  const config = configQuery.data as BatchConfig | undefined

  // 상세 로드 시 폼을 서버 값으로 동기화한다.
  React.useEffect(() => {
    if (config) {
      form.setFieldsValue({
        enabled: config.enabled,
        runHour: config.runHour,
        thresholdDays: config.thresholdDays,
      })
    }
  }, [config, form])

  // 실행 버튼 선제 차단은 서버의 최신 enabled 값을 기준으로 판단한다.
  const enabled = config?.enabled ?? false

  const onSave = async () => {
    const v = await form.validateFields()
    try {
      await updateMutation.mutateAsync({
        enabled: v.enabled,
        runHour: v.runHour,
        thresholdDays: v.thresholdDays,
      })
      message.success('배치 설정이 저장되었습니다.')
    } catch (e: any) {
      const status = e?.response?.status
      if (status === 404) {
        message.error('배치 설정을 찾을 수 없습니다.')
        return
      }
      message.error(e?.response?.data?.message ?? e?.message ?? '저장에 실패했습니다.')
    }
  }

  const runNow = async () => {
    try {
      const result = await runMutation.mutateAsync()
      const meta = batchStatusMeta(result.status)
      message.success(
        `실행 완료 (${meta.label}) · 처리 ${result.processedCount}건 · 실패 ${result.errorCount}건`
      )
      setPage(0)
    } catch (e: any) {
      const status = e?.response?.status
      const serverMsg = e?.response?.data?.message as string | undefined
      if (status === 409) {
        message.warning(serverMsg ?? '다른 배치 실행이 진행 중입니다. 잠시 후 다시 시도하세요.')
        return
      }
      if (status === 404) {
        message.error(serverMsg ?? '배치 설정을 찾을 수 없습니다.')
        return
      }
      message.error(serverMsg ?? e?.message ?? '실행에 실패했습니다.')
    }
  }

  const confirmRun = () => {
    const days = config?.thresholdDays ?? 0
    Modal.confirm({
      title: `${batchLabel(batchName)} 지금 실행`,
      icon: <ThunderboltOutlined style={{ color: '#faad14' }} />,
      content: (
        <Space direction="vertical" size={8}>
          <Typography.Text>
            탈회 요청 후 {days}일이 경과한 회원을 즉시 자동탈회 처리합니다.
          </Typography.Text>
          <Typography.Text type="danger">
            포인트 소각·개인정보 익명화가 동반되어 되돌리기 어려운 작업입니다. 계속하시겠습니까?
          </Typography.Text>
        </Space>
      ),
      okText: '실행',
      okButtonProps: { danger: true },
      cancelText: '취소',
      onOk: runNow,
    })
  }

  if (configQuery.isError) {
    const status = (configQuery.error as any)?.response?.status
    return (
      <PageShell title="배치 상세">
        <Alert
          type="error"
          showIcon
          message={status === 404 ? '배치 설정을 찾을 수 없습니다.' : '배치 설정을 불러오지 못했습니다.'}
          description={
            status === 404
              ? '해당 배치 설정이 아직 등록되지 않았습니다. 배치 목록에서 설정을 먼저 추가하세요.'
              : undefined
          }
          action={
            <Button size="small" onClick={() => configQuery.refetch()}>
              다시 시도
            </Button>
          }
        />
      </PageShell>
    )
  }

  const runDisabled = !enabled || runMutation.isPending

  return (
    <PageShell
      title={batchLabel(batchName)}
      extra={
        <Space>
          {!enabled && !configQuery.isLoading ? (
            <Typography.Text type="secondary" style={{ fontSize: 12 }}>
              비활성 상태에서는 실행할 수 없습니다.
            </Typography.Text>
          ) : null}
          <Tooltip title={!enabled ? '비활성 상태에서는 실행할 수 없습니다.' : ''}>
            <Button
              type="primary"
              danger
              icon={<ThunderboltOutlined />}
              disabled={runDisabled}
              loading={runMutation.isPending}
              onClick={confirmRun}
            >
              지금 실행
            </Button>
          </Tooltip>
        </Space>
      }
    >
      <Card title="배치 설정" loading={configQuery.isLoading}>
        <Form form={form} layout="vertical" requiredMark={false} style={{ maxWidth: 420 }}>
          <Form.Item label="활성 여부" name="enabled" valuePropName="checked">
            <Switch checkedChildren="ON" unCheckedChildren="OFF" />
          </Form.Item>
          <Form.Item
            label="실행 시각 (매일 HH:00)"
            name="runHour"
            rules={[
              { required: true, message: '실행 시각을 입력하세요' },
              { type: 'number', min: 0, max: 23, message: '0~23 사이 값을 입력하세요' },
            ]}
          >
            <InputNumber min={0} max={23} step={1} style={{ width: '100%' }} addonAfter="시" />
          </Form.Item>
          <Form.Item
            label="유예기간 (탈회 요청 후 N일 경과 시 자동탈회)"
            name="thresholdDays"
            rules={[
              { required: true, message: '유예기간을 입력하세요' },
              { type: 'number', min: 1, message: '1일 이상 입력하세요' },
            ]}
          >
            <InputNumber min={1} step={1} style={{ width: '100%' }} addonAfter="일" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" onClick={onSave} loading={updateMutation.isPending}>
              저장
            </Button>
          </Form.Item>
        </Form>

        {config ? (
          <Descriptions size="small" column={1} style={{ marginTop: 8, maxWidth: 420 }}>
            <Descriptions.Item label="마지막 수정자">{config.updatedBy ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="마지막 수정일시">{fmt(config.updatedAt)}</Descriptions.Item>
          </Descriptions>
        ) : null}
      </Card>

      <Card title="실행 이력">
        <Table<BatchExecution>
          rowKey={(r) => r.id}
          dataSource={executionsQuery.data?.items ?? []}
          loading={executionsQuery.isLoading}
          pagination={{
            current: (executionsQuery.data?.page ?? 0) + 1,
            pageSize: size,
            total: executionsQuery.data?.totalElements ?? 0,
            onChange: (p) => setPage(p - 1),
            showSizeChanger: false,
          }}
          columns={[
            {
              ...col('상태'),
              dataIndex: 'status',
              width: 110,
              render: (v: string) => {
                const meta = batchStatusMeta(v)
                return <Tag color={meta.color}>{meta.label}</Tag>
              },
            },
            {
              ...col('시작시각'),
              dataIndex: 'startedAt',
              width: 190,
              render: (v: string) => fmt(v),
            },
            {
              ...col('종료시각'),
              dataIndex: 'finishedAt',
              width: 190,
              render: (v: string | null) => fmt(v),
            },
            {
              ...col('처리건수', 'right'),
              dataIndex: 'processedCount',
              width: 100,
              render: (v: number) => v?.toLocaleString() ?? 0,
            },
            {
              ...col('실패건수', 'right'),
              dataIndex: 'errorCount',
              width: 100,
              render: (v: number) => v?.toLocaleString() ?? 0,
            },
            {
              ...col('에러메시지', 'left'),
              dataIndex: 'errorMessage',
              ellipsis: true,
              render: (v: string | null) =>
                v ? (
                  <Tooltip title={v}>
                    <Typography.Text type="danger" ellipsis style={{ maxWidth: 280 }}>
                      {v}
                    </Typography.Text>
                  </Tooltip>
                ) : (
                  '-'
                ),
            },
          ]}
        />
      </Card>
    </PageShell>
  )
}
