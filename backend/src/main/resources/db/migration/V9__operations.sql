CREATE TABLE operations (
 id VARCHAR(36) PRIMARY KEY, kind VARCHAR(40) NOT NULL, transaction_id VARCHAR(36),
 actor VARCHAR(36) NOT NULL, state VARCHAR(40) NOT NULL, evidence VARCHAR(4000) NOT NULL,
 created_at TIMESTAMP NOT NULL, dedup_key VARCHAR(160) UNIQUE,
 INDEX idx_operations_kind_created(kind, created_at),
 CONSTRAINT fk_operations_tx FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);
