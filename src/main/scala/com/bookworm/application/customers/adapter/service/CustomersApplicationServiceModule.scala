package com.bookworm.application.customers.adapter.service

import com.amazonaws.services.simpleemail.{AmazonSimpleEmailService, AmazonSimpleEmailServiceClientBuilder}
import com.bookworm.application.config.Configuration.CustomerConfig
import com.google.inject.{AbstractModule, Scopes}

import scala.concurrent.ExecutionContext

class CustomersApplicationServiceModule(
    customerConfig: CustomerConfig,
    executionContext: ExecutionContext
) extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[CustomerApplicationService]).in(Scopes.SINGLETON)
    bind(classOf[CustomerVerificationTokenApplicationService]).in(Scopes.SINGLETON)
    bind(classOf[CustomerRegistrationVerificationEmailProducerService]).in(Scopes.SINGLETON)
    bind(classOf[CustomerConfig]).toInstance(customerConfig)
    bind(classOf[ExecutionContext]).toInstance(executionContext)

    val amazonSimpleEmailService: AmazonSimpleEmailService = AmazonSimpleEmailServiceClientBuilder
      .standard()
      .withRegion(customerConfig.customerRegistrationVerificationConfig.awsRegion)
      .build()

    bind(classOf[AmazonSimpleEmailService]).toInstance(amazonSimpleEmailService)
  }
}
