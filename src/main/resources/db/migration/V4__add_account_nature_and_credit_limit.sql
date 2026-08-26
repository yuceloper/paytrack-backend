ALTER TABLE accounts
    ADD COLUMN nature VARCHAR(20) NOT NULL DEFAULT 'ASSET',
    ADD COLUMN credit_limit NUMERIC(19, 2);

ALTER TABLE accounts
    ADD CONSTRAINT chk_accounts_nature CHECK (nature IN ('ASSET', 'LIABILITY')),
    ADD CONSTRAINT chk_accounts_credit_limit_non_negative CHECK (credit_limit IS NULL OR credit_limit >= 0);
