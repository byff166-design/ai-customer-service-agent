MERGE INTO customer_order (order_no, customer_name, status, amount, created_at)
KEY(order_no) VALUES ('ORD1001', '张三', 'SHIPPED', 299.00, TIMESTAMP '2026-08-12 10:00:00');
MERGE INTO customer_order (order_no, customer_name, status, amount, created_at)
KEY(order_no) VALUES ('ORD1002', '李四', 'PAID', 88.50, TIMESTAMP '2026-08-15 14:20:00');
MERGE INTO customer_order (order_no, customer_name, status, amount, created_at)
KEY(order_no) VALUES ('ORD1003', '王五', 'DELIVERED', 568.00, TIMESTAMP '2026-08-08 09:30:00');
MERGE INTO customer_order (order_no, customer_name, status, amount, created_at)
KEY(order_no) VALUES ('ORD1004', '赵六', 'CANCELLED', 129.90, TIMESTAMP '2026-08-11 16:45:00');

MERGE INTO logistics_info (order_no, company, tracking_no, status, updated_at)
KEY(order_no) VALUES ('ORD1001', '顺丰速运', 'SF202608120001', '运输中，已到达深圳转运中心', TIMESTAMP '2026-08-17 11:30:00');
MERGE INTO logistics_info (order_no, company, tracking_no, status, updated_at)
KEY(order_no) VALUES ('ORD1003', '京东物流', 'JD202608080003', '已签收', TIMESTAMP '2026-08-10 18:12:00');
