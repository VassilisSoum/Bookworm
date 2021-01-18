package com.bookworm.application.books.domain.model

sealed abstract case class GenreName private[GenreName](genre: String) {

  private def readResolve(): Object =
    GenreName.create(genre)
}

object GenreName {

  def create(genre: String): Either[ValidationError, GenreName] =
    if (genre.isEmpty) Left(ValidationError.EmptyGenreName)
    else
      Right(new GenreName(genre) {})
}

case class Genre (genreId: GenreId, genreName: GenreName)
