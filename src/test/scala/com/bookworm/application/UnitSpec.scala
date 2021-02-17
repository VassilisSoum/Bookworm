package com.bookworm.application

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

abstract class UnitSpec extends WordSpec with Matchers with MockFactory {}
