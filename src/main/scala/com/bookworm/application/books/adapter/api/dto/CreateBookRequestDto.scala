package com.bookworm.application.books.adapter.api.dto

import com.bookworm.application.books.adapter.api.formats
import com.bookworm.application.books.domain.model._
import org.json4s.{JValue, Reader}

import java.util.UUID

case class CreateBookRequestDto(
    bookId: String, //TODO: Change to UUID in the response too
    title: String,
    summary: String,
    isbn: String,
    genre: String,
    authorIds: List[UUID]
)

object CreateBookRequestDto {

  implicit val createBookRequestDtoReads: Reader[CreateBookRequestDto] = (value: JValue) => {
    value.extract[CreateBookRequestDto]
  }

  implicit class CreateBookRequestDtoOps(createBookRequestDto: CreateBookRequestDto) {

    def toDomainModel: Either[ValidationError, Book] =
      for {
        title <- BookTitle.create(createBookRequestDto.title)
        summary <- BookSummary.create(createBookRequestDto.summary)
        isbn <- BookIsbn.create(createBookRequestDto.isbn)
        genreId = GenreId(UUID.randomUUID())
        bookId = BookId(UUID.randomUUID())
        authorIds = createBookRequestDto.authorIds.map(AuthorId)
        book = Book(bookId, BookDetails(title, summary, isbn, genreId, authorIds))
      } yield book
  }
}
