ALTER TABLE credit_cards
    ADD COLUMN credit_limit NUMERIC(19, 2);

ALTER TABLE credit_cards
    ADD CONSTRAINT chk_credit_cards_credit_limit_non_negative
        CHECK (credit_limit IS NULL OR credit_limit >= 0),
    ADD CONSTRAINT chk_credit_cards_current_debt_non_negative
        CHECK (current_debt >= 0),
    ADD CONSTRAINT chk_credit_cards_minimum_payment_non_negative
        CHECK (minimum_payment >= 0),
    ADD CONSTRAINT chk_credit_cards_debt_within_limit
        CHECK (credit_limit IS NULL OR current_debt <= credit_limit);
