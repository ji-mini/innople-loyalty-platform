import {
  Button,
  Card,
  Form,
  InputNumber,
  Modal,
  Select,
  Space,
  Switch,
  Table,
  Tag,
  Tooltip,
  Typography,
  message,
} from 'antd'
import dayjs from 'dayjs'
import React from 'react'
import { useNavigate } from 'react-router-dom'
import { PageShell } from '../common/PageShell'
import { col } from '../../shared/tableColumns'
import {
  BATCH_TYPES,
  batchLabel,
  useBatchConfigs,
  useCreateBatchConfig,
  type BatchConfig,
} from '../../shared/batch'

type CreateForm = {
  batchName: string
  runHour: number
  thresholdDays: number
  enabled: boolean
}

export function BatchListPage() {
  const nav = useNavigate()
  const configsQuery = useBatchConfigs()
  const createMutation = useCreateBatchConfig()
  const [open, setOpen] = React.useState(false)
  const [form] = Form.useForm<CreateForm>()

  const rows = configsQuery.data ?? []

  // 이미 등록된 배치는 선택지에서 제외한다(테넌트당 batch_name 1건 → 중복 생성 시 409 충돌 방지).
  const registeredNames = React.useMemo(() => new Set(rows.map((r) => r.batchName)), [rows])
  const availableTypes = React.useMemo(
    () => BATCH_TYPES.filter((t) => !registeredNames.has(t.name)),
    [registeredNames]
  )
  const noneAvailable = availableTypes.length === 0

  const openCreate = () => {
    if (noneAvailable) return
    form.resetFields()
    form.setFieldsValue({
      batchName: availableTypes[0].name,
      runHour: 3,
      thresholdDays: 30,
      enabled: false,
    })
    setOpen(true)
  }

  const onSubmitCreate = async () => {
    const v = await form.validateFields()
    try {
      const created = await createMutation.mutateAsync({
        batchName: v.batchName,
        enabled: v.enabled,
        runHour: v.runHour,
        thresholdDays: v.thresholdDays,
      })
      message.success('배치 설정이 생성되었습니다.')
      setOpen(false)
      nav(`/batches/${encodeURIComponent(created.batchName)}`)
    } catch (e: any) {
      const status = e?.response?.status
      if (status === 409) {
        message.warning(e?.response?.data?.message ?? '이미 존재하는 배치 설정입니다.')
        setOpen(false)
        configsQuery.refetch()
        return
      }
      message.error(e?.response?.data?.message ?? e?.message ?? '생성에 실패했습니다.')
    }
  }

  return (
    <PageShell
      title="배치관리"
      extra={
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            테넌트별 배치 스케줄과 실행을 관리합니다.
          </Typography.Text>
          <Tooltip title={noneAvailable ? '추가할 수 있는 배치가 없습니다. (지원 배치가 모두 등록됨)' : ''}>
            <Button type="primary" onClick={openCreate} disabled={noneAvailable}>
              배치 추가
            </Button>
          </Tooltip>
        </div>
      }
    >
      <Card>
        <Table<BatchConfig>
          rowKey={(r) => r.id}
          dataSource={rows}
          loading={configsQuery.isLoading}
          pagination={false}
          onRow={(r) => ({
            onClick: () => nav(`/batches/${encodeURIComponent(r.batchName)}`),
            style: { cursor: 'pointer' },
          })}
          columns={[
            {
              ...col('배치명', 'left'),
              dataIndex: 'batchName',
              render: (v: string) => <Typography.Text strong>{batchLabel(v)}</Typography.Text>,
            },
            {
              ...col('활성 여부'),
              dataIndex: 'enabled',
              width: 120,
              render: (v: boolean) => (v ? <Tag color="green">활성</Tag> : <Tag>비활성</Tag>),
            },
            {
              ...col('실행 시각'),
              dataIndex: 'runHour',
              width: 140,
              render: (v: number) => `매일 ${String(v).padStart(2, '0')}:00`,
            },
            {
              ...col('유예기간'),
              dataIndex: 'thresholdDays',
              width: 120,
              render: (v: number) => `${v}일`,
            },
            {
              ...col('마지막 실행일시'),
              dataIndex: 'lastExecutedAt',
              width: 190,
              render: (v: string | null) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '-'),
            },
            {
              ...col('마지막 수정'),
              dataIndex: 'updatedAt',
              width: 190,
              render: (v: string) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '-'),
            },
          ]}
          locale={{
            emptyText: (
              <Space direction="vertical" size={6}>
                <Typography.Text>등록된 배치 설정이 없습니다.</Typography.Text>
                <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                  “배치 추가”로 배치 실행 시각과 유예기간을 먼저 등록하세요.
                </Typography.Text>
              </Space>
            ),
          }}
        />
      </Card>

      <Modal
        open={open}
        title="배치 추가"
        okText="생성"
        onOk={onSubmitCreate}
        confirmLoading={createMutation.isPending}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" requiredMark={false}>
          <Form.Item
            label="배치"
            name="batchName"
            rules={[{ required: true, message: '추가할 배치를 선택하세요' }]}
          >
            <Select
              placeholder="추가할 배치를 선택하세요"
              options={availableTypes.map((t) => ({
                value: t.name,
                label: `${t.label} (${t.name})`,
              }))}
            />
          </Form.Item>
          <Form.Item
            label="실행 시각 (매일 HH:00)"
            name="runHour"
            rules={[{ required: true, message: '실행 시각(0~23)을 입력하세요' }]}
          >
            <InputNumber min={0} max={23} step={1} style={{ width: '100%' }} addonAfter="시" />
          </Form.Item>
          <Form.Item
            label="유예기간 (탈회 요청 후 N일 경과 시 자동탈회)"
            name="thresholdDays"
            rules={[{ required: true, message: '유예기간(1일 이상)을 입력하세요' }]}
          >
            <InputNumber min={1} step={1} style={{ width: '100%' }} addonAfter="일" />
          </Form.Item>
          <Form.Item label="활성 여부" name="enabled" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </PageShell>
  )
}
