package com.bookworm.application.service

import cats.effect.IO
import com.bookworm.application.repository.BookRepository
import com.bookworm.application.repository.model.Book

import javax.inject.Inject

trait BookService {
  def retrieveAllBooks: IO[List[Book]]
}

private[service] class BookServiceImpl @Inject() (bookRepository: BookRepository) extends BookService {

  override def retrieveAllBooks: IO[List[Book]] =
    bookRepository.getBooks
}
