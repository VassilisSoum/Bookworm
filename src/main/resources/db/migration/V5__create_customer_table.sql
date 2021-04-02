CREATE EXTENSION pgcrypto;

CREATE TABLE IF NOT EXISTS BOOKWORM.CUSTOMER
(
    id                 UUID      NOT NULL PRIMARY KEY,
    username           TEXT      NOT NULL UNIQUE, -- username is email
    password           TEXT      NOT NULL,
    firstName          TEXT      NOT NULL,
    lastName           TEXT      NOT NULL,
    age                SMALLINT  NOT NULL,
    registrationStatus TEXT      NOT NULL,
    createdAt          TIMESTAMP NOT NULL,
    updatedAt          TIMESTAMP NOT NULL
);