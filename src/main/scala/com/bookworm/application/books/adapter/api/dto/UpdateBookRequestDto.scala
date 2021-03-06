package com.bookworm.application.books.adapter.api.dto

import cats.syntax.list._
import com.bookworm.application.books.adapter.api.formats
import com.bookworm.application.books.domain.model._
import org.json4s.{Extraction, JValue, JsonFormat}

import java.util.UUID
import scala.util.Try

case class UpdateBookRequestDto(
    title: String,
    summary: String,
    isbn: String,
    genreId: String,
    authorIds: List[String],
    minPrice: Long,
    maxPrice: Long
)

object UpdateBookRequestDto {

  implicit val updateBookRequestDtoJsonFormat: JsonFormat[UpdateBookRequestDto] = new JsonFormat[UpdateBookRequestDto] {

    override def read(value: JValue): UpdateBookRequestDto =
      value.extract[UpdateBookRequestDto]

    override def write(updateBookRequestDto: UpdateBookRequestDto): JValue =
      Extraction.decompose(updateBookRequestDto)
  }

  implicit class UpdateBookRequestDtoOps(updateBookRequestDto: UpdateBookRequestDto) {

    def toDomainModel(bookId: BookId): Either[DomainValidationError, Book] =
      for {
        title <- BookTitle.create(updateBookRequestDto.title)
        summary <- BookSummary.create(updateBookRequestDto.summary)
        isbn <- BookIsbn.create(updateBookRequestDto.isbn)
        authorIds <- updateBookRequestDto.authorIds.toNel.toRight(DomainValidationError.EmptyBookAuthorList)
        genreId <- Try(UUID.fromString(updateBookRequestDto.genreId)).toEither.left.map(_ =>
          DomainValidationError.InvalidBookGenre
        )
        minPrice <- BookPrice.create(updateBookRequestDto.minPrice)
        maxPrice <- BookPrice.create(updateBookRequestDto.maxPrice)
        genre = GenreId(genreId)
        authors = authorIds.toList.map(authorId => AuthorId(UUID.fromString(authorId)))
        bookDetails <- BookDetails.create(title, summary, isbn, genre, authors, minPrice, maxPrice)
        book = Book(
          bookId,
          bookDetails,
          bookStatus = BookStatus.Available
        )
      } yield book
  }
}
