package com.bookworm.application.integration.books

import cats.free.Free
import com.bookworm.application.IntegrationTestModule
import com.bookworm.application.books.domain.model.BookStatus.{Available, Unavailable}
import com.bookworm.application.books.domain.model._
import doobie.ConnectionIO
import doobie.free.connection
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres.implicits._

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime}
import java.util.UUID

trait TestData extends IntegrationTestModule {

  val testGenreId: GenreId = GenreId(UUID.randomUUID())
  val testGenreName: String = "TestGenre"
  val testBookId: BookId = BookId(UUID.randomUUID())
  val testBookTitle: BookTitle = BookTitle.create("TestBookTitle").toOption.get
  val testBookSummary: BookSummary = BookSummary.create("TestBookSummary").toOption.get
  val testBookIsbn: BookIsbn = BookIsbn.create("9781234567897").toOption.get
  val testAuthorId: AuthorId = AuthorId(UUID.randomUUID())
  val testBookMinPrice: BookPrice = BookPrice.create(1000L).toOption.get
  val testBookMaxPrice: BookPrice = BookPrice.create(5000L).toOption.get
  val testGenre: Genre = Genre(testGenreId, GenreName.create(testGenreName).toOption.get)

  val testBookDetails: BookDetails = BookDetails
    .create(
      testBookTitle,
      testBookSummary,
      testBookIsbn,
      testGenreId,
      List(testAuthorId),
      testBookMinPrice,
      testBookMaxPrice
    )
    .toOption
    .get

  val testBook: Book =
    Book(
      bookId = testBookId,
      bookDetails = testBookDetails,
      bookStatus = BookStatus.Available
    )
  val testAuthorFirstName: AuthorFirstName = AuthorFirstName.create("TestAuthorFirstName").toOption.get
  val testAuthorLastName: AuthorLastName = AuthorLastName.create("TestAuthorLastName").toOption.get
  val testAuthor: Author = Author(testAuthorId, AuthorDetails(testAuthorFirstName, testAuthorLastName))

  val testPaginationLimit: PaginationLimit = PaginationLimit.create(PaginationLimit.defaultPaginationLimit).toOption.get

  override def afterAll(): Unit = {
    val transaction = for {
      _ <- sql"""truncate table bookworm.author CASCADE""".update.run
      _ <- sql"""truncate table bookworm.book CASCADE""".update.run
      _ <- sql"""truncate table bookworm.book_author CASCADE""".update.run
      _ <- sql"""truncate table bookworm.genre CASCADE""".update.run
    } yield ()

    transaction.transact(this.synchronousTransactor).unsafeRunSync()
    super.afterAll()
  }

  def insertIntoGenre(genre: Genre): ConnectionIO[Int] =
    sql"""insert into bookworm.genre(genreId, genreName)
         values (${genre.genreId.id}, ${genre.genreName.genre})
         """.update.run

  def insertIntoAuthor(author: Author): ConnectionIO[Int] = {
    val now: Timestamp = Timestamp.valueOf(LocalDateTime.now(fakeClock))
    sql"""insert into bookworm.author(authorId,firstName,lastName,createdAt,updatedAt)
         values (
          ${author.authorId.id},
          ${author.authorDetails.firstName.firstName},
          ${author.authorDetails.lastName.lastName},
          $now,
          $now
         )""".update.run
  }

  def insertIntoBook(book: Book): ConnectionIO[Long] = {
    val now: Timestamp = Timestamp.valueOf(LocalDateTime.now(fakeClock))
    val deleted = book.bookStatus match {
      case Available   => false
      case Unavailable => true
    }
    sql"""insert into bookworm.book(bookId,title,summary,isbn,genreId,deleted,minPrice,maxPrice,createdAt,updatedAt)
         values (
          ${book.bookId.id},
          ${book.bookDetails.title.value},
          ${book.bookDetails.summary.value},
          ${book.bookDetails.isbn.value},
          ${book.bookDetails.genre.id},
          $deleted,
          ${book.bookDetails.minPrice.value},
          ${book.bookDetails.maxPrice.value},
          $now,
          $now
         )""".update.withUniqueGeneratedKeys("id")
  }

  def insertIntoBookAuthor(bookId: BookId, authorId: AuthorId): ConnectionIO[Int] =
    sql"""insert into bookworm.book_author(bookId,authorId) 
         values (${bookId.id},${authorId.id})
         """.update.run

  def setupInitialData(): Unit = {
    val transaction: Free[connection.ConnectionOp, Unit] = for {
      _ <- insertIntoGenre(testGenre)
      _ <- insertIntoAuthor(testAuthor)
      _ <- insertIntoBook(testBook)
      _ <- insertIntoBookAuthor(testBookId, testAuthorId)
    } yield ()

    transaction.transact(this.synchronousTransactor).unsafeRunSync()
  }

  def runInTransaction[A](transaction: Free[connection.ConnectionOp, A]): A =
    transaction.transact(this.synchronousTransactor).unsafeRunSync()

  def advanceClockInMillis(value: Long): Instant = {
    fakeClock.current = fakeClock.current.plusMillis(value)
    fakeClock.current
  }

  def setClockAt(value: Instant): Unit =
    fakeClock.current = value
}
