INSERT INTO users (
    id,
    name,
    email,
    password_hash,
    preferred_currency,
    preferred_language,
    theme_mode,
    created_at,
    updated_at
) VALUES
(
    '11111111-1111-1111-1111-111111111111',
    'Ivan Petrov',
    'ivan@example.com',
    'test_hash_ivan',
    'EUR',
    'ru',
    'dark',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    '22222222-2222-2222-2222-222222222222',
    'Anna Sidorova',
    'anna@example.com',
    'test_hash_anna',
    'USD',
    'en',
    'light',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
