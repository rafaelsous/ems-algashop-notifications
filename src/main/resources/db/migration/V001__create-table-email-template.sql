CREATE TABLE IF NOT EXISTS email_template (
    id uuid NOT NULL,
    name VARCHAR(100) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT pk_email_template PRIMARY KEY (id)
);