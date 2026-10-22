ALTER TABLE transactions
  ADD COLUMN paid_by_user_id CHAR(36) NULL,
  ADD COLUMN payee_user_id CHAR(36) NULL,
  ADD INDEX idx_tx_paid_by (paid_by_user_id),
  ADD INDEX idx_tx_payee (payee_user_id),
  ADD CONSTRAINT fk_tx_paid_by FOREIGN KEY (paid_by_user_id) REFERENCES users(id),
  ADD CONSTRAINT fk_tx_payee FOREIGN KEY (payee_user_id) REFERENCES users(id);
