CREATE TABLE users (
    
    id BINARY(16) NOT NULL PRIMARY KEY,

    full_name VARCHAR(50) NOT NULL,

    email VARCHAR(255) UNIQUE NOT NULL,

    provider_id VARCHAR(255),

    password VARCHAR(255),

    auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',

    role_id BIGINT NOT NULL,

    profile_picture VARCHAR(255) NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uk_users_email
        UNIQUE (email),

    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id),

    CONSTRAINT chk_auth_provider 
        CHECK (auth_provider IN ('GOOGLE', 'FACEBOOK', 'LOCAL'))
);

CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_provider_id ON users(provider_id);