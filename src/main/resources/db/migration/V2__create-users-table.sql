CREATE TABLE users (
    id              UUID NOT NULL,
    
    full_name       VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    
    -- Auth info
    auth_provider   VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    provider_id     VARCHAR(255) NULL,        -- sub từ Google/Facebook
    password        VARCHAR(255) NULL,        -- chỉ có khi LOCAL
    
    -- Profile
    profile_picture VARCHAR(255) NULL,
    role_id         BIGINT NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    PRIMARY KEY (id),
    
    CONSTRAINT uk_users_email 
        UNIQUE (email),
    
    CONSTRAINT uk_users_provider 
        UNIQUE (auth_provider, provider_id), 
    
    CONSTRAINT fk_users_role 
        FOREIGN KEY (role_id) REFERENCES roles(id),
    
    CONSTRAINT chk_auth_provider 
        CHECK (auth_provider IN ('GOOGLE', 'FACEBOOK', 'LOCAL')),
    
    -- Ràng buộc logic: LOCAL phải có password, OAuth không được có
    CONSTRAINT chk_local_has_password 
        CHECK (auth_provider != 'LOCAL' OR password IS NOT NULL),
    
    CONSTRAINT chk_oauth_no_password 
        CHECK (auth_provider = 'LOCAL' OR password IS NULL),
    
    CONSTRAINT chk_oauth_has_provider_id 
        CHECK (auth_provider = 'LOCAL' OR provider_id IS NOT NULL)
);

-- Index cho query thường gặp
CREATE INDEX idx_users_email ON users(email);           -- có thể bỏ, UNIQUE đã tạo index
CREATE INDEX idx_users_provider ON users(auth_provider, provider_id);  -- có thể bỏ, UNIQUE đã tạo