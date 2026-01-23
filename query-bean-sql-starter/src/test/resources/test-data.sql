-- 测试数据初始化脚本
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

DELETE FROM "user";
DELETE FROM "order";

INSERT INTO "user" (name, age, birth, balance, create_time, create_by) VALUES ('susan', 28, '1997-07-15', 32142.39, CURRENT_TIMESTAMP, 'admin');
INSERT INTO "user" (name, age, birth, balance, create_time, create_by) VALUES ('alice', 24, '2001-07-12', 3123.00, CURRENT_TIMESTAMP, 'admin');

INSERT INTO "order" (order_no, user_id, user_name, amount, status, created_at) VALUES ('ORD001', 1, 'susan', 150.00, 'active', CURRENT_TIMESTAMP);
INSERT INTO "order" (order_no, user_id, user_name, amount, status, created_at) VALUES ('ORD002', 1, 'susan', 250.00, 'active', CURRENT_TIMESTAMP);
INSERT INTO "order" (order_no, user_id, user_name, amount, status, created_at) VALUES ('ORD003', 2, 'alice', 80.00, 'pending', CURRENT_TIMESTAMP);
INSERT INTO "order" (order_no, user_id, user_name, amount, status, created_at) VALUES ('ORD004', 2, 'alice', 320.00, 'completed', CURRENT_TIMESTAMP);
