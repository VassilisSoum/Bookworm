package com.bookworm.application.books.adapter.api.dto

import com.bookworm.application.books.domain.model.ValidationError
import com.bookworm.application.books.domain.model.ValidationError._
import org.json4s.JsonAST.JString
import org.json4s.{CustomSerializer, _}

sealed trait ValidationErrorType

object ValidationErrorType {
  final case object EmptyBookTitle extends ValidationErrorType
  final case object EmptyBookSummary extends ValidationErrorType
  final case object EmptyBookIsbn extends ValidationErrorType
  final case object EmptyAuthorFirstName extends ValidationErrorType
  final case object EmptyAuthorLastName extends ValidationErrorType
  final case object EmptyGenreName extends ValidationErrorType
  final case object EmptyContinuationToken extends ValidationErrorType
  final case object InvalidContinuationTokenFormat extends ValidationErrorType
  final case object NonPositivePaginationLimit extends ValidationErrorType
  final case object PaginationLimitExceedsMaximum extends ValidationErrorType

  final case object ValidationErrorTypeSerializer
    extends CustomSerializer[ValidationErrorType](_ =>
      (
        { case JString(validationErrorType) =>
          validationErrorType match {
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
          }
        },
        { case validationErrorType: ValidationErrorType =>
          JString(validationErrorType.toString)
        }
      )
    )
}

case class ValidationErrorDto(errorType: ValidationErrorType, message: String)

object ValidationErrorDto {

  implicit val formats = DefaultFormats + ValidationErrorType.ValidationErrorTypeSerializer

  implicit val validationErrorDtoWriter: JsonFormat[ValidationErrorDto] = new JsonFormat[ValidationErrorDto] {

    override def write(validationErrorDto: ValidationErrorDto): JValue =
      Extraction.decompose(validationErrorDto)

    override def read(value: JValue): ValidationErrorDto =
      value.extract[ValidationErrorDto]
  }

  implicit class ValidationErrorDtoOps(validationError: ValidationError) {

    def fromDomain: ValidationErrorDto =
      validationError match {
        case EmptyBookTitle =>
          ValidationErrorDto(ValidationErrorType.EmptyBookTitle, "Book title cannot be empty")
        case EmptyBookSummary =>
          ValidationErrorDto(ValidationErrorType.EmptyBookSummary, "Book summary cannot be empty")
        case EmptyBookIsbn =>
          ValidationErrorDto(ValidationErrorType.EmptyBookIsbn, "Book isbn cannot be empty")
        case EmptyAuthorFirstName =>
          ValidationErrorDto(ValidationErrorType.EmptyAuthorFirstName, "Author first name cannot be empty")
        case EmptyAuthorLastName =>
          ValidationErrorDto(ValidationErrorType.EmptyAuthorLastName, "Author last name cannot be empty")
        case EmptyGenreName =>
          ValidationErrorDto(ValidationErrorType.EmptyGenreName, "Genre name cannot be empty")
        case EmptyContinuationToken =>
          ValidationErrorDto(
            ValidationErrorType.EmptyContinuationToken,
            "Continuation pagination token cannot be empty"
          )
        case InvalidContinuationTokenFormat =>
          ValidationErrorDto(ValidationErrorType.InvalidContinuationTokenFormat, "Continuation token format is invalid")
        case NonPositivePaginationLimit =>
          ValidationErrorDto(
            ValidationErrorType.NonPositivePaginationLimit,
            "Pagination limit must be a positive number"
          )
        case PaginationLimitExceedsMaximum =>
          ValidationErrorDto(
            ValidationErrorType.PaginationLimitExceedsMaximum,
            "Pagination limit is too large"
          )
      }
  }
}
