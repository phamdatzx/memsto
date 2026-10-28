CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100),
    created_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE TABLE collections (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id   BIGINT       NOT NULL,
    name       VARCHAR(100) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_collections_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_collections_owner (owner_id, sort_order)
);

CREATE TABLE items (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    collection_id BIGINT       NOT NULL,
    type          VARCHAR(16)  NOT NULL, -- IMAGE | ICON | QUOTE
    object_key    VARCHAR(512),          -- null for QUOTE
    text_content  TEXT,                  -- quote text, or caption
    mime_type     VARCHAR(100),
    size_bytes    BIGINT,
    width         INT,
    height        INT,
    sort_order    INT          NOT NULL DEFAULT 0,
    created_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_items_collection FOREIGN KEY (collection_id) REFERENCES collections (id) ON DELETE CASCADE,
    INDEX idx_items_collection (collection_id, sort_order)
);
