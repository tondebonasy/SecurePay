CREATE TABLE payment_requests (
  id CHAR(36) PRIMARY KEY,
  payee_id CHAR(36) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  currency VARCHAR(8) NOT NULL,
  note VARCHAR(255) NULL,
  status VARCHAR(16) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  nonce VARCHAR(64) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  consumed_tx_id CHAR(36) NULL,
  INDEX idx_request_payee (payee_id),
  INDEX idx_request_status (status),
  INDEX idx_request_expires (expires_at),
  CONSTRAINT fk_request_payee FOREIGN KEY (payee_id) REFERENCES users(id)
);
