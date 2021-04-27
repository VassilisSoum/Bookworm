package com.bookworm.application.customers.adapter.service

import cats.effect.{ContextShift, IO}
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.model._
import com.bookworm.application.config.Configuration.{AwsConfig, CustomerConfig}
import com.bookworm.application.customers.adapter.logger
import com.bookworm.application.customers.adapter.service.CustomerRegistrationVerificationEmailProducerService.{customerFirstNameEmailField, customerLastNameEmailField, registrationVerificationLinkField}
import com.bookworm.application.customers.adapter.service.model.SendEmailVerificationServiceModel
import com.bookworm.application.customers.domain.port.inbound.query.EmailTemplateQueryModel
import com.bookworm.application.customers.domain.port.outbound.CustomerEmailTemplateRepository
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}

import java.nio.charset.StandardCharsets
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

class CustomerRegistrationVerificationEmailProducerService @Inject() (
    customerEmailTemplateRepository: CustomerEmailTemplateRepository[ConnectionIO],
    customerConfig: CustomerConfig,
    awsConfig: AwsConfig,
    amazonSimpleEmailService: AmazonSimpleEmailService,
    transactor: Transactor[IO],
    awsSesExecutionContext: ExecutionContext
)(implicit CS: ContextShift[IO]) {

  def sendRegistrationVerificationEmail(
    sendEmailVerificationServiceModel: SendEmailVerificationServiceModel
  ): IO[Try[Unit]] = {
    val registrationVerificationEmailTemplateName =
      customerConfig.customerRegistrationVerificationConfig.registrationVerificationEmailTemplateName
    customerEmailTemplateRepository
      .findBy(registrationVerificationEmailTemplateName)
      .transact(transactor)
      .attempt
      .flatMap {
        case Left(throwable) =>
          IO.delay(logger.error("{}", throwable)).map(_ => Failure(throwable))
        case Right(Some(emailTemplateQueryModel)) =>
          val sendEmailRequest: SendEmailRequest =
            constructEmailRequest(sendEmailVerificationServiceModel, emailTemplateQueryModel)

          CS.evalOn(awsSesExecutionContext)(
            IO.delay(amazonSimpleEmailService.sendEmail(sendEmailRequest)).attempt.flatMap {
              case Left(throwable) =>
                IO.delay(
                  logger.error(
                    s"Cannot sent registration verification email for customer " +
                    s"email ${sendEmailVerificationServiceModel.customerEmail}. Error is $throwable"
                  )
                ).map(_ => Failure(throwable))
              case Right(_) =>
                IO.delay(
                  logger.debug(
                    s"Sent registration verification email to ${sendEmailVerificationServiceModel.customerEmail}"
                  )
                ).map(_ => Success(()))
            }
          )

        case Right(None) =>
          IO.delay(logger.error(s"Unknown template name $registrationVerificationEmailTemplateName"))
            .map(_ => Failure(new IllegalStateException("Unknown template name")))
      }
  }

  private def constructEmailRequest(
    sendEmailVerificationServiceModel: SendEmailVerificationServiceModel,
    emailTemplateQueryModel: EmailTemplateQueryModel
  ): SendEmailRequest =
    new SendEmailRequest()
      .withDestination(
        new Destination().withToAddresses(sendEmailVerificationServiceModel.customerEmail)
      )
      .withMessage(
        new Message()
          .withBody(
            new Body().withHtml(
              new Content()
                .withCharset(StandardCharsets.UTF_8.toString)
                .withData(
                  emailTemplateQueryModel.templateBody
                    .replace(customerFirstNameEmailField, sendEmailVerificationServiceModel.customerFirstName)
                    .replace(customerLastNameEmailField, sendEmailVerificationServiceModel.customerLastName)
                    .replace(
                      registrationVerificationLinkField,
                      sendEmailVerificationServiceModel.verificationToken.toString
                    )
                )
            )
          )
          .withSubject(
            new Content()
              .withCharset(StandardCharsets.UTF_8.toString)
              .withData(emailTemplateQueryModel.templateSubject)
          )
      )
      .withSource(customerConfig.customerRegistrationVerificationConfig.senderEmail)
      .withConfigurationSetName(awsConfig.sesConfigurationSet)
}

object CustomerRegistrationVerificationEmailProducerService {
  private val customerFirstNameEmailField: String = "{CustomerFirstNameField}"
  private val customerLastNameEmailField: String = "{CustomerLastNameField}"
  private val registrationVerificationLinkField: String = "{RegistrationVerificationToken}"
}
