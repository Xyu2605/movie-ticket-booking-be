CREATE TABLE user_security (
    user_id UUID NOT NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    window_started_at TIMESTAMPTZ,
    locked_until TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_user_security
        PRIMARY KEY (user_id),

    CONSTRAINT fk_user_security_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_failed_attempts
        CHECK (failed_attempts >= 0)
);