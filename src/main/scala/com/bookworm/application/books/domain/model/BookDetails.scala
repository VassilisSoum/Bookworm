package com.bookworm.application.books.domain.model

sealed abstract case class BookTitle private[BookTitle] (value: String) {

  //to ensure validation and possibly singleton-ness, we override readResolve to use explicit companion object factory method
  private def readResolve(): Object =
    BookTitle.create(value)
}

object BookTitle {

  def create(title: String): Either[DomainValidationError, BookTitle] =
    if (title.isEmpty) Left(DomainValidationError.EmptyBookTitle)
    else
      Right(new BookTitle(title) {})
}

sealed abstract case class BookSummary private[BookSummary] (value: String) {

  private def readResolve(): Object =
    BookSummary.create(value)
}

object BookSummary {

  def create(summary: String): Either[DomainValidationError, BookSummary] =
    if (summary.isEmpty) Left(DomainValidationError.EmptyBookSummary)
    else
      Right(new BookSummary(summary) {})
}

sealed abstract case class BookIsbn private[BookIsbn] (value: String) {

  private def readResolve(): Object =
    BookIsbn.create(value)
}

object BookIsbn {

  def create(isbn: String): Either[DomainValidationError, BookIsbn] =
    if (isbn.isEmpty) Left(DomainValidationError.EmptyBookIsbn)
    else if (isbn.length != 13) Left(DomainValidationError.InvalidIsbnLength)
    else
      Right(new BookIsbn(isbn) {})
}

case class BookDetails(title: BookTitle, summary: BookSummary, isbn: BookIsbn, genre: GenreId, authors: List[AuthorId])
