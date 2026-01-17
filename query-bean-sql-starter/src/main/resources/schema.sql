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