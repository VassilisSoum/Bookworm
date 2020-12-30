package com.bookworm.application.books.rest.dto

import com.bookworm.application.books.repository.model.Book
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

case class BookDto(id: Long, title: String, summary: String)

object BookDto {
  implicit val encoder: Encoder[BookDto] = deriveEncoder[BookDto]
  implicit val decoder: Decoder[BookDto] = deriveDecoder[BookDto]

  def fromBook(book: Book): BookDto =
    BookDto(book.id, book.title, book.summary)
}
