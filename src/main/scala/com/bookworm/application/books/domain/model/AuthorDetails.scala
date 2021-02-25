package com.bookworm.application.books.domain.model

sealed abstract case class AuthorFirstName private[AuthorFirstName](firstName: String) {

  private def readResolve(): Object =
    AuthorFirstName.create(firstName)
}

object AuthorFirstName {

  def create(firstName: String): Either[DomainValidationError, AuthorFirstName] =
    if (firstName.isEmpty) Left(DomainValidationError.EmptyAuthorFirstName)
    else
      Right(new AuthorFirstName(firstName) {})
}

sealed abstract case class AuthorLastName private[AuthorLastName](lastName: String) {

  private def readResolve(): Object =
    AuthorLastName.create(lastName)
}

object AuthorLastName {

  def create(lastName: String): Either[DomainValidationError, AuthorLastName] =
    if (lastName.isEmpty) Left(DomainValidationError.EmptyAuthorLastName)
    else
      Right(new AuthorLastName(lastName) {})
}

case class AuthorDetails(firstName: AuthorFirstName, lastName: AuthorLastName)
