package com.bookworm.application.books.adapter.repository

import com.bookworm.application.books.adapter.repository.dao.AuthorDao
import com.bookworm.application.books.domain.model.AuthorId
import com.bookworm.application.books.domain.port.outbound.AuthorRepository
import doobie.ConnectionIO

import javax.inject.Inject

private[repository] class AuthorRepositoryImpl @Inject() (authorDao: AuthorDao) extends AuthorRepository[ConnectionIO] {

  override def exist(authorIds: List[AuthorId]): ConnectionIO[Boolean] =
    authorDao.allAuthorsExist(authorIds)
}
