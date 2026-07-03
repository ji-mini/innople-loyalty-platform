-- 회원 휴대폰번호 중복 등록 방지 (2겹 방어 중 DB 레벨 = 최후 방어).
-- 정책: 휴대폰번호는 "테넌트별" 유니크 (멀티테넌시 격리 구조상 글로벌 유니크는 부적절).
-- 논리삭제(soft delete) 컬럼이 없으므로 일반 UNIQUE 제약으로 충분하다.
-- phone_number 는 등록 시 선택값(NULL 허용)이지만, Postgres UNIQUE 는 NULL 을 서로 다른 값으로
-- 취급하므로 미입력 회원이 여럿이어도 문제되지 않는다.
--
-- [사전 조건] 기존 중복이 있으면 제약 추가가 실패한다(테스트 데이터는 수동 정리 예정). 확인 쿼리:
--   SELECT tenant_id, phone_number, count(*)
--   FROM members
--   WHERE phone_number IS NOT NULL
--   GROUP BY tenant_id, phone_number
--   HAVING count(*) > 1;

ALTER TABLE members
    ADD CONSTRAINT uk_members_tenant_phone_number UNIQUE (tenant_id, phone_number);
