import type { CSSProperties, ReactNode } from 'react'
import type { TableColumnType } from 'antd'

export type ColAlign = 'left' | 'center' | 'right'

/**
 * 테이블 헤더 셀. 데이터 셀과 정렬을 일치시키고, 헤더 텍스트는 줄바꿈되지 않도록 한다.
 */
export function headerCell(text: ReactNode, align: ColAlign = 'center') {
  const style: CSSProperties = { textAlign: align, whiteSpace: 'nowrap' }
  return <div style={style}>{text}</div>
}

/**
 * Ant Design Table 컬럼 정의 헬퍼.
 * 헤더/셀 정렬을 함께 지정하고(align), 헤더는 nowrap 처리한다.
 * width, ellipsis, dataIndex, render 등 나머지 컬럼 속성은 extra로 전달한다.
 * 반환 타입은 Ant Design의 TableColumnType과 호환된다.
 *
 * 사용 예) { ...col('적립률(%)', 'center', { width: 120 }), dataIndex: 'earnRatePercent', render: ... }
 */
export function col<T = any>(
  title: ReactNode,
  align: ColAlign = 'center',
  extra?: Partial<TableColumnType<T>>,
): TableColumnType<T> {
  return {
    title: headerCell(title, align),
    align,
    ...extra,
  }
}
