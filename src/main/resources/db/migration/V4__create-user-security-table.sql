CREATE TABLE user_security (
    user_id UUID NOT NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    window_started_at TIMESTAMPTZ,
    locked_until TIMESTAMPTZ,
    last_failed_at TIMESTAMPTZ,
    last_failed_ip VARCHAR(45),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
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
--  Index cho query admin "list user đang bị lock"
CREATE INDEX idx_user_security_locked_until 
    ON user_security(locked_until) 
    WHERE locked_until IS NOT NULL;

-- Index cho query "tìm user bị fail từ IP nào"
CREATE INDEX idx_user_security_last_failed_ip 
    ON user_security(last_failed_ip) 
    WHERE last_failed_ip IS NOT NULL;