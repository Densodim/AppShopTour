CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    preferred_currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    preferred_language VARCHAR(10) NOT NULL DEFAULT 'ru',
    theme_mode VARCHAR(20) NOT NULL DEFAULT 'dark',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
