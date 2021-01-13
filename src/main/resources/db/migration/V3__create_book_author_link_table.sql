CREATE TABLE IF NOT EXISTS BOOKWORM.BOOK_AUTHOR(
    bookId UUID NOT NULL,
    authorId UUID NOT NULL,
    PRIMARY KEY (bookId, authorId),
    CONSTRAINT fk_book FOREIGN KEY (bookId) REFERENCES BOOKWORM.BOOK(bookId) ON DELETE CASCADE,
    CONSTRAINT fk_author FOREIGN KEY (authorId) REFERENCES BOOKWORM.AUTHOR(authorId) ON DELETE CASCADE
);