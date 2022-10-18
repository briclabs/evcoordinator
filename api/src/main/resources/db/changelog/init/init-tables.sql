-- liquibase formatted sql

-- changeset liquibase:1
CREATE TABLE IF NOT EXISTS test_table(
    id int PRIMARY KEY,
    test text NOT NULL
)