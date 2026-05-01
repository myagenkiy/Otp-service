-- Создание базы данных
CREATE DATABASE otp_db;

\c otp_db;

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    login VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER'))
);

-- Таблица конфигурации
CREATE TABLE IF NOT EXISTS config (
    id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    code_lifetime_seconds INTEGER DEFAULT 300,
    code_length INTEGER DEFAULT 6
);

-- Вставляем дефолтную конфигурацию
INSERT INTO config (id, code_lifetime_seconds, code_length)
VALUES (1, 300, 6)
ON CONFLICT (id) DO NOTHING;

-- Таблица OTP кодов
CREATE TABLE IF NOT EXISTS otp_codes (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    operation_id VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'EXPIRED', 'USED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
);

-- Индексы
CREATE INDEX IF NOT EXISTS idx_otp_expires ON otp_codes(expires_at) WHERE status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_otp_operation ON otp_codes(operation_id);
CREATE INDEX IF NOT EXISTS idx_otp_user ON otp_codes(user_id);

-- Проверка
\dt