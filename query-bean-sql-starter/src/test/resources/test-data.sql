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

DELETE FROM "user";

INSERT INTO "user" (name, age, birth, balance, create_time, create_by) VALUES ('susan', 28, '1997-07-15', 32142.39, CURRENT_TIMESTAMP, 'admin');
INSERT INTO "user" (name, age, birth, balance, create_time, create_by) VALUES ('alice', 24, '2001-07-12', 3123.00, CURRENT_TIMESTAMP, 'admin');
