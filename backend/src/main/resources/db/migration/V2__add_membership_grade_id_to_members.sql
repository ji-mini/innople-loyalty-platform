-- members 테이블에 회원등급 FK 추가
ALTER TABLE members ADD COLUMN IF NOT EXISTS membership_grade_id UUID REFERENCES membership_grades(id);
