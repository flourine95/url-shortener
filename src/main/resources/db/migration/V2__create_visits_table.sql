CREATE TABLE visits (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(512),
    clicked_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_visits_short_code ON visits(short_code);
