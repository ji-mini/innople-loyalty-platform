# INNOPLE Loyalty Platform API 인터페이스 정의서

본 문서는 **현재 코드 기준**(Spring Boot 3 / Java 21)으로 정리한 외부 호출용 API 정의서입니다.

## 공통

### Base URL
- 예: `http://{host}:8090` (nginx 프록시) 또는 `http://{host}:3201` (backend 직접)

### Content-Type
- 요청/응답: `application/json`

### 멀티테넌시 헤더 (필수)
- **대상**: `/api/v1/public/**` 를 제외한 **모든 API**
- **헤더**: `X-Tenant-Id: {tenantUuid}`
- 누락/형식 오류 시: `400 Bad Request`

### 인증 헤더 (권장)
- 프론트는 `Authorization: Bearer {accessToken}` 를 전송합니다.
- **주의**: 현재 `SecurityConfig` 상 서버에서 강제 인증을 막아두진 않았습니다(permitAll). 추후 인증 필터가 붙으면 필수로 바뀝니다.

### 공통 에러 응답
- 대부분의 예외는 아래 형태로 반환됩니다.

```json
{
  "message": "에러 메시지",
  "timestamp": "2026-03-11T01:23:45.678Z"
}
```

### 주요 상태 코드
- **200 OK**: 정상 처리
- **400 Bad Request**: validation 실패, tenant 헤더 누락/형식 오류 등
- **401 Unauthorized**: 관리자 로그인 실패(Invalid credentials)
- **409 Conflict**: 비즈니스 충돌(중복 생성, 리소스 없음/상태 불일치, 포인트 부족 등)

---

## 1) Public (테넌트 헤더 불필요)

### 1.1 테넌트 목록 조회
- **GET** `/api/v1/public/tenants`

#### Response
```json
{
  "items": [
    { "tenantId": "8a4f3a9d-2755-4b7c-94da-14ffd5ebb61e", "name": "SPAO" }
  ]
}
```

### 1.2 헬스 체크
- **GET** `/api/v1/public/health`

#### Response
```json
{ "status": "UP", "time": "2026-03-11T01:23:45.678Z" }
```

---

## 2) 관리자 인증 (Tenant 헤더 필요)

### 2.1 관리자 로그인
- **POST** `/api/v1/admin/auth/login`

#### Request
```json
{
  "phoneNumber": "01012345678",
  "password": "password"
}
```

#### Response
```json
{
  "adminUserId": "11111111-1111-1111-1111-111111111111",
  "phoneNumber": "01012345678",
  "email": "admin@innople.com",
  "name": "관리자",
  "role": "SUPER_ADMIN",
  "accessToken": "c9a1bca8-3f1b-4f2d-8c5f-7b8f2d0b6e1a"
}
```

### 2.2 관리자 등록(회원가입)
- **POST** `/api/v1/admin/auth/register`

#### Request
```json
{
  "phoneNumber": "01012345678",
  "email": "admin@innople.com",
  "name": "관리자",
  "password": "password"
}
```

#### Response
```json
{
  "adminUserId": "11111111-1111-1111-1111-111111111111",
  "phoneNumber": "01012345678",
  "email": "admin@innople.com",
  "name": "관리자",
  "role": "OPERATOR"
}
```

---

## 3) 회원 (Tenant 헤더 필요)

### 3.1 회원 목록/검색 (Paged)
- **GET** `/api/v1/members`

#### Query Parameters
- `keyword` (optional): 통합 키워드
- `statusCode` (optional)
- `memberNo` (optional)
- `phoneNumber` (optional)
- `name` (optional)
- `webId` (optional)
- `joinedFrom` (optional, ISO DATE): `YYYY-MM-DD`
- `joinedTo` (optional, ISO DATE): `YYYY-MM-DD`
- `page` (default: 0)
- `size` (default: 20, max: 100)

#### Response
```json
{
  "items": [
    {
      "id": "22222222-2222-2222-2222-222222222222",
      "memberNo": "10000001",
      "name": "홍길동",
      "pointBalance": 1200,
      "statusCode": "ACTIVE",
      "phoneNumber": "01012345678",
      "webId": "honggildong",
      "joinedAt": "2026-03-11",
      "dormantAt": null,
      "withdrawnAt": null
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### 3.2 회원 상세 조회 (회원번호)
- **GET** `/api/v1/members/{memberNo}`

#### Response
```json
{
  "id": "22222222-2222-2222-2222-222222222222",
  "memberNo": "10000001",
  "name": "홍길동",
  "birthDate": null,
  "calendarType": null,
  "gender": null,
  "phoneNumber": "01012345678",
  "address": null,
  "webId": "honggildong",
  "statusCode": "ACTIVE",
  "joinedAt": "2026-03-11",
  "dormantAt": null,
  "withdrawnAt": null,
  "ci": null,
  "anniversaries": null
}
```

### 3.3 회원 원장(활동) 조회
- **GET** `/api/v1/members/{memberNo}/ledgers`

#### Query Parameters
- `limit` (default: 50, max: 200)

#### Response
```json
[
  {
    "id": "33333333-3333-3333-3333-333333333333",
    "memberNo": "10000001",
    "eventType": "REGISTER",
    "statusCodeBefore": null,
    "statusCodeAfter": "ACTIVE",
    "createdAt": "2026-03-11T01:23:45.678Z"
  }
]
```

### 3.4 회원 등록
- **POST** `/api/v1/members`

#### Request (MemberDtos.RegisterRequest)
```json
{
  "memberNo": "10000001",
  "name": "홍길동",
  "birthDate": "1990-01-01",
  "calendarType": "SOLAR",
  "gender": "MALE",
  "phoneNumber": "01012345678",
  "address": "서울시 ...",
  "webId": "honggildong",
  "statusCode": "ACTIVE",
  "joinedAt": "2026-03-11",
  "ci": "CI_STRING",
  "anniversaries": "..."
}
```

#### Response (MemberDtos.MemberResponse)
```json
{
  "id": "22222222-2222-2222-2222-222222222222",
  "memberNo": "10000001",
  "name": "홍길동",
  "birthDate": "1990-01-01",
  "calendarType": "SOLAR",
  "gender": "MALE",
  "phoneNumber": "01012345678",
  "address": "서울시 ...",
  "webId": "honggildong",
  "statusCode": "ACTIVE",
  "joinedAt": "2026-03-11",
  "dormantAt": null,
  "withdrawnAt": null,
  "ci": "CI_STRING",
  "anniversaries": "..."
}
```

### 3.5 회원 정보 수정
- **PUT** `/api/v1/members/{memberNo}`

#### Request (MemberDtos.UpdateInfoRequest)
```json
{
  "name": "홍길동",
  "birthDate": "1990-01-01",
  "calendarType": "SOLAR",
  "gender": "MALE",
  "phoneNumber": "01012345678",
  "address": "서울시 ...",
  "webId": "honggildong",
  "ci": "CI_STRING",
  "anniversaries": "..."
}
```

### 3.6 회원 상태 변경
- **PUT** `/api/v1/members/{memberNo}/status`

#### Request (MemberDtos.UpdateStatusRequest)
```json
{ "statusCode": "DORMANT", "dormantAt": "2026-03-11" }
```

### 3.7 회원 탈퇴 처리
- **PUT** `/api/v1/members/{memberNo}/withdraw`

#### Request (MemberDtos.WithdrawRequest)
```json
{ "withdrawnAt": "2026-03-11", "reason": "사용자 요청" }
```

---

## 4) 포인트 (Tenant 헤더 필요)

### 4.1 포인트 적립
- **POST** `/api/v1/points/earn`

#### Request (PointDtos.EarnRequest)
```json
{
  "memberId": "22222222-2222-2222-2222-222222222222",
  "amount": 1000,
  "expiresAt": "2027-03-11T00:00:00Z",
  "reason": "수기 적립"
}
```

#### Response (PointDtos.PointOperationResponse)
```json
{
  "ledgerId": "44444444-4444-4444-4444-444444444444",
  "eventType": "EARN",
  "amount": 1000,
  "currentBalance": 1000,
  "occurredAt": "2026-03-11T01:23:45.678Z"
}
```

### 4.2 포인트 사용(차감)
- **POST** `/api/v1/points/use`

#### Request (PointDtos.UseRequest)
```json
{ "memberId": "22222222-2222-2222-2222-222222222222", "amount": 500, "reason": "수기 차감" }
```

#### Response
```json
{
  "ledgerId": "55555555-5555-5555-5555-555555555555",
  "eventType": "USE",
  "amount": -500,
  "currentBalance": 500,
  "occurredAt": "2026-03-11T01:23:45.678Z"
}
```

### 4.3 포인트 수동 소멸(만료분 처리)
- **POST** `/api/v1/points/expire/manual`

#### Request (PointDtos.ManualExpireRequest)
```json
{
  "memberId": "22222222-2222-2222-2222-222222222222",
  "referenceAt": "2026-03-11T00:00:00Z",
  "reason": "수동 소멸"
}
```

#### Response
```json
{
  "ledgerId": "66666666-6666-6666-6666-666666666666",
  "eventType": "EXPIRE_MANUAL",
  "amount": -300,
  "currentBalance": 200,
  "occurredAt": "2026-03-11T01:23:45.678Z"
}
```

---

## 5) 포인트 정책 (Admin, Tenant 헤더 필요)

### 5.1 정책 목록(테넌트별)
- **GET** `/api/v1/admin/point-policies`

#### Response
```json
[
  {
    "id": "77777777-7777-7777-7777-777777777777",
    "pointType": "BASIC",
    "name": "기본 포인트",
    "validityDays": 365,
    "enabled": true,
    "description": "기본 적립 정책",
    "createdAt": "2026-03-11T01:23:45.678Z",
    "updatedAt": "2026-03-11T01:23:45.678Z"
  }
]
```

### 5.2 정책 생성
- **POST** `/api/v1/admin/point-policies`

#### Request
```json
{
  "pointType": "BASIC",
  "name": "기본 포인트",
  "validityDays": 365,
  "enabled": true,
  "description": "기본 적립 정책"
}
```

### 5.3 정책 수정
- **PUT** `/api/v1/admin/point-policies/{policyId}`

#### Request
```json
{
  "pointType": "EVENT",
  "name": "이벤트 포인트",
  "validityDays": 30,
  "enabled": true,
  "description": "기간 한정 이벤트"
}
```

---

## 6) 시스템 > 사용자 관리 (Admin, Tenant 헤더 필요)

### 6.1 어드민 사용자 목록/검색
- **GET** `/api/v1/admin/admin-users`

#### Query Parameters
- `keyword` (optional): 이름/이메일/휴대폰 부분 검색

#### Response
```json
[
  {
    "id": "11111111-1111-1111-1111-111111111111",
    "phoneNumber": "01012345678",
    "email": "admin@innople.com",
    "name": "관리자",
    "role": "SUPER_ADMIN",
    "createdAt": "2026-03-11T01:23:45.678Z",
    "updatedAt": "2026-03-11T01:23:45.678Z"
  }
]
```

### 6.2 어드민 사용자 생성
- **POST** `/api/v1/admin/admin-users`

#### Request
```json
{
  "phoneNumber": "01012345678",
  "email": "admin@innople.com",
  "name": "관리자",
  "password": "password",
  "role": "ADMIN"
}
```

### 6.3 어드민 사용자 수정
- **PUT** `/api/v1/admin/admin-users/{adminUserId}`

#### Request
```json
{
  "phoneNumber": "01012345678",
  "email": "admin@innople.com",
  "name": "관리자(수정)",
  "role": "SUPER_ADMIN"
}
```

---

## 호출 예시 (curl)

### 포인트 적립
```bash
curl -X POST "http://{host}:8090/api/v1/points/earn" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: {tenantUuid}" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{"memberId":"{memberUuid}","amount":1000,"expiresAt":"2027-03-11T00:00:00Z","reason":"수기 적립"}'
```

### 회원 등록
```bash
curl -X POST "http://{host}:8090/api/v1/members" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: {tenantUuid}" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{"memberNo":"10000001","name":"홍길동","statusCode":"ACTIVE","joinedAt":"2026-03-11"}'
```

