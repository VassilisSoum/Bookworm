CREATE SCHEMA BOOKWORM;

CREATE TABLE IF NOT EXISTS BOOKWORM.BOOK(
    bookId UUID PRIMARY KEY,
    title TEXT NOT NULL,
    summary TEXT NOT NULL,
    isbn TEXT NOT NULL
);