package com.bookworm.application.books.domain

sealed abstract case class BookTitle private[BookTitle] (title: String) {

  //to ensure validation and possibly singleton-ness, we override readResolve to use explicit companion object factory method
  private def readResolve(): Object =
    BookTitle.create(title)
}

object BookTitle {

  def create(title: String): Either[ValidationError, BookTitle] =
    if (title.isEmpty) Left(ValidationError.EmptyBookTitle)
    else
      Right(new BookTitle(title) {})
}

sealed abstract case class BookSummary private[BookSummary] (summary: String) {

  private def readResolve(): Object =
    BookSummary.create(summary)
}

object BookSummary {

  def create(summary: String): Either[ValidationError, BookSummary] =
    if (summary.isEmpty) Left(ValidationError.EmptyBookSummary)
    else
      Right(new BookSummary(summary) {})
}

sealed abstract case class BookIsbn private[BookIsbn] (isbn: String) {

  private def readResolve(): Object =
    BookIsbn.create(isbn)
}

object BookIsbn {

  def create(isbn: String): Either[ValidationError, BookIsbn] =
    if (isbn.isEmpty) Left(ValidationError.EmptyBookIsbn)
    else
      Right(new BookIsbn(isbn) {})
}

case class BookDetails(title: BookTitle, summary: BookSummary, isbn: BookIsbn, genre: GenreId)
