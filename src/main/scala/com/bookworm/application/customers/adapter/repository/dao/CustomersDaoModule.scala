package com.bookworm.application.customers.adapter.repository.dao

import com.google.inject.{AbstractModule, Scopes}

class CustomersDaoModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[CustomerDao]).in(Scopes.SINGLETON)
    bind(classOf[CustomerVerificationTokenDao]).in(Scopes.SINGLETON)
    bind(classOf[EmailTemplateDao]).in(Scopes.SINGLETON)
  }
}
