package com.bookworm.application.books.adapter.api.dto

import com.bookworm.application.books.adapter.api.formats
import org.json4s._

case class GetBooksResponseDto(books: List[BookResponseDto], nextPage: Option[String])

object GetBooksResponseDto {

  implicit val getBooksResponseDtoFormat = new JsonFormat[GetBooksResponseDto] {

    override def write(getBooksResponseDto: GetBooksResponseDto): JValue =
      Extraction.decompose(getBooksResponseDto)

    override def read(value: JValue): GetBooksResponseDto =
      value.extract[GetBooksResponseDto]
  }
}
