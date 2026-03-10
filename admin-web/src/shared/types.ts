export type AdminLoginRequest = {
  phoneNumber: string
  password: string
}

export type AdminLoginResponse = {
  adminUserId: string
  phoneNumber: string
  email: string | null
  name: string
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
}

export type TenantPublicItem = {
  tenantId: string
  name: string
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
  statusCode: string
  phoneNumber: string | null
  webId: string | null
  joinedAt: string
  dormantAt: string | null
  withdrawnAt: string | null
}

export type MemberDetail = {
  id: string
  memberNo: string
  name: string
  birthDate: string | null
  calendarType: 'SOLAR' | 'LUNAR' | null
  gender: 'MALE' | 'FEMALE' | 'UNKNOWN' | null
  phoneNumber: string | null
  address: string | null
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
  memberNo: string
  eventType: string
  statusCodeBefore: string
  statusCodeAfter: string
  createdAt: string
}

