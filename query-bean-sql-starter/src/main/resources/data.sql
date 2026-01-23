INSERT INTO "user" (name, age, birth, balance, create_time, create_by) VALUES ('susan', 28, '1997-07-15', 32142.39, CURRENT_TIMESTAMP, 'admin');
INSERT INTO "user" (name, age, birth, balance, create_time, create_by) VALUES ('alice', 24, '2001-07-12', 3123.00, CURRENT_TIMESTAMP, 'admin');

INSERT INTO "order" (order_no, user_id, user_name, amount, status, created_at) VALUES ('ORD001', 1, 'susan', 150.00, 'active', CURRENT_TIMESTAMP);
INSERT INTO "order" (order_no, user_id, user_name, amount, status, created_at) VALUES ('ORD002', 1, 'susan', 250.00, 'active', CURRENT_TIMESTAMP);
INSERT INTO "order" (order_no, user_id, user_name, amount, status, created_at) VALUES ('ORD003', 2, 'alice', 80.00, 'pending', CURRENT_TIMESTAMP);
INSERT INTO "order" (order_no, user_id, user_name, amount, status, created_at) VALUES ('ORD004', 2, 'alice', 320.00, 'completed', CURRENT_TIMESTAMP);
