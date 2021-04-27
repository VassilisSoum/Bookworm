package com.bookworm.application.customers.adapter.service

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.bookworm.application.config.Configuration.{AwsConfig, CustomerConfig}
import com.google.inject.{AbstractModule, Scopes}

import scala.concurrent.ExecutionContext

class CustomersApplicationServiceModule(
    customerConfig: CustomerConfig,
    awsConfig: AwsConfig,
    amazonSimpleEmailService: AmazonSimpleEmailService,
    executionContext: ExecutionContext
) extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[CustomerApplicationService]).in(Scopes.SINGLETON)
    bind(classOf[CustomerVerificationTokenApplicationService]).in(Scopes.SINGLETON)
    bind(classOf[CustomerRegistrationVerificationEmailProducerService]).in(Scopes.SINGLETON)
    bind(classOf[CustomerConfig]).toInstance(customerConfig)
    bind(classOf[AwsConfig]).toInstance(awsConfig)
    bind(classOf[ExecutionContext]).toInstance(executionContext) //TODO: Differentiate it that for supporting multiple execution contexts
    bind(classOf[AmazonSimpleEmailService]).toInstance(amazonSimpleEmailService)
  }
}
