ALTER TABLE urls
ADD COLUMN original_url_hash CHAR(32) GENERATED ALWAYS AS (md5(original_url)) STORED;

CREATE INDEX idx_urls_original_url_hash ON urls(original_url_hash);
