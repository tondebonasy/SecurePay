CREATE TABLE users (
  id CHAR(36) PRIMARY KEY,
  phone VARCHAR(32) NOT NULL UNIQUE,
  email VARCHAR(254) NULL UNIQUE,
  display_name VARCHAR(120) NULL,
  password_hash VARCHAR(255) NOT NULL,
  roles VARCHAR(255) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE device_sessions (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  device_id VARCHAR(128) NOT NULL,
  refresh_token_hash VARCHAR(255) NOT NULL,
  refresh_expires_at TIMESTAMP NOT NULL,
  revoked_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_session_user (user_id),
  INDEX idx_session_device (device_id),
  CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE user_device_tokens (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  device_id VARCHAR(128) NOT NULL,
  expo_push_token VARCHAR(255) NOT NULL,
  platform VARCHAR(32) NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_push_user_device (user_id, device_id),
  CONSTRAINT fk_push_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE transactions (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  currency VARCHAR(8) NOT NULL,
  status VARCHAR(32) NOT NULL,
  paypal_order_id VARCHAR(64) NULL,
  paypal_capture_id VARCHAR(64) NULL,
  idempotency_key VARCHAR(128) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_tx_user (user_id),
  INDEX idx_tx_status (status),
  UNIQUE KEY uq_tx_user_idem (user_id, idempotency_key),
  CONSTRAINT fk_tx_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE transaction_audit (
  id CHAR(36) PRIMARY KEY,
  transaction_id CHAR(36) NOT NULL,
  old_status VARCHAR(32) NULL,
  new_status VARCHAR(32) NOT NULL,
  message VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_audit_tx (transaction_id),
  CONSTRAINT fk_audit_tx FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);

CREATE TABLE transaction_proofs (
  id CHAR(36) PRIMARY KEY,
  transaction_id CHAR(36) NOT NULL,
  proof_hash VARCHAR(128) NOT NULL,
  stellar_tx_hash VARCHAR(128) NOT NULL,
  ledger BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_proof_tx (transaction_id),
  CONSTRAINT fk_proof_tx FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);

CREATE TABLE webhook_events (
  id CHAR(36) PRIMARY KEY,
  provider VARCHAR(16) NOT NULL,
  event_id VARCHAR(128) NOT NULL,
  event_type VARCHAR(128) NOT NULL,
  verified BOOLEAN NOT NULL DEFAULT FALSE,
  raw_body LONGTEXT NOT NULL,
  headers_json LONGTEXT NOT NULL,
  received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  processed_at TIMESTAMP NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'RECEIVED',
  UNIQUE KEY uq_webhook_event (provider, event_id)
);

CREATE TABLE audit_events (
  id CHAR(36) PRIMARY KEY,
  actor_user_id CHAR(36) NULL,
  actor_device_id VARCHAR(128) NULL,
  action VARCHAR(128) NOT NULL,
  entity_type VARCHAR(64) NULL,
  entity_id VARCHAR(64) NULL,
  ip_address VARCHAR(64) NULL,
  user_agent VARCHAR(255) NULL,
  metadata_json LONGTEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_audit_actor (actor_user_id),
  INDEX idx_audit_action (action)
);

CREATE TABLE outbox_jobs (
  id CHAR(36) PRIMARY KEY,
  job_type VARCHAR(32) NOT NULL,
  payload_json LONGTEXT NOT NULL,
  status VARCHAR(16) NOT NULL,
  attempts INT NOT NULL DEFAULT 0,
  last_error VARCHAR(255) NULL,
  next_run_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_outbox_status_next (status, next_run_at)
);

CREATE TABLE idempotency_records (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  idem_key VARCHAR(128) NOT NULL,
  request_hash VARCHAR(128) NOT NULL,
  response_status INT NOT NULL,
  response_body LONGTEXT NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_idem_user_key (user_id, idem_key),
  INDEX idx_idem_expires (expires_at),
  CONSTRAINT fk_idem_user FOREIGN KEY (user_id) REFERENCES users(id)
);
