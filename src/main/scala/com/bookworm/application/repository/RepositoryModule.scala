package com.bookworm.application.repository

import com.bookworm.application.repository.dao.{BookDao, BookDaoImpl}
import com.google.inject.{AbstractModule, Scopes}
import net.codingwell.scalaguice.ScalaModule

class RepositoryModule extends AbstractModule with ScalaModule {
  /*lazy val bookDao: BookDao = wire[BookDaoImpl]

  lazy val bookRepository: BookRepository = wire[BookRepositoryImpl]

  lazy val transactionManager: TransactionManager = wire[TransactionManager]*/

  override def configure(): Unit = {
    bind[BookDao].to[BookDaoImpl].in(Scopes.SINGLETON)
    bind[BookRepository].to[BookRepositoryImpl].in(Scopes.SINGLETON)
  }
}
