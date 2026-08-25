CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS credit_cards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    last_four_digits VARCHAR(255),
    statement_day INTEGER NOT NULL,
    due_day INTEGER NOT NULL,
    current_debt NUMERIC(19,2) NOT NULL DEFAULT 0,
    minimum_payment NUMERIC(19,2) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS loans (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    institution_name VARCHAR(255) NOT NULL,
    installment_amount NUMERIC(19,2) NOT NULL,
    payment_day INTEGER NOT NULL,
    total_installments INTEGER NOT NULL,
    remaining_installments INTEGER NOT NULL,
    remaining_principal NUMERIC(19,2),
    start_date DATE,
    end_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    provider VARCHAR(255),
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(255) NOT NULL,
    billing_period VARCHAR(255) NOT NULL,
    billing_day INTEGER,
    next_billing_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS bills (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    provider VARCHAR(255),
    category VARCHAR(255) NOT NULL,
    expected_amount NUMERIC(19,2),
    due_day INTEGER NOT NULL,
    next_due_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    institution VARCHAR(255),
    balance NUMERIC(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'TRY',
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS transaction_categories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    icon_key VARCHAR(255),
    built_in BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (user_id, name)
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    source_type VARCHAR(255),
    source_id BIGINT,
    amount NUMERIC(19,2) NOT NULL,
    due_date DATE NOT NULL,
    recurring BOOLEAN NOT NULL DEFAULT FALSE,
    series_id VARCHAR(255),
    recurrence_day INTEGER,
    recurrence_frequency VARCHAR(255),
    recurrence_interval INTEGER,
    recurrence_end_date DATE,
    paid BOOLEAN NOT NULL DEFAULT FALSE,
    institution VARCHAR(255),
    note VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS income_sources (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'TRY',
    frequency VARCHAR(255) NOT NULL,
    recurrence_day INTEGER,
    recurrence_interval INTEGER,
    recurrence_end_date DATE,
    next_income_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    note VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS income_occurrences (
    id BIGSERIAL PRIMARY KEY,
    income_source_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    expected_date DATE NOT NULL,
    received BOOLEAN NOT NULL DEFAULT FALSE,
    received_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_income_occurrence_source_date UNIQUE (income_source_id, expected_date)
);

CREATE TABLE IF NOT EXISTS account_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    account_id BIGINT NOT NULL,
    counter_account_id BIGINT,
    category_id BIGINT,
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    occurred_on DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    source_type VARCHAR(255),
    source_id BIGINT,
    reversed BOOLEAN NOT NULL DEFAULT FALSE,
    reversed_at TIMESTAMP WITH TIME ZONE
);

ALTER TABLE payments ADD COLUMN IF NOT EXISTS series_id VARCHAR(255);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS recurrence_day INTEGER;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS recurrence_frequency VARCHAR(255);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS recurrence_interval INTEGER;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS recurrence_end_date DATE;

ALTER TABLE income_sources ADD COLUMN IF NOT EXISTS recurrence_interval INTEGER;
ALTER TABLE income_sources ADD COLUMN IF NOT EXISTS recurrence_end_date DATE;

ALTER TABLE account_transactions ADD COLUMN IF NOT EXISTS category_id BIGINT;
ALTER TABLE account_transactions ADD COLUMN IF NOT EXISTS reversed BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE account_transactions ADD COLUMN IF NOT EXISTS reversed_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_payments_user_due_date ON payments(user_id, due_date);
CREATE INDEX IF NOT EXISTS idx_payments_series_date ON payments(series_id, due_date);
CREATE INDEX IF NOT EXISTS idx_income_occurrences_user_date ON income_occurrences(user_id, expected_date);
CREATE INDEX IF NOT EXISTS idx_account_transactions_user_date ON account_transactions(user_id, occurred_on);
