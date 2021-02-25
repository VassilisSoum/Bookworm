package com.bookworm.application.books.adapter.api.dto

import cats.syntax.list._
import com.bookworm.application.books.adapter.api.formats
import com.bookworm.application.books.domain.model._
import org.json4s.{Extraction, JValue, JsonFormat}

import java.util.UUID
import scala.util.Try

case class AddBookRequestDto(
    title: String,
    summary: String,
    isbn: String,
    genreId: String,
    authorIds: List[String]
)

object AddBookRequestDto {

  implicit val createBookRequestDtoJsonFormat: JsonFormat[AddBookRequestDto] = new JsonFormat[AddBookRequestDto] {

    override def read(value: JValue): AddBookRequestDto =
      value.extract[AddBookRequestDto]

    override def write(createBookRequestDto: AddBookRequestDto): JValue =
      Extraction.decompose(createBookRequestDto)
  }

  implicit class AddBookRequestDtoOps(addBookRequestDto: AddBookRequestDto) {

    def toDomainModel: Either[DomainValidationError, Book] =
      for {
        title <- BookTitle.create(addBookRequestDto.title)
        summary <- BookSummary.create(addBookRequestDto.summary)
        isbn <- BookIsbn.create(addBookRequestDto.isbn)
        authorIds <- addBookRequestDto.authorIds.toNel.toRight(DomainValidationError.EmptyBookAuthorList)
        genreId <- Try(UUID.fromString(addBookRequestDto.genreId)).toEither.left.map(_ =>
          DomainValidationError.InvalidBookGenre
        )
        bookId = BookId(UUID.randomUUID())
        book = Book(
          bookId,
          BookDetails(
            title,
            summary,
            isbn,
            GenreId(genreId),
            authorIds.toList.map(authorId => AuthorId(UUID.fromString(authorId)))
          ),
          BookStatus.Available
        )
      } yield book
  }
}
