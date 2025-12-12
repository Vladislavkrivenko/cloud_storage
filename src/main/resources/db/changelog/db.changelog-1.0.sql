--liquibase formatted sql
--changeset vladislav:1

CREATE TABLE Users
(
    id      SERIAL PRIMARY KEY,
    login    VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);