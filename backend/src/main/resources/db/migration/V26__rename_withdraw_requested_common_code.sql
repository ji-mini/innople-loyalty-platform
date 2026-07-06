-- 기존 tenant들의 MEMBER_STATUS 공통코드 중 WITHDRAW_REQUESTED 라벨을 정식 명칭('탈퇴요청')으로 일괄 정리.
-- 코드값(code)은 불변, name(라벨)만 갱신한다. 이미 '탈퇴요청'인 row는 WHERE 조건으로 제외되어 멱등하다.
UPDATE common_codes
SET name = '탈퇴요청'
WHERE code_group = 'MEMBER_STATUS'
  AND code = 'WITHDRAW_REQUESTED'
  AND name <> '탈퇴요청';
