-- 일반 unique 제약 제거
ALTER TABLE users DROP INDEX nickname;
ALTER TABLE users DROP INDEX phone_number;

-- Functional unique index 생성 (활성 사용자만 unique 강제)
-- 탈퇴자(deleted_at IS NOT NULL)는 IF 결과가 NULL → unique 체크에서 제외
ALTER TABLE users
    ADD UNIQUE INDEX uk_users_nickname_alive ((IF(deleted_at IS NULL, nickname, NULL)));

ALTER TABLE users
    ADD UNIQUE INDEX uk_users_phone_alive ((IF(deleted_at IS NULL, phone_number, NULL)));