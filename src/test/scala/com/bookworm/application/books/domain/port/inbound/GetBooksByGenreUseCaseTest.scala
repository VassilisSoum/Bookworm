package com.bookworm.application.books.domain.port.inbound

import com.bookworm.application.AbstractUnitTest
import com.bookworm.application.books.domain.model.{ContinuationToken, GenreId, PaginationInfo, PaginationLimit}
import com.bookworm.application.books.domain.port.outbound.BookRepository

import java.util.UUID
import scala.util.{Success, Try}

class GetBooksByGenreUseCaseTest extends AbstractUnitTest {
  val bookRepository: BookRepository[Try] = mock[BookRepository[Try]]

  val getBookByGenreUseCase: GetBooksByGenreUseCase[Try] = new GetBooksByGenreUseCase[Try](bookRepository)

  "GetBookByGenreUseCase" should {
    "retrieve books given a genre id and pagination information and return a new continuationToken when the " +
    "returned list of books is not empty" in {
      val genreId = GenreId(UUID.randomUUID())
      val continuationToken = ContinuationToken.create("29-01-2021T21:00:45_18727").toOption.get
      val paginationLimit = PaginationLimit.create(1).toOption.get
      val paginationInfo = PaginationInfo(Some(continuationToken), paginationLimit)
      val expectedResponse = List(testBookQueryModel)
      val expectedContinuationToken =
        ContinuationToken
          .create(s"${testBookQueryModel.updatedAt}${ContinuationToken.delimiter}${expectedResponse.head.id}")
          .toOption
          .get
      (bookRepository.getAllByGenre _).expects(genreId, paginationInfo).returns(Success(expectedResponse))
      val actualResponse = getBookByGenreUseCase.retrieveBooksByGenre(genreId, paginationInfo)

      actualResponse.isSuccess shouldBe true
      actualResponse.get.books shouldBe expectedResponse
      actualResponse.get.continuationToken shouldBe Some(expectedContinuationToken)
    }

    "retrieve books given a genre id and pagination information and do not return a new continuationToken when the " +
    "returned list of books is empty" in {
      val genreId = GenreId(UUID.randomUUID())
      val continuationToken = ContinuationToken.create("29-01-2021T21:00:45_18727").toOption.get
      val paginationLimit = PaginationLimit.create(1).toOption.get
      val paginationInfo = PaginationInfo(Some(continuationToken), paginationLimit)
      (bookRepository.getAllByGenre _).expects(genreId, paginationInfo).returns(Success(List.empty))
      val actualResponse = getBookByGenreUseCase.retrieveBooksByGenre(genreId, paginationInfo)

      actualResponse.isSuccess shouldBe true
      actualResponse.get.books shouldBe List.empty
      actualResponse.get.continuationToken shouldBe None
    }
  }
}
