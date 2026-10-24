-- Runs once, the first time the postgres container starts with an empty volume.
-- POSTGRES_DB already creates inventory_db; here we add a second, separate database for order-service.
CREATE DATABASE order_db;
