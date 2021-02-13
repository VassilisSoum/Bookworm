package com.bookworm.application.books.adapter.api.dto

import com.bookworm.application.books.adapter.api.formats
import com.bookworm.application.books.domain.port.inbound.query.BookQueryModel
import org.json4s._

final case class BookResponseDto(
    bookId: String,
    title: String,
    summary: String,
    isbn: String,
    genre: String
)

object BookResponseDto {

  implicit val bookResponseDtoFormat: JsonFormat[BookResponseDto] = new JsonFormat[BookResponseDto] {

    override def write(bookResponseDto: BookResponseDto): JValue =
      Extraction.decompose(bookResponseDto)

    override def read(value: JValue): BookResponseDto =
      value.extract[BookResponseDto]
  }

  implicit class BookResponseDtoOps(bookQueryModel: BookQueryModel) {

    def fromDomainQueryModel: BookResponseDto =
      BookResponseDto(
        bookId = bookQueryModel.bookId.toString,
        title = bookQueryModel.title,
        summary = bookQueryModel.summary,
        isbn = bookQueryModel.isbn,
        genre = bookQueryModel.genre
      )
  }
}
