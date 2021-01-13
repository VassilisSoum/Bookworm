package com.bookworm.application.books.rest.dto

import com.bookworm.application.books.service.repository.model.Book
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

import java.util.UUID

case class BookResponseDto(id: UUID, title: String, summary: String, isbn: String, authors: List[AuthorResponseDto])

object BookResponseDto {
  implicit val encoder: Encoder[BookResponseDto] = deriveEncoder[BookResponseDto]
  implicit val decoder: Decoder[BookResponseDto] = deriveDecoder[BookResponseDto]

  def fromBook(book: Book): BookResponseDto =
    BookResponseDto(
      book.bookId.id,
      book.title,
      book.summary,
      book.isbn,
      book.authors.map(author => AuthorResponseDto(author.authorId.id, author.firstName, author.lastName, List.empty))
    )
}
