package com.bookworm.application.books.adapter.repository

import com.bookworm.application.books.domain.model.AuthorId
import com.bookworm.application.integration.books.TestData
import org.scalatest.MustMatchers.convertToAnyMustWrapper

import java.time.LocalDateTime
import java.util.UUID

class AuthorRepositoryImplTest extends TestData {

  override def beforeAll(): Unit = {
    setupInitialData()
    super.beforeAll()
  }

  "AuthorRepositoryImpl" should {
    fakeClock.current = LocalDateTime
      .of(2025, 2, 7, 10, 0, 0)
      .atZone(fakeClock.zoneId)
      .toInstant
    val authorRepository = injector.getInstance(classOf[AuthorRepositoryImpl])

    "return true when searching for existence of provided author ids" in {
      runInTransaction(authorRepository.exist(List(testAuthorId))) mustBe true
    }

    "return false when searching for existence of provided author ids" in {
      runInTransaction(authorRepository.exist(List(AuthorId(UUID.randomUUID())))) mustBe false
    }

    "return all authors of a provided book id" in {
      val authors = runInTransaction(authorRepository.getAllByBookId(testBookId))
      authors.size mustBe 1
      authors.head.authorId mustBe testAuthorId.id
      authors.head.firstName mustBe testAuthorFirstName.firstName
      authors.head.lastName mustBe testAuthorLastName.lastName
    }
  }

}
