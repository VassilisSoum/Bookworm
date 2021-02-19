package com.bookworm.application.books.adapter.api.dto

import com.bookworm.application.books.adapter.api.formats
import com.bookworm.application.books.domain.model.ValidationError
import com.bookworm.application.books.domain.model.ValidationError._
import org.json4s.JsonAST.JString
import org.json4s.{CustomSerializer, _}

case class ValidationErrorDto(errorType: ValidationError, message: String)

object ValidationErrorDto {

  final case object ValidationErrorSerializer
    extends CustomSerializer[ValidationError](_ =>
      (
        { case JString(validationError) =>
          validationError match {
            case "EmptyBookTitle"                 => EmptyBookTitle
            case "EmptyBookSummary"               => EmptyBookSummary
            case "EmptyBookIsbn"                  => EmptyBookIsbn
            case "EmptyAuthorFirstName"           => EmptyAuthorFirstName
            case "EmptyAuthorLastName"            => EmptyAuthorLastName
            case "EmptyGenreName"                 => EmptyGenreName
            case "EmptyContinuationToken"         => EmptyContinuationToken
            case "InvalidContinuationTokenFormat" => InvalidContinuationTokenFormat
            case "NonPositivePaginationLimit"     => NonPositivePaginationLimit
            case "PaginationLimitExceedsMaximum"  => PaginationLimitExceedsMaximum
            case "InvalidIsbnLength"              => InvalidIsbnLength
            case "EmptyBookAuthorList"            => EmptyBookAuthorList
            case "InvalidBookGenre"               => InvalidBookGenre
          }
        },
        { case validationError: ValidationError =>
          JString(validationError.toString)
        }
      )
    )

  implicit val validationErrorDtoWriter: JsonFormat[ValidationErrorDto] = new JsonFormat[ValidationErrorDto] {

    override def write(validationErrorDto: ValidationErrorDto): JValue =
      Extraction.decompose(validationErrorDto)

    override def read(value: JValue): ValidationErrorDto =
      value.extract[ValidationErrorDto]
  }

  def fromDomain(validationError: ValidationError): ValidationErrorDto =
    validationError match {
      case EmptyBookTitle =>
        ValidationErrorDto(ValidationError.EmptyBookTitle, "Book title cannot be empty")
      case EmptyBookSummary =>
        ValidationErrorDto(ValidationError.EmptyBookSummary, "Book summary cannot be empty")
      case EmptyBookIsbn =>
        ValidationErrorDto(ValidationError.EmptyBookIsbn, "Book isbn cannot be empty")
      case EmptyAuthorFirstName =>
        ValidationErrorDto(ValidationError.EmptyAuthorFirstName, "Author first name cannot be empty")
      case EmptyAuthorLastName =>
        ValidationErrorDto(ValidationError.EmptyAuthorLastName, "Author last name cannot be empty")
      case EmptyGenreName =>
        ValidationErrorDto(ValidationError.EmptyGenreName, "Genre name cannot be empty")
      case EmptyContinuationToken =>
        ValidationErrorDto(
          ValidationError.EmptyContinuationToken,
          "Continuation pagination token cannot be empty"
        )
      case InvalidContinuationTokenFormat =>
        ValidationErrorDto(ValidationError.InvalidContinuationTokenFormat, "Continuation token format is invalid")
      case NonPositivePaginationLimit =>
        ValidationErrorDto(
          ValidationError.NonPositivePaginationLimit,
          "Pagination limit must be a positive number"
        )
      case PaginationLimitExceedsMaximum =>
        ValidationErrorDto(
          ValidationError.PaginationLimitExceedsMaximum,
          "Pagination limit is too large"
        )
      case InvalidIsbnLength =>
        ValidationErrorDto(ValidationError.InvalidIsbnLength, "Isbn should be 13 characters long")
      case EmptyBookAuthorList =>
        ValidationErrorDto(
          ValidationError.EmptyBookAuthorList,
          "List of book authors cannot be empty when adding a book"
        )
      case InvalidBookGenre =>
        ValidationErrorDto(
          ValidationError.InvalidBookGenre,
          "Genre is not valid"
        )
    }
}
