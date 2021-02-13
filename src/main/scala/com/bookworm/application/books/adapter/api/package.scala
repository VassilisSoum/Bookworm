package com.bookworm.application.books.adapter

import com.bookworm.application.books.adapter.api.dto.BusinessErrorDto.BusinessErrorSerializer
import com.bookworm.application.books.adapter.api.dto.ValidationErrorDto.ValidationErrorSerializer
import org.json4s.{DefaultFormats, Formats}

package object api {

  implicit val formats: Formats = DefaultFormats + ValidationErrorSerializer + BusinessErrorSerializer
}
