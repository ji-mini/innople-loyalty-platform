## INNOPLE Loyalty Platform (Spring Boot 3 / Java 21)

멀티테넌트 SaaS 로열티 플랫폼의 도메인/포인트(ledger 기반) 기본 골격입니다.

### 핵심 규칙

- **멀티테넌시**: 모든 엔티티에 `tenantId` 포함, 모든 Repository 조회는 `tenantId` 조건 포함
- **TenantContext**: 요청 헤더 `X-Tenant-Id` (기본값)에서 UUID를 읽어 `TenantContext`에 설정
- **포인트는 Ledger 기반**
  - 적립: `PointLot`(로트) 생성 + `PointLedger` 기록 + `PointAccount.currentBalance` 갱신(캐시)
  - 사용/만료: **FEFO**(`expiresAt ASC, createdAt ASC`)로 `PointLot.remainingAmount` 차감 + `PointAllocation` 생성 + `PointLedger` 기록 + `PointAccount.currentBalance` 갱신
- **낙관적 락**: `PointAccount.version`에 `@Version`
- **트랜잭션**: `PointService`에서 단일 트랜잭션으로 처리

### PostgreSQL 실행

#### 개발(dev)

`.env.dev` 파일에 값을 채운 뒤 실행하세요.

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d
```

#### 운영(prd)

`.env.prd` 파일에 값을 채운 뒤 실행하세요.

```bash
docker compose -f docker-compose.yml -f docker-compose.prd.yml up -d
```

#### 포함되는 서비스

- **api**: Spring Boot 애플리케이션(도커 빌드로 bootJar 생성 후 실행)

#### 환경변수 흐름(중요)

- dev/prd 모두 DB는 **외부(PostgreSQL RDS 등)** 로 연결하는 구조입니다.
- `.env.dev` / `.env.prd`에 아래 키를 넣으면, Spring이 그대로 사용합니다.
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`

예) dev의 RDS 엔드포인트가 `mini-forge-dev-db.cp6ciiyw4ag1.ap-northeast-2.rds.amazonaws.com` 이면:

- `SPRING_DATASOURCE_URL=jdbc:postgresql://mini-forge-dev-db.cp6ciiyw4ag1.ap-northeast-2.rds.amazonaws.com:5432/<DB_NAME>?currentSchema=public`

### 애플리케이션 실행

현재 PC에 Gradle이 설치되어 있다면:

```bash
gradle bootRun
```

Gradle이 없다면, Gradle 설치 후 실행하거나 IDE(IntelliJ 등)에서 Gradle 프로젝트로 import 해서 실행하세요.

#### Spring 프로파일

- **dev(기본값)**: `application-dev.yml` 사용 (`ddl-auto: update`)
- **prd**: `application-prd.yml` 사용 (`ddl-auto: validate`)

prd로 실행 예:

```bash
gradle bootRun --args='--spring.profiles.active=prd'
```

### 포인트 API (보안 없음)

모든 요청에 헤더가 필요합니다.

- `X-Tenant-Id: {tenantUuid}`

#### 적립

`POST /api/v1/points/earn`

```json
{
  "memberId": "00000000-0000-0000-0000-000000000001",
  "amount": 1000,
  "expiresAt": "2030-01-01T00:00:00Z",
  "reason": "signup bonus"
}
```

#### 사용

`POST /api/v1/points/use`

```json
{
  "memberId": "00000000-0000-0000-0000-000000000001",
  "amount": 300,
  "reason": "order #1234"
}
```

#### 수동 만료

`POST /api/v1/points/expire/manual`

```json
{
  "memberId": "00000000-0000-0000-0000-000000000001",
  "referenceAt": "2026-02-12T00:00:00Z",
  "reason": "manual expire"
}
```

