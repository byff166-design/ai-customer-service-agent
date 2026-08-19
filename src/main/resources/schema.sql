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
    trace_id VARCHAR(64),
    session_id VARCHAR(64) NOT NULL,
    tool_name VARCHAR(64) NOT NULL,
    request_summary VARCHAR(500),
    result_summary VARCHAR(1000),
    success BOOLEAN NOT NULL,
    cost_ms BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL
);

ALTER TABLE tool_call_log ADD COLUMN IF NOT EXISTS trace_id VARCHAR(64);
ALTER TABLE tool_call_log ADD COLUMN IF NOT EXISTS cost_ms BIGINT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS conversation_summary (
    session_id VARCHAR(64) PRIMARY KEY,
    summary VARCHAR(4000) NOT NULL,
    summarized_message_count BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ticket_order_no ON support_ticket(order_no);
CREATE INDEX IF NOT EXISTS idx_tool_log_session ON tool_call_log(session_id);
CREATE INDEX IF NOT EXISTS idx_tool_log_trace ON tool_call_log(trace_id);
