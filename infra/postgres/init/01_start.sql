-- Runs once, automatically, the first time the postgres container
-- initializes its data volume (docker-entrypoint-initdb.d convention).
--
-- "une base par service" (per Conception.pdf): each Java/Python service
-- that needs persistence gets its own database.

CREATE DATABASE dispatch_db;
CREATE DATABASE pro_registry_db;
CREATE DATABASE matching_db;
CREATE DATABASE evaluation_db;

-- Grant the default app user full rights on each (single-user dev setup;
-- tighten this later if you add per-service DB credentials).
GRANT ALL PRIVILEGES ON DATABASE dispatch_db TO CURRENT_USER;
GRANT ALL PRIVILEGES ON DATABASE pro_registry_db TO CURRENT_USER;
GRANT ALL PRIVILEGES ON DATABASE matching_db TO CURRENT_USER;
GRANT ALL PRIVILEGES ON DATABASE evaluation_db TO CURRENT_USER;