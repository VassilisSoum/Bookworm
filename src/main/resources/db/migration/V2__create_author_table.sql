CREATE TABLE IF NOT EXISTS BOOKWORM.AUTHOR(
    authorId UUID PRIMARY KEY,
    firstName TEXT NOT NULL,
    lastName TEXT NOT NULL,
    createdAt TIMESTAMP,
    updatedAt TIMESTAMP
);