export type AdminLoginRequest = {
  phoneNumber: string
  password: string
}

export type AdminRole = 'OPERATOR' | 'ADMIN' | 'SUPER_ADMIN'

export type AdminLoginResponse = {
  adminUserId: string
  phoneNumber: string
  email: string | null
  name: string
  role: AdminRole
  accessToken: string
}

export type AdminRegisterRequest = {
  phoneNumber: string
  email?: string | null
  name: string
  password: string
}

export type AdminRegisterResponse = {
  adminUserId: string
  phoneNumber: string
  email: string | null
  name: string
  role: AdminRole
}

export type TenantPublicItem = {
  tenantId: string
  name: string
  representativeCode: string
}

export type TenantPublicListResponse = {
  items: TenantPublicItem[]
}

export type PagedResponse<T> = {
  items: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type MemberSummary = {
  id: string
  memberNo: string
  name: string
  pointBalance: number
  statusCode: string
  phoneNumber: string | null
  email: string | null
  webId: string | null
  joinedAt: string
  dormantAt: string | null
  withdrawnAt: string | null
}

export type MemberAddress = {
  id: string
  zipCode: string
  roadAddress: string
  jibunAddress: string | null
  detailAddress: string | null
  buildingName: string | null
  siDo: string | null
  siGunGu: string | null
  eupMyeonDong: string | null
  legalDongCode: string | null
}

export type MemberDetail = {
  id: string
  memberNo: string
  name: string
  pointBalance: number
  gradeName: string | null
  birthDate: string | null
  calendarType: 'SOLAR' | 'LUNAR' | null
  gender: 'MALE' | 'FEMALE' | 'UNKNOWN' | null
  phoneNumber: string | null
  email: string | null
  address: MemberAddress | null
  webId: string | null
  statusCode: string
  joinedAt: string
  dormantAt: string | null
  withdrawnAt: string | null
  ci: string | null
  anniversaries: string | null
}

export type MemberLedger = {
  id: string
  eventType: string
  statusCodeBefore: string
  statusCodeAfter: string
  createdAt: string
}

/** 포인트 적립/사용/소멸 이력 (PointLedger) */
export type PointLedgerItem = {
  id: string
  memberNo: string
  eventType: 'EARN' | 'USE' | 'EXPIRE_AUTO' | 'EXPIRE_MANUAL' | 'ADJUST_EARN' | 'ADJUST_USE'
  amount: number
  reason: string | null
  sourceChannel: string
  expiresAt: string | null
  approvalNo: string
  referenceType: string | null
  referenceId: string | null
  createdAt: string
}

