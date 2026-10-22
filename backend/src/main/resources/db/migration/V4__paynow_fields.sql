ALTER TABLE transactions
  ADD COLUMN paynow_reference VARCHAR(64) NULL,
  ADD COLUMN paynow_poll_url VARCHAR(512) NULL,
  ADD COLUMN paynow_method VARCHAR(32) NULL,
  ADD COLUMN paynow_phone VARCHAR(32) NULL,
  ADD INDEX idx_tx_paynow_ref (paynow_reference);
