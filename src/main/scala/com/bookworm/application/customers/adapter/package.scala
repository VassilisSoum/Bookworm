package com.bookworm.application.customers

import dev.profunktor.redis4cats.data.RedisCodec
import org.slf4j.{Logger, LoggerFactory}

package object adapter {
  val logger: Logger = LoggerFactory.getLogger(getClass)
  val stringCodec: RedisCodec[String, String] = RedisCodec.Utf8
}
