ALTER TABLE users
  ADD COLUMN phone_normalized VARCHAR(32) NULL,
  ADD UNIQUE KEY uq_user_phone_normalized (phone_normalized);

UPDATE users
SET phone_normalized = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(phone, ' ', ''), '+', ''), '-', ''), '(', ''), ')', '');

UPDATE users
SET phone_normalized = CASE
  WHEN phone_normalized LIKE '263%' AND CHAR_LENGTH(phone_normalized) >= 12 THEN CONCAT('0', RIGHT(phone_normalized, 9))
  ELSE phone_normalized
END;
