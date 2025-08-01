-- auto-generated definition
create table user
(
    id          bigint auto_increment
        primary key,
    name        varchar(50)    null,
    age         int            null,
    birth       date           null,
    balance     decimal(16, 2) null,
    create_time datetime       null,
    create_by   varchar(50)    null
);

INSERT INTO user (name, age, birth, balance, create_time, create_by) VALUES ('susan', 28, '1997-07-15', 32142.39, CURRENT_TIMESTAMP, 'admin');
INSERT INTO user (name, age, birth, balance, create_time, create_by) VALUES ('alice', 24, '2001-07-12', 3123.00, CURRENT_TIMESTAMP, 'admin');
