export type AdminLoginRequest = {
  email: string
  password: string
}

export type AdminLoginResponse = {
  adminUserId: string
  email: string
  name: string
  accessToken: string
}

export type AdminRegisterRequest = {
  email: string
  name: string
  password: string
}

export type AdminRegisterResponse = {
  adminUserId: string
  email: string
  name: string
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

