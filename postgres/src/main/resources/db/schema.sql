-- Схема таблиц для репозиториев вопросов и ответов

CREATE TABLE IF NOT EXISTS questions (
    id   VARCHAR(36) PRIMARY KEY,
    text TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS answers (
    id          SERIAL PRIMARY KEY,
    question_id VARCHAR(36) NOT NULL REFERENCES questions(id),
    text        TEXT        NOT NULL
);

-- Начальные данные: тестовые вопросы
INSERT INTO questions (id, text) VALUES
    ('550e8400-e29b-41d4-a716-446655440000', 'What is your name?'),
    ('6ba7b810-9dad-11d1-80b4-00c04fd430c8', 'What is your quest?')
ON CONFLICT (id) DO NOTHING;
