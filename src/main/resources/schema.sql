CREATE TABLE IF NOT EXISTS customer_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    customer_name VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS logistics_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    company VARCHAR(64) NOT NULL,
    tracking_no VARCHAR(64) NOT NULL,
    status VARCHAR(128) NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS support_ticket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_no VARCHAR(32) NOT NULL UNIQUE,
    order_no VARCHAR(32),
    problem_description VARCHAR(500) NOT NULL,
    status VARCHAR(32) NOT NULL,
    priority VARCHAR(16) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS tool_call_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    tool_name VARCHAR(64) NOT NULL,
    request_summary VARCHAR(500),
    result_summary VARCHAR(1000),
    success BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ticket_order_no ON support_ticket(order_no);
CREATE INDEX IF NOT EXISTS idx_tool_log_session ON tool_call_log(session_id);
