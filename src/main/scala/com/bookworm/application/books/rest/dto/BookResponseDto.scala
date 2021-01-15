package com.bookworm.application.books.rest.dto

import com.bookworm.application.books.domain.{Author, Book}
import com.bookworm.application.books.service.repository.model.Book
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

import java.util.UUID

case class BookResponseDto(id: UUID, title: String, summary: String, isbn: String, authors: List[AuthorResponseDto])

object BookResponseDto {
  implicit val encoder: Encoder[BookResponseDto] = deriveEncoder[BookResponseDto]
  implicit val decoder: Decoder[BookResponseDto] = deriveDecoder[BookResponseDto]

  def from(bookAndAuthors: (Book, List[Author])): BookResponseDto =
    BookResponseDto(
      bookAndAuthors._1.bookId.id,
      bookAndAuthors._1.title,
      bookAndAuthors._1.summary,
      bookAndAuthors._1.isbn,
      bookAndAuthors._2.map(author =>
        AuthorResponseDto(author.authorId.id, author.firstName, author.lastName, List.empty)
      )
    )
}
