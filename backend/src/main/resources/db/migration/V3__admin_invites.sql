CREATE TABLE admin_invites (
  id CHAR(36) PRIMARY KEY,
  code_hash VARCHAR(255) NOT NULL,
  created_by_user_id CHAR(36) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  consumed_at TIMESTAMP NULL,
  consumed_by_user_id CHAR(36) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_invite_code (code_hash),
  INDEX idx_invite_expires (expires_at),
  CONSTRAINT fk_invite_creator FOREIGN KEY (created_by_user_id) REFERENCES users(id),
  CONSTRAINT fk_invite_consumer FOREIGN KEY (consumed_by_user_id) REFERENCES users(id)
);
