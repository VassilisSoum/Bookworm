package com.bookworm.application.books.service

import cats.effect.IO
import com.bookworm.application.books.service.repository.BookRepository
import com.bookworm.application.books.service.repository.model.{Author, Book}

import java.util.UUID
import javax.inject.Inject

trait BookService {
  def retrieveAllBooks(genreId: UUID): IO[Map[Book, List[Author]]]
}

private[service] class BookServiceImpl @Inject() (bookRepository: BookRepository) extends BookService {

  override def retrieveAllBooks(genre: UUID): IO[Map[Book, List[Author]]] =
    bookRepository.getBooksAndAuthorsForGenre(genre)
}
