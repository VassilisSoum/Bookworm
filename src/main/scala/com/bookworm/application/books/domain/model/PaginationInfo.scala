package com.bookworm.application.books.domain.model

//TODO: This should be timestamp and a unique id
sealed abstract case class ContinuationToken private[ContinuationToken] (continuationToken: String) {

  private def readResolve(): Object =
    ContinuationToken.create(continuationToken)
}

object ContinuationToken {

  val delimiter = "_"

  def create(continuationToken: String): Either[ValidationError, ContinuationToken] =
    if (continuationToken.isEmpty) Left(ValidationError.EmptyContinuationToken)
    else if (continuationTokenIsInvalid(continuationToken)) Left(ValidationError.InvalidContinuationTokenFormat)
    else
      Right(new ContinuationToken(continuationToken) {})

  private def continuationTokenIsInvalid(continuationToken: String): Boolean =
    !continuationToken.contains(delimiter)
}

sealed abstract case class PaginationLimit private[PaginationLimit] (paginationLimit: Int) {

  private def readResolve(): Object =
    PaginationLimit.create(paginationLimit)
}

object PaginationLimit {

  private val maximumPaginationLimit: Int = 100
  val defaultPaginationLimit: Int = 10

  def create(paginationLimit: Int): Either[ValidationError, PaginationLimit] =
    if (paginationLimit <= 0) Left(ValidationError.NonPositivePaginationLimit)
    else if (paginationLimit > maximumPaginationLimit) Left(ValidationError.PaginationLimitExceedsMaximum)
    else
      Right(new PaginationLimit(paginationLimit) {})
}

case class PaginationInfo(continuationToken: Option[ContinuationToken], limit: PaginationLimit)
