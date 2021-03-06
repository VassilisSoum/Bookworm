package com.bookworm.application.books.adapter.api.dto

import com.bookworm.application.books.adapter.api.formats
import com.bookworm.application.books.domain.model.DomainValidationError
import com.bookworm.application.books.domain.model.DomainValidationError._
import org.json4s.JsonAST.JString
import org.json4s.{CustomSerializer, _}

case class ValidationErrorDto(errorType: DomainValidationError, message: String)

object ValidationErrorDto {

  final case object ValidationErrorSerializer
    extends CustomSerializer[DomainValidationError](_ =>
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
            case "InvalidBookId"                  => InvalidBookId
            case "NegativeBookPrice"              => NegativeBookPrice
            case "MaxPriceLessThanMinPrice"       => MaxPriceLessThanMinPrice
          }
        },
        { case validationError: DomainValidationError =>
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

  def fromDomain(validationError: DomainValidationError): ValidationErrorDto =
    validationError match {
      case EmptyBookTitle =>
        ValidationErrorDto(DomainValidationError.EmptyBookTitle, "Book title cannot be empty")
      case EmptyBookSummary =>
        ValidationErrorDto(DomainValidationError.EmptyBookSummary, "Book summary cannot be empty")
      case EmptyBookIsbn =>
        ValidationErrorDto(DomainValidationError.EmptyBookIsbn, "Book isbn cannot be empty")
      case EmptyAuthorFirstName =>
        ValidationErrorDto(DomainValidationError.EmptyAuthorFirstName, "Author first name cannot be empty")
      case EmptyAuthorLastName =>
        ValidationErrorDto(DomainValidationError.EmptyAuthorLastName, "Author last name cannot be empty")
      case EmptyGenreName =>
        ValidationErrorDto(DomainValidationError.EmptyGenreName, "Genre name cannot be empty")
      case EmptyContinuationToken =>
        ValidationErrorDto(
          DomainValidationError.EmptyContinuationToken,
          "Continuation pagination token cannot be empty"
        )
      case InvalidContinuationTokenFormat =>
        ValidationErrorDto(DomainValidationError.InvalidContinuationTokenFormat, "Continuation token format is invalid")
      case NonPositivePaginationLimit =>
        ValidationErrorDto(
          DomainValidationError.NonPositivePaginationLimit,
          "Pagination limit must be a positive number"
        )
      case PaginationLimitExceedsMaximum =>
        ValidationErrorDto(
          DomainValidationError.PaginationLimitExceedsMaximum,
          "Pagination limit is too large"
        )
      case InvalidIsbnLength =>
        ValidationErrorDto(DomainValidationError.InvalidIsbnLength, "Isbn should be 13 characters long")
      case EmptyBookAuthorList =>
        ValidationErrorDto(
          DomainValidationError.EmptyBookAuthorList,
          "List of book authors cannot be empty when adding a book"
        )
      case InvalidBookGenre =>
        ValidationErrorDto(
          DomainValidationError.InvalidBookGenre,
          "Genre is not valid"
        )
      case InvalidBookId =>
        ValidationErrorDto(
          DomainValidationError.InvalidBookId,
          "Book id is not valid"
        )
      case NegativeBookPrice =>
        ValidationErrorDto(
          DomainValidationError.NegativeBookPrice,
          "Book price must not be negative"
        )
      case MaxPriceLessThanMinPrice =>
        ValidationErrorDto(
          DomainValidationError.MaxPriceLessThanMinPrice,
          "Max price cannot be less than min price"
        )
    }
}
