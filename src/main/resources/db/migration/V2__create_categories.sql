-- 카테고리 테이블 - 자기참조로 2단계 구조 지원 (대분류 → 중분류)
CREATE TABLE categories (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(50)  NOT NULL,
    parent_id     BIGINT       NULL,
    display_order INT          NOT NULL DEFAULT 0,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_parent_name UNIQUE (parent_id, name),
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id),
    INDEX idx_categories_parent (parent_id)
);

-- 대분류 시드 (parent_id NULL). 중분류는 추후 마이그레이션에서 추가 예정.
INSERT INTO categories (name, parent_id, display_order, active, created_at, updated_at) VALUES
('의류', NULL, 1, TRUE, NOW(6), NOW(6)),
('디지털/가전', NULL, 2, TRUE, NOW(6), NOW(6)),
('가구/인테리어', NULL, 3, TRUE, NOW(6), NOW(6)),
('도서', NULL, 4, TRUE, NOW(6), NOW(6)),
('뷰티', NULL, 5, TRUE, NOW(6), NOW(6)),
('스포츠/레저', NULL, 6, TRUE, NOW(6), NOW(6)),
('기타', NULL, 7, TRUE, NOW(6), NOW(6));