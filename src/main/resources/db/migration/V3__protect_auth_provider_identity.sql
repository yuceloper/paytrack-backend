CREATE UNIQUE INDEX IF NOT EXISTS uk_users_auth_provider_subject
    ON users (auth_provider, provider_subject)
    WHERE provider_subject IS NOT NULL;
