package com.bookworm.application.books.adapter.api.dto

import com.bookworm.application.books.domain.port.inbound.query.BookWithAuthorQuery
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

import java.util.UUID

case class BookAndAuthorResponseDto(
    bookId: UUID,
    title: String,
    summary: String,
    isbn: String,
    genreId: UUID,
    authorId: UUID,
    firstName: String,
    lastName: String
)

object BookAndAuthorResponseDto {

  implicit val encoder: Encoder[BookAndAuthorResponseDto] = deriveEncoder[BookAndAuthorResponseDto]
  implicit val decoder: Decoder[BookAndAuthorResponseDto] = deriveDecoder[BookAndAuthorResponseDto]

  implicit class BookAndAuthorResponseDtoOps(bookWithAuthorQuery: BookWithAuthorQuery) {

    def fromDomain: BookAndAuthorResponseDto =
      BookAndAuthorResponseDto(
        bookId = bookWithAuthorQuery.bookId,
        title = bookWithAuthorQuery.title,
        summary = bookWithAuthorQuery.summary,
        isbn = bookWithAuthorQuery.isbn,
        genreId = bookWithAuthorQuery.genreId,
        authorId = bookWithAuthorQuery.authorId,
        firstName = bookWithAuthorQuery.firstName,
        lastName = bookWithAuthorQuery.lastName
      )
  }
}
