CREATE TABLE IF NOT EXISTS "user"
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)    NULL,
    age         INT            NULL,
    birth       DATE           NULL,
    balance     DECIMAL(16, 2) NULL,
    create_time DATETIME       NULL,
    create_by   VARCHAR(50)    NULL
);

CREATE TABLE IF NOT EXISTS "order"
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no    VARCHAR(50)    NULL,
    user_id     BIGINT         NULL,
    user_name   VARCHAR(50)    NULL,
    amount      DECIMAL(16, 2) NULL,
    status      VARCHAR(20)    NULL,
    created_at  DATETIME       NULL
    );